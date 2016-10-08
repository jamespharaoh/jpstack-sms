package wbs.sms.message.status.console;

import static wbs.utils.web.HtmlAttributeUtils.htmlAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlAttributeFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleBlockOpen;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleClose;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.utils.web.HtmlStyleUtils.htmlStyleRuleWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent () {

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
	void renderHtmlBodyContent () {

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
