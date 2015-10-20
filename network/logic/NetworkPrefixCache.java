package wbs.sms.network.logic;

import static wbs.framework.utils.etc.Misc.moreThan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkPrefixObjectHelper;
import wbs.sms.network.model.NetworkPrefixRec;
import wbs.sms.network.model.NetworkRec;

// TODO what to do with this :-(

@Log4j
@SingletonComponent ("networkPrefixCache")
public
class NetworkPrefixCache {

	@Inject
	NetworkObjectHelper networkHelper;

	@Inject
	NetworkPrefixObjectHelper networkPrefixHelper;

	@Getter @Setter
	int reloadSecs = 60;

	private
	Map<String, Integer> entries;

	private
	Instant lastReload =
		new Instant (0);

	private synchronized
	void reloadEntries () {

		entries =
			new HashMap<String,Integer> ();

		List<NetworkPrefixRec> list =
			networkPrefixHelper.findAll ();

		for (NetworkPrefixRec networkPrefix : list)
			entries.put (
				networkPrefix.getPrefix (),
				networkPrefix.getNetwork ().getId ());

	}

	private synchronized
	Map<String,Integer> getEntries () {

		Instant now =
			Instant.now ();

		if (
			moreThan (
				now.getMillis (),
				lastReload.getMillis () + reloadSecs * 1000)
		) {

			reloadEntries ();

			lastReload = now;

		}

		return entries;

	}

	public
	NetworkRec lookupNetwork (
			String number) {

		Map<String,Integer> entries =
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

			Integer networkId =
				entries.get (prefixToTry);

			if (networkId != null) {

				log.debug (
					"Found " + prefixToTry + ", networkId = " + networkId + " for " + number);

				return networkHelper.find (networkId);

			}

		}

		log.debug ("Found nothing for " + number);

		return null;

	}

}