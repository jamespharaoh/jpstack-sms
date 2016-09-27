package wbs.sms.messageset.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttributeFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;
import static wbs.utils.web.HtmlInputUtils.htmlOptionWrite;
import static wbs.utils.web.HtmlInputUtils.htmlSelectClose;
import static wbs.utils.web.HtmlInputUtils.htmlSelectOpen;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;

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

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/sms-message-set.js"))

			.build ();

	}

	// implementation

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

	public
	void goRow (
			long row) {

		htmlTableRowOpen ();

		// output checkbox

		htmlTableCellOpen (
			htmlRowSpanAttribute (2l));

		formatWriter.writeLineFormat (
			"<input",
			" type=\"checkbox\"",
			" id=\"enabled_%h\"",
			row,
			" name=\"enabled_%h\"",
			row,
			formData.containsKey ("enabled_" + row)
				? " checked"
				: "",
			" onclick=\"form_magic ()\"",
			">");

		htmlTableCellClose ();

		// output i

		htmlTableCellWrite (
			integerToDecimalString (
				row + 1));

		// output route

		String routeStr =
			formData.get (
				"route_" + row);

		htmlTableCellOpen ();

		htmlSelectOpen (
			stringFormat (
				"route_%s",
				row),
			htmlIdAttributeFormat (
				"route_%s",
				row));

		htmlOptionWrite ();

		for (
			RouteRec route
				: routes
		) {

			htmlOptionWrite (

				integerToDecimalString (
					route.getId ()),

				stringEqualSafe (
					Long.toString (
						route.getId ()),
					routeStr),

				stringFormat (
					"%h.%h",
					route.getSlice ().getCode (),
					route.getCode ())

			);

		}

		htmlSelectClose ();

		// output number

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" id=\"number_%h\"",
			row,
			" name=\"number_%h\"",
			row,
			" size=\"16\"",
			" value=\"%h\"",
			formData.get ("number_" + row),
			">");

		htmlTableCellClose ();

		// output chars

		htmlTableCellOpen ();

		formatWriter.writeLineFormat (
			"<span",
			" id=\"chars_%h\"",
			row,
			">&nbsp;</span>");

		htmlTableCellClose ();

		htmlTableRowClose ();

		// output second row

		htmlTableRowOpen ();

		htmlTableCellOpen (
			htmlColumnSpanAttribute (4l));

		formatWriter.writeLineFormat (
			"<textarea",
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
			">%h</textarea>",
			formData.get (
				"message_" + row));

		htmlTableCellClose ();

		htmlTableRowClose ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPost ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"hidden\"",
			" name=\"num_messages\"",
			" value=\"%h\"",
			formData.get (
				"num_messages"),
			">");

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"",
			"i",
			"Route",
			"Number",
			"Chars");

		for (
			int index = 0;
			index < numMessages;
			index ++
		) {

			htmlTableRowSeparatorWrite ();

			goRow (
				index);

		}

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"save changes\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

		htmlFormClose ();

		htmlScriptBlockWrite (
			"form_magic ()");

	}

}
