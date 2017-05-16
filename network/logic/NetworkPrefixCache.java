package wbs.sms.network.logic;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.time.TimeUtils.laterThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkPrefixObjectHelper;
import wbs.sms.network.model.NetworkPrefixRec;
import wbs.sms.network.model.NetworkRec;

// TODO what to do with this :-(

@SingletonComponent ("networkPrefixCache")
public
class NetworkPrefixCache {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	void reloadEntries (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"reloadEntries");

		) {

			entries =
				new HashMap<> ();

			List <NetworkPrefixRec> list =
				networkPrefixHelper.findAll (
					transaction);

			for (
				NetworkPrefixRec networkPrefix
					: list
			) {

				entries.put (
					networkPrefix.getPrefix (),
					networkPrefix.getNetwork ().getId ());

			}

		}

	}

	private synchronized
	Map <String, Long> getEntries (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getEntries");

		) {

			if (
				laterThan (
					transaction.now (),
					lastReload.plus (
						reloadSecs * 1000))
			) {

				reloadEntries (
					transaction);

				lastReload =
					transaction.now ();

			}

			return entries;

		}

	}

	public
	NetworkRec lookupNetwork (
			@NonNull Transaction parentTransaction,
			@NonNull String number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupNetwork");

		) {

			Map <String, Long> entries =
				getEntries (
					transaction);

			for (

				String prefixToTry =
					number;

				prefixToTry.length () > 0;

				prefixToTry =
					prefixToTry.substring (
						0,
						prefixToTry.length () - 1)

			) {

				transaction.debugFormat (
					"Trying %s for %s",
					prefixToTry,
					number);

				Long networkId =
					entries.get (
						prefixToTry);

				if (
					isNotNull (
						networkId)
				) {

					transaction.debugFormat (
						"Found %s, networkId = %s for %s",
						prefixToTry,
						integerToDecimalString (
							networkId),
						number);

					return networkHelper.findRequired (
						transaction,
						networkId);

				}

			}

			transaction.debugFormat (
				"Found nothing for %s",
				number);

			return null;

		}

	}

}