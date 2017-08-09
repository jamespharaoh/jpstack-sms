package wbs.sms.number.core.console;

import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.number.core.model.NumberRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("numberServicesPart")
public
class NumberServicesPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberConsoleHelper numberHelper;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	Map <String, ServiceRec> services;

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

			NumberRec number =
				numberHelper.findFromContextRequired (
					transaction);

			List <ServiceRec> allServices =
				messageHelper.projectServices (
					transaction,
					number);

			services =
				mapWithDerivedKey (
					iterableFilter (
						allServices,
						service ->
							objectManager.canView (
								transaction,
								privChecker,
								service)),
					service ->
						objectManager.objectPathMini (
							transaction,
							service));

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			// open table

			formatWriter.writeLineFormat (
				"<table class=\"list\">\n");

			formatWriter.increaseIndent ();

			// write header

			formatWriter.writeLineFormat (
				"<tr>");

			formatWriter.increaseIndent ();

			formatWriter.writeLineFormat (
				"<th>Subject</th>");

			formatWriter.writeLineFormat (
				"<th>Service</th>");

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"</tr>");

			// write empty contents

			if (
				mapIsEmpty (
					services)
			) {

				formatWriter.writeLineFormat (
					"<tr>");

				formatWriter.increaseIndent ();

				formatWriter.writeLineFormat (
					"<td colspan=\"2\">Nothing to show</td>");

				formatWriter.decreaseIndent ();

				formatWriter.writeLineFormat (
					"</tr>");

			}

			// write table contents

			for (
				ServiceRec service
					: services.values ()
			) {

				Record <?> parent =
					objectManager.getParentRequired (
						transaction,
						service);

				// open table row

				formatWriter.writeLineFormat (
					"<tr>");

				formatWriter.increaseIndent ();

				// write parent table cell

				objectManager.writeTdForObjectLink (
					transaction,
					formatWriter,
					privChecker,
					parent);

				// write service table cell

				objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					privChecker,
					service,
					parent);

				// close table row

				formatWriter.decreaseIndent ();

				formatWriter.writeLineFormat (
					"</tr>");

			}

			// close table

			formatWriter.decreaseIndent ();

			formatWriter.writeLineFormat (
				"</table>");

		}

	}

}
