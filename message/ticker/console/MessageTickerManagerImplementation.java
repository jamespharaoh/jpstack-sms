package wbs.sms.message.ticker.console;

import static wbs.utils.collection.MapUtils.emptyMap;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.platform.media.model.MediaRec;

import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("messageTickerManager")
public
class MessageTickerManagerImplementation
	implements MessageTickerManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	ObjectManager objectManager;

	// properties

	@Getter @Setter
	Duration updateDuration =
		Duration.millis (
			500);

	// state

	Long generation = 0l;

	Instant lastUpdate =
		millisToInstant (0);

	Long generations = 2000l;

	private
	Map <Long, MessageTickerMessage> messageTickerMessages =
		emptyMap ();

	// implementation

	private synchronized
	void update (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"update");

		) {

			// don't run too often

			Instant now =
				Instant.now ();

			if (
				earlierThan (
					now,
					lastUpdate.plus (
						updateDuration))
			) {
				return;
			}

			lastUpdate = now;

			// get a generation number

			generation ++;

			// update the info

			List <MessageRec> recentMessages =
				messageHelper.findRecentLimit (
					transaction,
					generations);

			Collections.sort (
				recentMessages,
				Ordering.natural ().reverse ());

			ImmutableMap.Builder <Long, MessageTickerMessage>
				messageTickerMessagesBuilder =
					ImmutableMap.builder ();

			for (
				MessageRec message
					: recentMessages
			) {

				MessageTickerMessage messageTickerMessage =
					messageTickerMessages.get (
						message.getId ());

				if (messageTickerMessage != null) {

					if (messageTickerMessage.status != message.getStatus ()) {

						messageTickerMessage =
							new MessageTickerMessage (
								messageTickerMessage);

						messageTickerMessage.status =
							message.getStatus ();

						messageTickerMessage.statusGeneration =
							generation;

					}

				} else {

					messageTickerMessage =
						new MessageTickerMessage ();

					messageTickerMessage.messageGeneration =
						generation;

					messageTickerMessage.statusGeneration =
						generation;

					messageTickerMessage.messageId =
						message.getId ();

					messageTickerMessage.routeGlobalId =
						objectManager.getGlobalId (
							transaction,
							message.getRoute ());

					messageTickerMessage.serviceParentGlobalId =
						objectManager.getParentGlobalId (
							transaction,
							message.getService ());

					messageTickerMessage.affiliateParentGlobalId =
						objectManager.getParentGlobalId (
							transaction,
							message.getAffiliate ());

					messageTickerMessage.createdTime =
						message.getCreatedTime ();

					messageTickerMessage.numFrom =
						message.getNumFrom ();

					messageTickerMessage.numTo =
						message.getNumTo ();

					// TODO this encoding looks like it's in the wrong layer

					messageTickerMessage.text =
						spacify (
							formatWriterConsumerToString (
								"  ",
								formatWriter ->

						messageConsoleLogic.writeMessageContentText (
							transaction,
							formatWriter,
							message)

					));

					messageTickerMessage.direction =
						message.getDirection ();

					messageTickerMessage.status =
						message.getStatus ();

					messageTickerMessage.charge =
						message.getCharge ();

					for (
						MediaRec media
							: message.getMedias ()
					) {

						messageTickerMessage.mediaIds.add (
							media.getId ());

					}

				}

				messageTickerMessagesBuilder.put (
					message.getId (),
					messageTickerMessage);

			}

			messageTickerMessages =
				messageTickerMessagesBuilder.build ();

		}

	}

	@Override
	public
	synchronized Collection <MessageTickerMessage> getMessages (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getMessages");

		) {

			update (
				transaction);

			return messageTickerMessages.values ();

		}

	}

}
