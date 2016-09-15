package wbs.sms.message.core.console;

import static wbs.utils.collection.CollectionUtils.collectionIsNotEmpty;
import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlLinkWriteHtml;

import java.util.List;

import com.google.common.base.Optional;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.currency.logic.CurrencyLogic;
import wbs.platform.media.console.MediaConsoleLogic;
import wbs.platform.media.model.MediaRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.FailedMessageObjectHelper;
import wbs.sms.message.outbox.model.FailedMessageRec;

@PrototypeComponent ("messageSummaryPart")
public
class MessageSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	MediaConsoleLogic mediaConsoleLogic;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	FailedMessageObjectHelper failedMessageHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;
	Optional <FailedMessageRec> failedMessageOptional;
	MessageConsolePlugin plug;
	String summaryHtml;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		failedMessageOptional =
			failedMessageHelper.find (
				message.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		// open table

		htmlTableOpenDetails ();

		// write id

		htmlTableDetailsRowWrite (
			"ID",
			integerToDecimalString (
				message.getId ()));

		htmlTableDetailsRowWrite (
			"Thread ID",
			integerToDecimalString (
				message.getThreadId ()));

		htmlTableDetailsRowWrite (
			"Other ID",
			ifNull (
				message.getOtherId (),
				"—"));

		htmlTableDetailsRowWriteRaw (
			"Message",
			() -> messageConsoleLogic.writeMessageContentHtml (
				formatWriter,
				message));

		if (
			enumEqualSafe (
				message.getDirection (),
				MessageDirection.in)
		) {

			htmlTableDetailsRowWriteRaw (
				"Number from",
				() ->
					objectManager.writeTdForObjectMiniLink (
						message.getNumber ()));

			htmlTableDetailsRowWrite (
				"Number to",
				message.getNumTo ());

		} else {

			htmlTableDetailsRowWrite (
				"Number from",
				message.getNumFrom ());

			htmlTableDetailsRowWriteRaw (
				"Number to",
				() ->
					objectManager.writeTdForObjectMiniLink (
						message.getNumber ()));

		}

		htmlTableDetailsRowWrite (
			"Status",
			message.getStatus ().getDescription ());

		htmlTableDetailsRowWrite (
			"Direction",
			message.getDirection ().name ());

		htmlTableDetailsRowWriteRaw (
			"Route",
			() ->
				objectManager.writeTdForObjectMiniLink (
					message.getRoute ()));

		htmlTableDetailsRowWrite (
			"Network",
			message.getNetwork ().getDescription ());

		htmlTableDetailsRowWriteRaw (
			"Service",
			() ->
				objectManager.writeTdForObjectMiniLink (
					message.getService ()));

		htmlTableDetailsRowWriteRaw (
			"Affiliate",
			() -> 
				objectManager.writeTdForObjectMiniLink (
					message.getAffiliate ()));

		if (
			enumEqualSafe (
				message.getDirection (),
				MessageDirection.in)
		) {

			htmlTableDetailsRowWrite (
				"Time sent",
				ifNotNullThenElseEmDash (
					message.getNetworkTime (),
					() ->
						userConsoleLogic.timestampWithTimezoneString (
							message.getNetworkTime ())));

			htmlTableDetailsRowWrite (
				"Time received",
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()));

			htmlTableDetailsRowWrite (
				"Time processed",
				ifNotNullThenElseEmDash (
					message.getProcessedTime (),
					() ->
						userConsoleLogic.timestampWithTimezoneString (
							message.getProcessedTime ())));

			htmlTableDetailsRowWriteRaw (
				"Command",
				() ->
					objectManager.writeTdForObjectMiniLink (
						message.getCommand ()));

		} else {

			htmlTableDetailsRowWrite (
				"Time created",
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()));

			htmlTableDetailsRowWrite (
				"Time sent",
				ifNotNullThenElseEmDash (
					message.getProcessedTime (),
					() ->
						userConsoleLogic.timestampWithTimezoneString (
							message.getProcessedTime ())));

			htmlTableDetailsRowWrite (
				"Time received",
				ifNotNullThenElseEmDash (
					message.getNetworkTime (),
					() ->
						userConsoleLogic.timestampWithTimezoneString (
							message.getNetworkTime ())));

		}

		htmlTableDetailsRowWriteRaw (
			"Charge",
			currencyLogic.formatHtmlTd (
				message.getRoute ().getCurrency (),
				message.getCharge ()));

		List <MediaRec> medias =
			message.getMedias ();

		if (
			collectionIsNotEmpty (
				medias)
		) {

			htmlTableDetailsRowWriteRaw (
				"Media",
				() -> {

				formatWriter.writeLineFormat (
					"<td>");

				formatWriter.increaseIndent ();

				for (
					int index = 0;
					index < medias.size ();
					index ++
				) {

					MediaRec media =
						medias.get (index);

					htmlLinkWriteHtml (
						requestContext.resolveContextUrl (
							stringFormat (
								"/message_media",
								"/%d",
								message.getId (),
								"/%d",
								index,
								"/message_media_summary")),
						() -> mediaConsoleLogic.writeMediaThumb100 (
							media));

				}

				formatWriter.decreaseIndent ();

				formatWriter.writeLineFormat (
					"</td>");

			});

		}

		htmlTableDetailsRowWrite (
			"Tags",
			joinWithCommaAndSpace (
				message.getTags ()));

		if (
			optionalIsPresent (
				failedMessageOptional)
		) {

			htmlTableDetailsRowWrite (
				"Failure reason",
				failedMessageOptional.get ().getError ());

		}

		htmlTableDetailsRowWriteRaw (
			"User",
			() ->
				objectManager.writeTdForObjectMiniLink (
					message.getUser ()));

		htmlTableDetailsRowWriteRaw (
			"Delivery type",
			() ->
				objectManager.writeTdForObjectMiniLink (
					message.getDeliveryType ()));

		htmlTableClose ();

	}

}
