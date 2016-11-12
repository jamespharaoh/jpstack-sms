package wbs.sms.message.status.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributeFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent (
			@NonNull TaskLogger parentTaskLogger) {

		renderStyleBlock ();
		renderScriptBlock ();

	}

	private
	void renderStyleBlock () {

		htmlStyleBlockOpen ();

		htmlStyleRuleWrite (
			"#inboxRow",
			"#outboxRow",
			htmlStyleRuleEntry (
				"cursor",
				"pointer"));

		htmlStyleRuleClose ();

		htmlStyleBlockClose ();

	}

	private
	void renderScriptBlock () {

		// script block open

		htmlScriptBlockOpen ();

		// function open

		formatWriter.writeLineFormatIncreaseIndent (
			"function updateMessage (numInbox, numOutbox, numNotPro) {");

		// variables

		formatWriter.writeLineFormat (
			"var inboxCell = document.getElementById ('inboxCell');");

		formatWriter.writeLineFormat (
			"var outboxCell = document.getElementById ('outboxCell');");

		formatWriter.writeLineFormat (
			"var inboxRow = document.getElementById ('inboxRow');");

		formatWriter.writeLineFormat (
			"var outboxRow = document.getElementById ('outboxRow');");

		// inbox items

		formatWriter.writeLineFormatIncreaseIndent (
			"if (numInbox > 0) {");

		formatWriter.writeLineFormat (
			"inboxCell.firstChild.data = '' + numInbox + ' items in inbox';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormat (
			"showTableRow (inboxRow, numInbox > 0);");

		// outbox items

		formatWriter.writeLineFormatIncreaseIndent (
			"if (numOutbox > 0) {");

		formatWriter.writeLineFormat (
			"outboxCell.firstChild.data = '' + numOutbox + ' items in outbox';");

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		formatWriter.writeLineFormat (
			"showTableRow (outboxRow, numOutbox > 0);");

		// function close

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		// script block close

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		renderInboxRow ();
		renderOutboxRow ();

	}

	private
	void renderInboxRow () {

		htmlTableRowOpen (

			htmlIdAttribute (
				"inboxRow"),

			htmlAttribute (
				"onmouseover",
				"this.className='hover';"),

			htmlAttribute (
				"onmouseout",
				"this.className='';"),

			htmlAttributeFormat (
				"onclick",
				"top.frames.main.location='%j';",
				requestContext.resolveApplicationUrl (
					"/inbox")),

			htmlStyleRuleEntry (
				"display",
				"none")

		);

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"inboxCell"));

		htmlTableRowClose ();

	}

	private
	void renderOutboxRow () {

		htmlTableRowOpen (

			htmlIdAttribute (
				"outboxRow"),

			htmlAttribute (
				"onmouseover",
				"this.className='hover';"),

			htmlAttribute (
				"onmouseout",
				"this.className='';"),

			htmlAttributeFormat (
				"onclick",
				"top.frames.main.location='%j'",
				requestContext.resolveApplicationUrl (
					"/outboxes")),

			htmlStyleRuleEntry (
				"display",
				"none")

		);

		htmlTableCellWrite (
			"—",
			htmlIdAttribute (
				"outboxCell"));

		htmlTableRowClose ();

	}

}
