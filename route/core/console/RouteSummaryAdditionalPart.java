package wbs.sms.route.core.console;

import java.util.Collections;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.part.PagePart;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeSummaryAdditionalPart")
public
class RouteSummaryAdditionalPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	RouteSummaryAdditionalPartManager routeSummaryAdditionalPartManager;

	// state

	RouteRec route;

	PagePart summaryAdditionalPart;

	@Override
	public
	void prepare () {

		route =
			routeHelper.find (
				requestContext.stuffInt ("routeId"));

		if (route.getSender () != null) {

			summaryAdditionalPart =
				routeSummaryAdditionalPartManager.getPagePartBySenderCode (
					route.getSender ().getCode ());

		}

		if (summaryAdditionalPart != null) {

			summaryAdditionalPart.setup (
				Collections.<String,Object>emptyMap ());

			summaryAdditionalPart.prepare ();

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (summaryAdditionalPart != null) {

			summaryAdditionalPart.renderHtmlBodyContent ();

		}

	}

}