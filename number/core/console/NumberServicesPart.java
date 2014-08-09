package wbs.sms.number.core.console;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@PrototypeComponent ("numberServicesPart")
public
class NumberServicesPart
	extends AbstractPagePart {

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	PrivChecker privChecker;

	Map<String,ServiceRec> services =
		new TreeMap<String,ServiceRec>();

	@Override
	public
	void prepare () {

		NumberRec number =
			numberHelper.find (
				requestContext.stuffInt ("numberId"));

		List<ServiceRec> allServices =
			messageHelper.projectServices (
				number);

		for (ServiceRec service : allServices) {

			if (! objectManager.canView (service))
				continue;

			services.put (
				objectManager.objectPath (
					service,
					null,
					true,
					false),
				service);

		}

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Subject</th>\n",
			"<th>Service</th>\n",
			"</tr>\n");

		if (services.size () == 0) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"2\">Nothing to show</td>\n",
				"</tr>\n");

		}

		for (ServiceRec service
				: services.values ()) {

			Record<?> parent =
				objectManager.getParent (
					service);

			printFormat (
				"<tr>\n",

				"%s\n",
				objectManager.tdForObject (
					parent,
					null,
					false,
					true),

				"%s\n",
				objectManager.tdForObject (
					service,
					parent,
					true,
					true),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
