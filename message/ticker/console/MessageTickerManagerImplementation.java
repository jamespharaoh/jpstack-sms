package wbs.sms.message.ticker.console;

import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.spacify;
import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	@SingletonDependency
	ObjectManager objectManager;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	// properties

	@Getter @Setter
	Duration updateDuration =
		Duration.millis (
			1000);

	// state

	Long generation = 0l;

	Instant lastUpdate =
		millisToInstant (0);

	Long generations = 1000l;

	private
	Map <Long, MessageTickerMessage> messageTickerMessages =
		new TreeMap<> ();

	// implementation

	private synchronized
	void update () {

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

		List <MessageRec> newMessages =
			messageHelper.findRecentLimit (
				1000l);

		Map <Long,MessageTickerMessage> newMessageTickerMessages =
			new TreeMap<> ();

		for (
			MessageRec message
				: newMessages
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
						message.getRoute ());

				messageTickerMessage.serviceParentGlobalId =
					objectManager.getParentGlobalId (
						message.getService ());

				messageTickerMessage.affiliateParentGlobalId =
					objectManager.getParentGlobalId (
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
							formatWriter ->

					messageConsoleLogic.writeMessageContentText (
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

			newMessageTickerMessages.put (
				message.getId (),
				messageTickerMessage);

		}

		messageTickerMessages =
			newMessageTickerMessages;

	}

	@Override
	public
	synchronized Collection<MessageTickerMessage> getMessages () {

		update ();

		return messageTickerMessages.values ();

	}

}
