package wbs.sms.messageset.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.part.AbstractPagePart;
import wbs.platform.console.request.FormData;
import wbs.platform.console.request.MapFormData;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableSet;

@Accessors (fluent = true)
@PrototypeComponent ("messageSetPart")
public
class MessageSetPart
	extends AbstractPagePart {

	@Inject
	RouteConsoleHelper routeHelper;

	@Getter @Setter
	MessageSetFinder messageSetFinder;

	FormData formData;
	int numMessages;

	Collection<RouteRec> routes;

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/DOM.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/gsm.js"))

			.build ();

	}

	void prepareFormData (
			MessageSetRec messageSet) {

		Map<String,String> map =
			new HashMap<String,String>();

		formData =
			new MapFormData (map);

		numMessages =
			messageSet.getMessages ().size () + 2;

		if (numMessages < 4)
			numMessages = 4;

		map.put (
			"num_messages",
			Integer.toString (numMessages));

		for (
			int index = 0;
			index < numMessages;
			index ++
		) {

			MessageSetMessageRec messageSetMessage =
				index < messageSet.getMessages ().size ()
					? messageSet.getMessages ().get (index)
					: null;

			if (messageSetMessage != null) {

				map.put (
					"enabled_" + index,
					"on");

				map.put (
					"route_" + index,
					Integer.toString (
						messageSetMessage.getRoute ().getId ()));

				map.put (
					"number_" + index,
					messageSetMessage.getNumber ());

				map.put (
					"message_" + index,
					messageSetMessage.getMessage ());

			} else {

				map.put (
					"route_" + index,
					"");

				map.put (
					"number_" + index,
					"");

				map.put (
					"message_" + index,
					"");

			}

		}

	}

	@Override
	public
	void prepare () {

		MessageSetRec messageSet =
			messageSetFinder.findMessageSet (
				requestContext);

		prepareFormData (
			messageSet);

		List<RouteRec> routeList =
			routeHelper.findAll ();

		Collections.sort (
			routeList);

		routes =
			routeList;

	}

	@Override
	public
	void goHeadStuff () {

		out.println("<script language=\"JavaScript\">");
		out.println("function form_magic () {");
		out.println("  for (var i = 0; i < " + numMessages + "; i++) { ");
		out.println("    var check = document.getElementById ('enabled_' + i)");
		out.println("    var route = document.getElementById ('route_' + i)");
		out.println("    var number = document.getElementById ('number_' + i)");
		out
				.println("    var message = document.getElementById ('message_' + i)");
		out.println("    route.disabled = ! check.checked");
		out.println("    number.disabled = ! check.checked");
		out.println("    message.disabled = ! check.checked");
		out.println("  }");
		out.println("}");
		out.println("</script>");
	}

	public
	void goRow (
			int row) {

		out.println ("<tr>");

		// output checkbox
		out.print("<td rowspan=\"2\"><input type=\"checkbox\" id=\"enabled_"
				+ row + "\" name=\"enabled_" + row + "\"");
		if (formData.contains("enabled_" + row))
			out.print(" checked");
		out.println(" onclick=\"form_magic ()\"></td>");

		// output i
		out.println("<td>" + (row + 1) + "</td>");

		// output route

		String routeStr =
			formData.get ("route_" + row);

		out.print (
			stringFormat (
				"<td><select",
				" id=\"route_%h\"",
				row,
				" name=\"route_%h\"",
				row,
				">\n",

				"<option>\n"));

		for (RouteRec route
				: routes) {

			out.print (
				stringFormat (
					"<option",
					" value=\"%h\"",
					route.getId (),
					equal (
						Integer.toString (
							route.getId ()),
							routeStr)
						? " selected" : "",
					">%h.%h</option>\n",
					route.getSlice ().getCode (),
					route.getCode ()));

		}

		out.println("</select></td>");

		// output number
		out.println("<td><input type=\"text\" id=\"number_" + row
				+ "\" name=\"number_" + row + "\" size=\"16\"" + " value=\""
				+ Html.encode(formData.get("number_" + row)) + "\"></td>");

		// output chars
		out.println("<td><span id=\"chars_" + row + "\">&nbsp;</span></td>");

		out.println("</tr>");

		// output second row
		out.println("<tr> <td colspan=\"4\">");
		out
				.println("<textarea rows=\"3\" cols=\"96\" id=\"message_"
						+ row
						+ "\" name=\"message_"
						+ row
						+ "\""
						+ " onkeyup=\"gsmCharCount (this, document.getElementById ('chars_"
						+ row
						+ "'))\""
						+ " onfocus=\"gsmCharCount (this, document.getElementById ('chars_"
						+ row + "'))\">"
						+ Html.encode(formData.get("message_" + row))
						+ "</textarea>");
		out.println("</td> </tr>");
	}

	@Override
	public
	void goBodyStuff () {

		out.println("<form method=\"post\">");

		out.println("<input type=\"hidden\" name=\"num_messages\"" + "value=\""
				+ Html.encode(formData.get("num_messages")) + "\">");

		out.println("<p><input type=\"submit\" value=\"save changes\"></p>");

		out.println("<table class=\"list\" border=\"0\" cellspacing=\"1\">");

		out.println("<tr>");
		out.println("<th>&nbsp;</th>");
		out.println("<th>i</th>");
		out.println("<th>Route</th>");
		out.println("<th>Number</th>");
		out.println("<th>Chars</th>");
		out.println("</tr>");

		for (int i = 0; i < numMessages; i++) {
			out.println("<tr class=\"sep\">");
			goRow(i);
		}

		out.println("</table>");

		out.println("<p><input type=\"submit\" value=\"save changes\"></p>");

		out.println("</form>");

		out.println("<script language=\"JavaScript\">");
		out.println("form_magic ()");
		out.println("</script>");

	}

}
