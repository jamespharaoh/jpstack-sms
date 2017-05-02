package wbs.sms.route.core.console;

import static wbs.utils.collection.MapUtils.emptyMap;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			route =
				routeHelper.findFromContextRequired (
					transaction);

			if (route.getSender () != null) {

				summaryAdditionalPart =
					routeSummaryAdditionalPartManager.getPagePartBySenderCode (
						route.getSender ().getCode ());

			}

			if (summaryAdditionalPart != null) {

				summaryAdditionalPart.setup (
					transaction,
					emptyMap ());

				summaryAdditionalPart.prepare (
					transaction);

			}

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (summaryAdditionalPart != null) {

				summaryAdditionalPart.renderHtmlBodyContent (
					transaction);

			}

		}

	}

}