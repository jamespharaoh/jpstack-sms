package wbs.sms.route.core.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.log4j.Log4j;

import wbs.console.part.PagePart;
import wbs.framework.component.annotations.SingletonComponent;

@Log4j
@SingletonComponent ("routeSummaryAdditionalPartManager")
public
class RouteSummaryAdditionalPartManager {

	@Inject
	Map<String,RouteSummaryAdditionalPartFactory> factories =
		Collections.emptyMap ();

	Map<String,RouteSummaryAdditionalPartFactory> factoriesBySenderCode =
		new LinkedHashMap<String,RouteSummaryAdditionalPartFactory> ();

	@PostConstruct
	public
	void afterPropertiesSet () {

		log.debug (
			stringFormat (
				"searching for factories"));

		factoriesBySenderCode =
			new HashMap<String,RouteSummaryAdditionalPartFactory> ();

		for (Map.Entry<String,RouteSummaryAdditionalPartFactory> entry
				: factories.entrySet ()) {

			String factoryName =
				entry.getKey ();

			RouteSummaryAdditionalPartFactory factory =
				entry.getValue ();

			log.debug (
				stringFormat (
					"got factory \"%s\"",
					factoryName));

			for (String senderCode
					: factory.getSenderCodes ()) {

				log.debug (
					stringFormat (
						"sender code \"%s\"",
						senderCode));

				factoriesBySenderCode.put (
					senderCode,
					factory);

			}

		}

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
