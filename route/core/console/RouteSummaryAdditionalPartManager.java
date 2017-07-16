package wbs.sms.route.core.console;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("routeSummaryAdditionalPartManager")
public
class RouteSummaryAdditionalPartManager {

	// singleton dependencies

	@SingletonDependency
	Map <String, RouteSummaryAdditionalPartFactory> factories;

	@ClassSingletonDependency
	LogContext logContext;

	// state

	Map <String, RouteSummaryAdditionalPartFactory> factoriesBySenderCode;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

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

				taskLogger.debugFormat (
					"got factory \"%s\"",
					factoryName);

				for (
					String senderCode
						: factory.getSenderCodes ()
				) {

					taskLogger.debugFormat (
						"sender code \"%s\"",
						senderCode);

					factoriesBySenderCodeBuilder.put (
						senderCode,
						factory);

				}

			}

			factoriesBySenderCode =
				factoriesBySenderCodeBuilder.build ();

		}

	}

	public
	PagePart getPagePartBySenderCode (
			@NonNull Transaction parentTransaction,
			@NonNull String senderCode) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"getPagePartBySenderCode");

		) {

			RouteSummaryAdditionalPartFactory factory =
				factoriesBySenderCode.get (
					senderCode);

			if (factory == null)
				return null;

			return factory.getPagePart (
				transaction,
				senderCode);

		}

	}

}
