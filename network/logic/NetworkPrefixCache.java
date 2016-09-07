package wbs.sms.network.logic;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.laterThan;
import static wbs.framework.utils.etc.TimeUtils.millisToInstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkPrefixObjectHelper;
import wbs.sms.network.model.NetworkPrefixRec;
import wbs.sms.network.model.NetworkRec;

// TODO what to do with this :-(

@Log4j
@SingletonComponent ("networkPrefixCache")
public
class NetworkPrefixCache {

	// dependencies

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NetworkPrefixObjectHelper networkPrefixHelper;

	// properties

	@Getter @Setter
	int reloadSecs = 60;

	// state

	private
	Map<String,Long> entries;

	private
	Instant lastReload =
		millisToInstant (0);

	// implementation

	private synchronized
	void reloadEntries () {

		entries =
			new HashMap<> ();

		List<NetworkPrefixRec> list =
			networkPrefixHelper.findAll ();

		for (
			NetworkPrefixRec networkPrefix
				: list
		) {

			entries.put (
				networkPrefix.getPrefix (),
				networkPrefix.getNetwork ().getId ());

		}

	}

	private synchronized
	Map<String,Long> getEntries () {

		Instant now =
			Instant.now ();

		if (
			laterThan (
				now,
				lastReload.plus (
					reloadSecs * 1000))
		) {

			reloadEntries ();

			lastReload = now;

		}

		return entries;

	}

	public
	NetworkRec lookupNetwork (
			String number) {

		Map <String, Long> entries =
			getEntries ();

		for (

			String prefixToTry =
				number;

			prefixToTry.length () > 0;

			prefixToTry =
				prefixToTry.substring (
					0,
					prefixToTry.length () - 1)

		) {

			log.debug ("Trying " + prefixToTry + " for " + number);

			Long networkId =
				entries.get (
					prefixToTry);

			if (
				isNotNull (
					networkId)
			) {

				log.debug (
					stringFormat (
						"Found %s, networkId = %s for %s",
						prefixToTry,
						networkId,
						number));

				return networkHelper.findRequired (
					networkId);

			}

		}

		log.debug ("Found nothing for " + number);

		return null;

	}

}