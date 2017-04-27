package wbs.sms.message.ticker.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.max;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlUtils.htmlColourFromObject;

import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.console.async.ConsoleAsyncConnectionHandle;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.core.console.ConsoleAsyncSubscription;
import wbs.platform.media.console.MediaConsoleHelper;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.ticker.console.MessageTickerUpdateAsyncHelper.SubscriberState;

import wbs.utils.time.TimeFormatter;

@SingletonComponent ("messageTickerUpdateAsyncHelper")
public
class MessageTickerUpdateAsyncHelper
	implements ConsoleAsyncSubscription.Helper <SubscriberState> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MediaConsoleHelper mediaHelper;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageTickerManager messageTickerManager;

	@SingletonDependency
	TimeFormatter timeFormatter;

	@SingletonDependency
	WbsConfig wbsConfig;

	// state

	Collection <MessageTickerMessage> messageTickerMessages;

	// details

	@Override
	public
	String endpointPath () {
		return "/sms-message-ticker/update";
	}

	@Override
	public
	String endpointName () {
		return "Message ticker update";
	}

	// implementation

	@Override
	public
	SubscriberState newSubscription (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"newSubscription");

		) {

			return new SubscriberState ()

				.generation (
					0l);

		}

	}

	@Override
	public
	void prepareUpdate (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"prepareUpdate");

		) {

			messageTickerMessages =
				messageTickerManager.getMessages ();

		}

	}

	@Override
	public
	void updateSubscriber (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SubscriberState state,
			@NonNull ConsoleAsyncConnectionHandle connectionHandle,
			@NonNull OwnedTransaction transaction,
			@NonNull UserRec user,
			@NonNull UserPrivChecker privChecker) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"updateSubscriber");

		) {

			JsonArray messagesArray =
				new JsonArray ();

			JsonArray statusesArray =
				new JsonArray ();

			Long newGeneration =
				state.generation ();

			for (
				MessageTickerMessage messageTickerMessage
					: messageTickerMessages
			) {

				if (
					lessThan (
						state.generation (),
						messageTickerMessage.messageGeneration ())
				) {

					if (
						messageIsVisible (
							taskLogger,
							privChecker,
							messageTickerMessage)
					) {

						messagesArray.add (
							createMessage (
								taskLogger,
								user,
								messageTickerMessage));

					}

				} else if (
					lessThan (
						state.generation (),
						messageTickerMessage.statusGeneration ())
				) {

					if (
						messageIsVisible (
							taskLogger,
							privChecker,
							messageTickerMessage)
					) {

						statusesArray.add (
							createStatus (
								taskLogger,
								messageTickerMessage));

					}

				}

				newGeneration =
					max (
						newGeneration,
						messageTickerMessage.messageGeneration (),
						messageTickerMessage.statusGeneration ());

			}

			// send update

			JsonObject updateData =
				new JsonObject ();

			updateData.add (
				"messages",
				messagesArray);

			updateData.add (
				"statuses",
				statusesArray);

			connectionHandle.send (
				taskLogger,
				updateData);

			// update state

			state.generation (
				newGeneration);

		}

	}

	private
	boolean messageIsVisible (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserPrivChecker privChecker,
			@NonNull MessageTickerMessage messageTickerMessage) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"messageIsVisible");

		) {

			return (

				privChecker.canRecursive (
					taskLogger,
					messageTickerMessage.routeGlobalId (),
					"messages")

				|| privChecker.canRecursive (
					taskLogger,
					messageTickerMessage.serviceParentGlobalId (),
					"messages")

				|| privChecker.canRecursive (
					taskLogger,
					messageTickerMessage.affiliateParentGlobalId (),
					"messages")

			);

		}

	}

	private
	JsonObject createMessage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull UserRec user,
			@NonNull MessageTickerMessage messageTickerMessage) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMessage");

		) {

			JsonObject messageObject =
				new JsonObject ();

			// message id

			messageObject.addProperty (
				"messageId",
				messageTickerMessage.messageId ());

			// row class

			if (
				enumEqualSafe (
					messageTickerMessage.direction (),
					MessageDirection.in)
			) {

				messageObject.addProperty (
					"rowClass",
					"message-in");

			} else if (
				moreThanZero (
					messageTickerMessage.charge ())
			) {

				messageObject.addProperty (
					"rowClass",
					"message-out-charge");

			} else {

				messageObject.addProperty (
					"rowClass",
					"message-out");

			}

			// numbers and colour

			String number =
				messageTickerMessage.direction () == MessageDirection.in
					? messageTickerMessage.numFrom ()
					: messageTickerMessage.numTo ();

			messageObject.addProperty (
				"number",
				number);

			messageObject.addProperty (
				"colour",
				htmlColourFromObject (
					number));

			messageObject.addProperty (
				"numberFrom",
				messageTickerMessage.numFrom ());

			messageObject.addProperty (
				"numberTo",
				messageTickerMessage.numTo ());

			// link

			messageObject.addProperty (
				"link",
				stringFormat (
					"/message",
					"/%u",
					integerToDecimalString (
						messageTickerMessage.messageId ()),
					"/message.summary"));

			// timestamp

			messageObject.addProperty (
				"timestamp",
				timeFormatter.timeString (
					timeFormatter.timezone (
						ifNull (
							user.getDefaultTimezone (),
							user.getSlice ().getDefaultTimezone (),
							wbsConfig.defaultTimezone ())),
					messageTickerMessage.createdTime ()));

			// message body

			messageObject.addProperty (
				"body",
				messageTickerMessage.text ());

			// media

			JsonArray mediaArray =
				new JsonArray ();

			for (
				Long mediaId
					: messageTickerMessage.mediaIds ()
			) {

				MediaRec media =
					mediaHelper.findRequired (
						mediaId);

				mediaArray.add (
					new JsonPrimitive (
						formatWriterConsumerToString (
							formatWriter ->
								mediaConsoleLogic.writeMediaThumb32OrText (
									taskLogger,
									formatWriter,
									media))));

			}

			messageObject.add (
				"media",
				mediaArray);

			// status

			messageObject.addProperty (
				"statusClass",
				messageConsoleLogic.classForMessageStatus (
					messageTickerMessage.status ()));

			messageObject.addProperty (
				"statusCharacter",
				Character.toString (
					messageConsoleLogic.charForMessageStatus (
						messageTickerMessage.status ())));

			// return

			return messageObject;

		}

	}

	private
	JsonObject createStatus (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageTickerMessage messageTickerMessage) {

		JsonObject statusObject =
			new JsonObject ();

		statusObject.addProperty (
			"messageId",
			messageTickerMessage.messageId ());

		statusObject.addProperty (
			"statusClass",
			messageConsoleLogic.classForMessageStatus (
				messageTickerMessage.status ()));

		statusObject.addProperty (
			"statusCharacter",
			Character.toString (
				messageConsoleLogic.charForMessageStatus (
					messageTickerMessage.status ())));

		// return

		return statusObject;

	}

	// subscriber state

	@Accessors (fluent = true)
	@Data
	public static
	class SubscriberState {
		Long generation;
	}

}
