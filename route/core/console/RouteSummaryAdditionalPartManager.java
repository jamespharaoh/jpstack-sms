package wbs.sms.route.core.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.extern.log4j.Log4j;

import wbs.console.part.PagePart;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@Log4j
@SingletonComponent ("routeSummaryAdditionalPartManager")
public
class RouteSummaryAdditionalPartManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, RouteSummaryAdditionalPartFactory> factories;

	// state

	Map <String, RouteSummaryAdditionalPartFactory> factoriesBySenderCode;

	// life cycle

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		log.debug (
			stringFormat (
				"searching for factories"));

		ImmutableMap.Builder <String, RouteSummaryAdditionalPartFactory>
		factoriesBySenderCodeBuilder =
			ImmutableMap.builder ();

		for (
			Map.Entry <String, RouteSummaryAdditionalPartFactory> entry
				: factories.entrySet ()
		) {

			String factoryName =
				entry.getKey ();

			RouteSummaryAdditionalPartFactory factory =
				entry.getValue ();

			log.debug (
				stringFormat (
					"got factory \"%s\"",
					factoryName));

			for (
				String senderCode
					: factory.getSenderCodes ()
			) {

				log.debug (
					stringFormat (
						"sender code \"%s\"",
						senderCode));

				factoriesBySenderCodeBuilder.put (
					senderCode,
					factory);

			}

		}

		factoriesBySenderCode =
			factoriesBySenderCodeBuilder.build ();

		log.debug (
			stringFormat (
				"done"));

	}

	public
	PagePart getPagePartBySenderCode (
			String senderCode) {

		RouteSummaryAdditionalPartFactory factory =
			factoriesBySenderCode.get (senderCode);

		if (factory == null)
			return null;

		return factory.getPagePart (
			senderCode);

	}

}
