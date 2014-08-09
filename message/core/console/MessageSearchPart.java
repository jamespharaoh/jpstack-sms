package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.emptyStringIfNull;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.toStringNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.affiliate.model.AffiliateObjectHelper;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.html.ObsoleteDateField;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.console.NetworkConsoleHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@PrototypeComponent ("messageSearchPart")
public
class MessageSearchPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	AffiliateObjectHelper affiliateHelper;

	@Inject
	NetworkConsoleHelper networkHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	PrivChecker privChecker;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	ServiceObjectHelper serviceHelper;

	@Inject
	UserObjectHelper userHelper;

	// state

	Map<String,ServiceRec> services =
		new TreeMap<String,ServiceRec> ();

	Map<String,UserRec> users =
		new TreeMap<String,UserRec> ();

	Map<String,AffiliateRec> affiliates =
		new TreeMap<String,AffiliateRec> ();

	Map<String,RouteRec> routes =
		new TreeMap<String,RouteRec> ();

	Map<String,String> networks =
		new TreeMap<String,String> ();

	// implementation

	@Override
	public
	void prepare () {

		for (ServiceRec service
				: serviceHelper.findAll ()) {

			if (objectManager.canView (service)) {

				services.put (
					objectManager.objectPath (
						service,
						null,
						true,
						true),
					service);

			}

		}

		for (UserRec user
				: userHelper.findAll ()) {

			if (! objectManager.canView (user))
				continue;

			users.put (
				objectManager.objectPath (
					user,
					null,
					true,
					true),
				user);

		}

		for (AffiliateRec affiliate
				: affiliateHelper.findAll ()) {

			if (! objectManager.canView (affiliate))
				continue;

			affiliates.put (
				objectManager.objectPath (
					affiliate,
					null,
					true,
					true),
				affiliate);

		}

		for (RouteRec route
				: routeHelper.findAll ()) {

			if (! objectManager.canView (route))
				continue;

			routes.put (
				objectManager.objectPath (
					route,
					null,
					true,
					true),
				route);

		}

		networks.put ("", "");

		for (NetworkRec network
				: new TreeSet<NetworkRec> (
					networkHelper.findAll ())) {

			networks.put (
				network.getId ().toString (),
				network.getDescription ());

		}

	}

	@Override
	public
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script type=\"text/javascript\">\n");

		printFormat (
			"var inputIds = [ %s ];\n",
			joinWithSeparator (
				", ",
				"'",
				ImmutableList.<String>of (
					"id",
					"number",
					"routeId",
					"status",
					"direction",
					"date",
					"timeFrom",
					"timeTo",
					"message",
					"userId"),
				"'"));

		printFormat (
			"function clearForm () {\n",
			"  for (var i = 0; i < inputIds.length; i++) {\n",
			"    try {\n",
			"      var input = document.getElementById (inputIds[i]);\n",
			"      input.value = '';\n",
			"    } catch (e) { }\n",
			"  }\n",
			"}\n");

		printFormat (
			"function doToday () {\n",
			"  document.getElementById ('date').value = '%j';\n",
			ObsoleteDateField.format (
				new Date ()),
			"  document.getElementById ('timeFrom').value = '';\n",
			"  document.getElementById ('timeTo').value = '';\n",
			"}\n");

		Calendar sevenDaysAgoCalendar =
			Calendar.getInstance ();

		sevenDaysAgoCalendar.add (
			Calendar.DATE,
			-7);

		printFormat (
			"function do7Days () {\n",
			"  document.getElementById ('date').value = '';\n",
			"  document.getElementById ('timeFrom').value = '%j';\n",
			ObsoleteDateField.format (
				sevenDaysAgoCalendar.getTime ()),
			"  document.getElementById ('timeTo').value = '';\n",
			"}\n");

		Calendar oneMonthAgoCalendar =
			Calendar.getInstance ();

		oneMonthAgoCalendar.add (
			Calendar.MONTH,
			-1);

		printFormat (
			"function do1Month () {\n",
			"  document.getElementById ('date').value = '';\n",
			"  document.getElementById ('timeFrom').value = '%j';\n",
			ObsoleteDateField.format (
				sevenDaysAgoCalendar.getTime ()),
			"  document.getElementById ('timeTo').value = '';\n",
			"}\n");

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/message.search"),
			" method=\"post\"",
			" id=\"form\"",
			">\n");

		printFormat (
			"<table\n",
			" border=\"0\"",
			" cellspacing=\"0\"",
			"><tr><td>\n");

		printFormat (
			"<p>By message id<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"id\"",
			" name=\"id\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"messageSearchId")),
			"\"></p>\n");

		printFormat (
			"<p>By number<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"number\"",
			" name=\"number\"",
			" size=\"32\"",
			" value=\"%h\"",
			emptyStringIfNull (
				(String)
				requestContext.session (
					"messageSearchNumber")),
			"></p>\n");

		printFormat (
			"<p>By route<br>\n",

			"<select",
			" id=\"routeId\"",
			" name=\"routeId\"",
			">\n",

			"<option>\n");

		Integer routeId =
		(Integer)
			requestContext.session ("messageSearchRouteId");

		for (Map.Entry<String,RouteRec> entry
				: routes.entrySet ()) {

			printFormat (
				"<option",

				" value=\"%h\"",
				entry.getValue ().getId (),

				"%s",
				equal (
						entry.getValue ().getId (),
						routeId)
					? " selected"
					: "",

				">%h</option>",
				entry.getKey ());

		}

		printFormat (
			"</select></p>\n");

		printFormat (
			"<p>By status</p>\n");

		printFormat (
			"<select id=\"status\" name=\"status\">\n",
			"<option>\n");

		Integer statusInt = (Integer)
			requestContext.session ("messageSearchStatus");

		for (MessageStatus status
				: MessageStatus.values ()) {

			printFormat (
				"<option",

				" value=\"%h\"",
				status.getOrdinal (),

				statusInt != null
						&& status.getOrdinal () == statusInt
					? " selected"
					: "",

				">%h</option>\n",
				status.getDescription ());

		}

		printFormat (
			"</select></p>\n");

		printFormat (
			"<p>By direction<br>\n",
			"%s</p>\n",
			Html.select (
				"direction",
				messageDirectionOptions,
				toStringNull (
					requestContext.session (
						"messageSearchDirection"))));

		// by user

		printFormat (
			"<p>By user<br>\n",

			"<select id=\"userId\" name=\"userId\">\n",

			"<option>\n");

		Integer userId = (Integer)
			requestContext.session ("messageSearchUserId");

		for (Map.Entry<String,UserRec> entry
				: users.entrySet ()) {

			printFormat (
				"<option value=\"%h\"",
				entry.getValue ().getId (),

				equal (
						userId,
						entry.getValue ().getId ())
					? " selected"
					: "",

				">%h</option>\n",
				entry.getKey ());

		}

		printFormat (
			"</select></p>\n");

		// next column

		printFormat (
			"</td>\n",
			"<td width=\"10\">&nbsp;</td>\n",
			"<td>\n");

		// by date/time

		printFormat (
			"<p>By date/time<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"date\"",
			" name=\"date\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				requestContext.session ("messageSearchDate"),
				""),
			">\n",

			"<input\n",
			" type=\"button\"",
			" value=\"today\"",
			" onclick=\"doToday ();\"",
			"></p>\n");

		printFormat (
			"<p>From date/time<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"timeFrom\"",
			" name=\"timeFrom\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (
				requestContext.session (
					"messageSearchTimeAfter"),
				""),
			">\n",

			"<input",
			" type=\"button\"",
			" value=\"7 days\"",
			" onclick=\"do7Days ();\"",
			">\n",

			"<input",
			" type=\"button\"",
			" value=\"1 month\"",
			" onclick=\"do1Month ();\"",
			"></p>\n");

		printFormat (
			"<p>To date/time<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"timeTo\"",
			" name=\"timeTo\"",
			" size=\"32\"",
			" value=\"%h\"",
			ifNull (requestContext.session ("messageSearchTimeBefore")),
			"></p>\n");

		printFormat (
			"<p>Message text<br>\n",

			"<input",
			" type=\"text\"",
			" id=\"message\"",
			" name=\"message\"",
			" size=\"32\"",
			requestContext.session ("messageSearchMessage") != null
				? stringFormat (
					" value=\"%h\"",
					requestContext.session ("messageSearchMessage"))
				: "",
			"></p>\n");

		printFormat (
			"<p>By network<br>\n",
			"%s</p>\n",
			Html.select (
				"networkId",
				networks,
				toStringNull (
					requestContext.session (
						"messageSearchNetworkId"))));

		printFormat (
			"</td>\n",
			"</tr>\n",
			"</table>\n");

		// service id

		printFormat (
			"<p>By service<br>\n",
			"<select id=\"serviceId\" name=\"serviceId\">\n",
			"<option>\n");

		Integer serviceId =
			(Integer)
			requestContext.session ("messageSearchServiceId");

		for (Map.Entry<String,ServiceRec> serviceEntry
				: services.entrySet ()) {

			printFormat (
				"<option value=\"%h\"",
				serviceEntry.getValue ().getId ());

			if (equal (serviceId, serviceEntry.getValue ().getId ()))
				printFormat (
					" selected");

			printFormat (
				">%h</option>\n",
				serviceEntry.getKey ());

		}

		printFormat (
			"</select></p>\n");

		// affiliate

		printFormat (
			"<p>By affiliate<br>",

			"<select",
			" id=\"affiliateId\"",
			" name=\"affiliateId\"",
			">\n",

			"<option>");

		Integer affiliateId =
			(Integer)
			requestContext.session ("messageSearchAffiliateId");

		for (Map.Entry<String,AffiliateRec> affiliateEntry
				: affiliates.entrySet ()) {

			printFormat (
				"<option value=\"%h\"",
				affiliateEntry.getValue ().getId (),

				equal (
						affiliateId,
						affiliateEntry.getValue ().getId ())
					? " selected"
					: "",

				">%h</option>\n",
				affiliateEntry.getKey ());

		}

		printFormat (
			"</select></p>\n");

		// actions

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"search messages\"",
			">\n",

			"<input",
			" type=\"button\"",
			" value=\"clear form\"",
			" onclick=\"clearForm ()\"",
			"></p>\n",

			"<p><input",
			" type=\"radio\"",
			" name=\"outputType\"",
			" value=\"HTML\"",
			" checked",
			">HTML</p>\n",

			"<p><input",
			" type=\"radio\"",
			" name=\"outputType\"",
			" value=\"CSV\"",
			">CSV</p>\n");

		printFormat (
			"</form>\n");

	}

	public final static
	Map<String,String> messageDirectionOptions =
		ImmutableMap.<String,String>builder ()
			.put ("", "")
			.put ("in", "inbound")
			.put ("out", "outbound")
			.build ();

}
