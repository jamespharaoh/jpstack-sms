package wbs.sms.network.logic;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.string.StringUtils.stringFormatObsolete;
import static wbs.utils.time.TimeUtils.laterThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkPrefixObjectHelper;
import wbs.sms.network.model.NetworkPrefixRec;
import wbs.sms.network.model.NetworkRec;

// TODO what to do with this :-(

@Log4j
@SingletonComponent ("networkPrefixCache")
public
class NetworkPrefixCache {

	// singleton dependencies

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	NetworkPrefixObjectHelper networkPrefixHelper;

	// properties

	@Getter @Setter
	int reloadSecs = 60;

	// state

	private
	Map <String, Long> entries;

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
					stringFormatObsolete (
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