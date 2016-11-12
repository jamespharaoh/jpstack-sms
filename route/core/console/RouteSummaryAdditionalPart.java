package wbs.sms.route.core.console;

import java.util.Collections;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeSummaryAdditionalPart")
public
class RouteSummaryAdditionalPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	RouteSummaryAdditionalPartManager routeSummaryAdditionalPartManager;

	// state

	RouteRec route;

	PagePart summaryAdditionalPart;

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

		if (route.getSender () != null) {

			summaryAdditionalPart =
				routeSummaryAdditionalPartManager.getPagePartBySenderCode (
					route.getSender ().getCode ());

		}

		if (summaryAdditionalPart != null) {

			summaryAdditionalPart.setup (
				Collections.emptyMap ());

			summaryAdditionalPart.prepare (
				taskLogger);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		if (summaryAdditionalPart != null) {

			summaryAdditionalPart.renderHtmlBodyContent (
				taskLogger);

		}

	}

}