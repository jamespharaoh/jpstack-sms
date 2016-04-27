package wbs.sms.number.core.console;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.Record;
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
	UserPrivChecker privChecker;

	Map<String,ServiceRec> services =
		new TreeMap<String,ServiceRec> ();

	@Override
	public
	void prepare () {

		NumberRec number =
			numberHelper.find (
				requestContext.stuffInt (
					"numberId"));

		List<ServiceRec> allServices =
			messageHelper.projectServices (
				number);

		for (
			ServiceRec service
				: allServices
		) {

			if (! objectManager.canView (service))
				continue;

			services.put (
				objectManager.objectPathMini (
					service),
				service);

		}

	}

	@Override
	public
	void renderHtmlBodyContent () {

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

		for (
			ServiceRec service
				: services.values ()
		) {

			Record<?> parent =
				objectManager.getParent (
					service);

			printFormat (
				"<tr>\n",

				"%s\n",
				objectManager.tdForObjectLink (
					parent),

				"%s\n",
				objectManager.tdForObjectMiniLink (
					service,
					parent),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
