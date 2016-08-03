package wbs.sms.message.ticker.console;

import static wbs.framework.utils.etc.StringUtils.spacify;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;
import static wbs.framework.utils.etc.TimeUtils.millisToInstant;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.platform.media.model.MediaRec;
import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@SingletonComponent ("messageTickerManager")
public
class MessageTickerManagerImplementation
	implements MessageTickerManager {

	// dependencies

	@Inject
	ObjectManager objectManager;

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageObjectHelper messageHelper;

	// properties

	@Getter @Setter
	int updateTimeMs = 1000;

	// state

	int generation = 0;

	Instant lastUpdate =
		millisToInstant (0);

	int generations = 1000;

	private
	Map<Integer,MessageTickerMessage> messageTickerMessages =
		new TreeMap<Integer,MessageTickerMessage> ();

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
					updateTimeMs))
		) {
			return;
		}

		lastUpdate = now;

		// get a generation number

		generation ++;

		// update the info

		List<MessageRec> newMessages =
			messageHelper.findRecentLimit (
				1000);

		Map<Integer,MessageTickerMessage> newMessageTickerMessages =
			new TreeMap<Integer,MessageTickerMessage> ();

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
						messageConsoleLogic.messageContentText (
							message));

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
