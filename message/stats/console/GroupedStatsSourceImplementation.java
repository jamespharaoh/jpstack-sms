package wbs.sms.message.stats.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;

import wbs.sms.message.stats.logic.MessageStatsLogic;
import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.misc.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("groupedStatsSource")
public
class GroupedStatsSourceImplementation
	implements GroupedStatsSource {

	// singleton dependencies

	@SingletonDependency
	MessageStatsLogic messageStatsLogic;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// properties

	@Getter @Setter
	SmsStatsCriteria groupCriteria;

	@Getter @Setter
	SmsStatsSource statsSource;

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Long>> critMap;

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Long>> filterMap;

	@Getter @Setter
	String url;

	@Getter @Setter
	UrlParams urlParams;

	// implementation

	@Override
	public
	Map <String, GroupStats> load (
			@NonNull SmsStatsTimeScheme timeScheme,
			@NonNull LocalDate start,
			@NonNull LocalDate end) {

		Map <String, GroupStats> ret =
			new TreeMap<> ();

		Optional <RouteRec> route =
			statsSource.findRoute ();

		if (

			optionalIsPresent (
				route)

			&& critMap.containsKey (
				SmsStatsCriteria.route)

		) {

			Set <Long> routeIds =
				critMap.get (
					SmsStatsCriteria.route);

			if (routeIds.size () == 1) {

				route =
					optionalOf (
						routeHelper.findRequired (
							routeIds.iterator ().next ()));

			}

		}

		List<MessageStatsRec> allMessageStats =
			statsSource.findMessageStats (
				start,
				end,
				timeScheme,
				Optional.fromNullable (
					groupCriteria),
				critMap,
				Optional.fromNullable (
					filterMap));

		for (
			MessageStatsRec messageStats
				: allMessageStats
		) {

			String groupName =
				groupName (
					messageStats);

			if (groupCriteria == SmsStatsCriteria.route) {

				route =
					optionalOf (
						messageStats.getMessageStatsId ().getRoute ());

			}

			GroupStats groupStats =
				ret.get (groupName);

			if (groupStats == null) {

				groupStats =
					new GroupStats (
						route,
						groupUrl (
							messageStats));

				ret.put (
					groupName,
					groupStats);

			}

			Map<LocalDate,MessageStatsData> statsByDate =
				groupStats.getStatsByDate ();

			LocalDate date =
				messageStats.getMessageStatsId ().getDate ();

			MessageStatsData stats =
				statsByDate.get (date);

			if (stats == null) {

				stats =
					new MessageStatsData ();

				statsByDate.put (
					date,
					stats);

			}

			messageStatsLogic.addTo (
				stats,
				messageStats.getStats ());

		}

		return ret;

	}

	String groupName (
			MessageStatsRec mse) {

		if (groupCriteria == null)
			return "Total";

		switch (groupCriteria) {

			case route:

				return objectName (
					mse.getMessageStatsId ().getRoute ());

			case service:

				return objectName (
					mse.getMessageStatsId ().getService ());

			case affiliate:

				return objectName (
					mse.getMessageStatsId ().getAffiliate ());

			case batch:

				return mse.getMessageStatsId ().getBatch ().getId ().toString ();

			case network:

				return mse.getMessageStatsId ().getNetwork ().getDescription ();

		}

		throw new IllegalArgumentException ();

	}

	Optional <String> groupUrl (
			MessageStatsRec messageStats) {

		if (groupCriteria == null) {
			return optionalAbsent ();
		}

		UrlParams myUrlParams =
			new UrlParams (
				urlParams);

		myUrlParams.set (
			groupCriteria.toString (),
			integerToDecimalString (
				groupId (
					messageStats)));

		return optionalOf (
			myUrlParams.toUrl (
				url));

	}

	Long groupId (
			MessageStatsRec mse) {

		if (groupCriteria == null)
			return null;

		switch (groupCriteria) {

			case route:

				return mse.getMessageStatsId ().getRoute ().getId ();

			case service:

				return mse.getMessageStatsId ().getService ().getId ();

			case affiliate:

				return mse.getMessageStatsId ().getAffiliate ().getId ();

			case batch:

				return mse.getMessageStatsId ().getBatch ().getId ();

			case network:

				return mse.getMessageStatsId ().getNetwork ().getId ();

		}

		throw new IllegalArgumentException ();

	}

	String objectName (
			Record<?> object) {

		return objectManager.objectPathMini (
			object);

	}

}
