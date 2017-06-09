package wbs.sms.messageset.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttributeFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlInputUtils.htmlOptionWrite;
import static wbs.web.utils.HtmlInputUtils.htmlSelectClose;
import static wbs.web.utils.HtmlInputUtils.htmlSelectOpen;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
@PrototypeComponent ("messageSetPart")
public
class MessageSetPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// properties

	@Getter @Setter
	MessageSetFinder messageSetFinder;

	// state

	Map <String,String> formData;
	int numMessages;

	Collection <RouteRec> routes;

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
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			MessageSetRec messageSet =
				messageSetFinder.findMessageSet (
					transaction,
					requestContext);

			prepareFormData (
				messageSet);

			List<RouteRec> routeList =
				routeHelper.findAll (
					transaction);

			Collections.sort (
				routeList);

			routes =
				routeList;

		}

	}

	public
	void goRow (
			@NonNull FormatWriter formatWriter,
			@NonNull Long row) {

		htmlTableRowOpen (
			formatWriter);

		// output checkbox

		htmlTableCellOpen (
			formatWriter,
			htmlRowSpanAttribute (2l));

		formatWriter.writeLineFormat (
			"<input",
			" type=\"checkbox\"",
			" id=\"enabled_%h\"",
			integerToDecimalString (
				row),
			" name=\"enabled_%h\"",
			integerToDecimalString (
				row),
			formData.containsKey ("enabled_" + row)
				? " checked"
				: "",
			" onclick=\"form_magic ()\"",
			">");

		htmlTableCellClose (
			formatWriter);

		// output i

		htmlTableCellWrite (
			formatWriter,
			integerToDecimalString (
				row + 1));

		// output route

		String routeStr =
			formData.get (
				"route_" + row);

		htmlTableCellOpen (
			formatWriter);

		htmlSelectOpen (
			formatWriter,
			stringFormat (
				"route_%s",
				integerToDecimalString (
					row)),
			htmlIdAttributeFormat (
				"route_%s",
				integerToDecimalString (
					row)));

		htmlOptionWrite (
			formatWriter);

		for (
			RouteRec route
				: routes
		) {

			htmlOptionWrite (
				formatWriter,

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

		htmlSelectClose (
			formatWriter);

		// output number

		htmlTableCellOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"<input",
			" type=\"text\"",
			" id=\"number_%h\"",
			integerToDecimalString (
				row),
			" name=\"number_%h\"",
			integerToDecimalString (
				row),
			" size=\"16\"",
			" value=\"%h\"",
			formData.get ("number_" + row),
			">");

		htmlTableCellClose (
			formatWriter);

		// output chars

		htmlTableCellOpen (
			formatWriter);

		formatWriter.writeLineFormat (
			"<span",
			" id=\"chars_%h\"",
			integerToDecimalString (
				row),
			">&nbsp;</span>");

		htmlTableCellClose (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

		// output second row

		htmlTableRowOpen (
			formatWriter);

		htmlTableCellOpen (
			formatWriter,
			htmlColumnSpanAttribute (4l));

		formatWriter.writeLineFormat (
			"<textarea",
			" rows=\"3\"",
			" cols=\"96\"",
			" id=\"message_%h\"",
			integerToDecimalString (
				row),
			" name=\"message_%h\"",
			integerToDecimalString (
				row),
			" onkeyup=\"%h\"",
			stringFormat (
				"gsmCharCount (this, document.getElementById ('chars_%j'))",
				integerToDecimalString (
					row)),
			" onfocus=\"%h\"",
			stringFormat (
				"gsmCharCount (this, document.getElementById ('chars_%j'))",
				integerToDecimalString (
					row)),
			">%h</textarea>",
			formData.get (
				"message_" + row));

		htmlTableCellClose (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

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

			htmlFormOpenPost (
				formatWriter);

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

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"",
				"i",
				"Route",
				"Number",
				"Chars");

			for (
				long index = 0l;
				index < numMessages;
				index ++
			) {

				htmlTableRowSeparatorWrite (
					formatWriter);

				goRow (
					formatWriter,
					index);

			}

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"save changes\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			htmlScriptBlockWrite (
				formatWriter,
				"form_magic ()");

		}

	}

}
