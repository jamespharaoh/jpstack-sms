package wbs.sms.messageset.console;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageSetPart")
public
class MessageSetPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// properties

	@Getter @Setter
	MessageSetFinder messageSetFinder;

	// state

	Map <String,String> formData;
	int numMessages;

	Collection<RouteRec> routes;

	// implementation

	@Override
	public
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/DOM.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/gsm.js"))

			.build ();

	}

	void prepareFormData (
			MessageSetRec messageSet) {

		formData =
			new HashMap<String,String>();

		numMessages =
			messageSet.getMessages ().size () + 2;

		if (numMessages < 4)
			numMessages = 4;

		formData.put (
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

				formData.put (
					"enabled_" + index,
					"on");

				formData.put (
					"route_" + index,
					Long.toString (
						messageSetMessage.getRoute ().getId ()));

				formData.put (
					"number_" + index,
					messageSetMessage.getNumber ());

				formData.put (
					"message_" + index,
					messageSetMessage.getMessage ());

			} else {

				formData.put (
					"route_" + index,
					"");

				formData.put (
					"number_" + index,
					"");

				formData.put (
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
	void renderHtmlHeadContent () {

		printFormat (
			"<script language=\"JavaScript\">\n",
			"function form_magic () {\n",
			"  for (var i = 0; i < " + numMessages + "; i++) {\n",
			"    var check = document.getElementById ('enabled_' + i)\n",
			"    var route = document.getElementById ('route_' + i)\n",
			"    var number = document.getElementById ('number_' + i)\n",
			"    var message = document.getElementById ('message_' + i)\n",
			"    route.disabled = ! check.checked\n",
			"    number.disabled = ! check.checked\n",
			"    message.disabled = ! check.checked\n",
			"  }\n",
			"}\n",
			"</script>\n");

	}


	public
	void goRow (
			int row) {

		printFormat (
			"<tr>\n");

		// output checkbox

		printFormat (
			"<td",
			" rowspan=\"2\"",
			"><input",
			" type=\"checkbox\"",
			" id=\"enabled_%h\"",
			row,
			" name=\"enabled_%h\"",
			row,
			formData.containsKey ("enabled_" + row)
				? " checked"
				: "",
			" onclick=\"form_magic ()\"",
			"></td>\n");

		// output i

		printFormat (
			"<td>%h</td>\n",
			row + 1);

		// output route

		String routeStr =
			formData.get (
				"route_" + row);

		printFormat (
			"<td><select",
			" id=\"route_%h\"",
			row,
			" name=\"route_%h\"",
			row,
			">\n");

		printFormat (
			"<option>\n");

		for (RouteRec route
				: routes) {

			printFormat (
				"<option",
				" value=\"%h\"",
				route.getId (),
				ifThenElse (
					stringEqualSafe (
						Long.toString (
							route.getId ()),
						routeStr),
					() -> " selected",
					() -> ""),
				">%h.%h</option>\n",
				route.getSlice ().getCode (),
				route.getCode ());

		}

		printFormat (
			"</select></td>");

		// output number

		printFormat (
			"<td><input",
			" type=\"text\"",
			" id=\"number_%h\"",
			row,
			" name=\"number_%h\"",
			row,
			" size=\"16\"",
			" value=\"%h\"",
			formData.get ("number_" + row),
			"></td>\n");

		// output chars

		printFormat (
			"<td><span",
			" id=\"chars_%h\"",
			row,
			">&nbsp;</span></td>\n");

		printFormat (
			"</tr>\n");

		// output second row

		printFormat (
			"<tr>\n");

		printFormat (
			"<td",
			" colspan=\"4\"><textarea",
			" rows=\"3\"",
			" cols=\"96\"",
			" id=\"message_%h\"",
			row,
			" name=\"message_%h\"",
			row,
			" onkeyup=\"%h\"",
			stringFormat (
				"gsmCharCount (this, document.getElementById ('chars_%j'))",
				row),
			" onfocus=\"%h\"",
			stringFormat (
				"gsmCharCount (this, document.getElementById ('chars_%j'))",
				row),
			">%h</textarea></td>\n",
			formData.get (
				"message_" + row));

		printFormat (
			"</tr>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"num_messages\"",
			" value=\"%h\"",
			formData.get (
				"num_messages"),
			">\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"<table",
			" class=\"list\"",
			" border=\"0\"",
			" cellspacing=\"1\"",
			">\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>i</th>\n",
			"<th>Route</th>\n",
			"<th>Number</th>\n",
			"<th>Chars</th>\n",
			"</tr>\n");

		for (
			int index = 0;
			index < numMessages;
			index ++
		) {

			printFormat (
				"<tr class=\"sep\">\n");

			goRow (
				index);

		}

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" value=\"save changes\"",
			"></p>\n");

		printFormat (
			"</form>\n");

		printFormat (
			"<script language=\"JavaScript\">\n",
			"form_magic ()\n",
			"</script>\n");

	}

}
