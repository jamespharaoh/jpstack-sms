package wbs.sms.number.core.console;

import static wbs.utils.collection.IterableUtils.iterableFilter;
import static wbs.utils.collection.MapUtils.mapIsEmpty;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.List;
import java.util.Map;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.Record;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@PrototypeComponent ("numberServicesPart")
public
class NumberServicesPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	// properties

	Map <String, ServiceRec> services;

	@Override
	public
	void prepare () {

		NumberRec number =
			numberHelper.findRequired (
				requestContext.stuffInteger (
					"numberId"));

		List <ServiceRec> allServices =
			messageHelper.projectServices (
				number);

		services =
			mapWithDerivedKey (
				iterableFilter (
					objectManager::canView,
					allServices),
				objectManager::objectPathMini);

	}

	@Override
	public
	void renderHtmlBodyContent () {

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
				objectManager.getParent (
					service);

			// open table row

			formatWriter.writeLineFormat (
				"<tr>");

			formatWriter.increaseIndent ();

			// write parent table cell

			objectManager.writeTdForObjectLink (
				formatWriter,
				parent);

			// write service table cell

			objectManager.writeTdForObjectMiniLink (
				formatWriter,
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
