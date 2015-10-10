package wbs.sms.message.status.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlHeadContent () {

		printFormat (
			"<style type=\"text/css\">\n",
			"#inboxRow, #outboxRow { display: none; cursor: pointer; }\n",
			"</style>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",

			"function updateMessage (numInbox, numOutbox, numNotPro) {\n",

			"  var inboxCell = document.getElementById ('inboxCell');\n",
			"  var outboxCell = document.getElementById ('outboxCell');\n",

			"  var inboxRow = document.getElementById ('inboxRow');\n",
			"  var outboxRow = document.getElementById ('outboxRow');\n",

			"  if (numInbox > 0) inboxCell.firstChild.data = '' + numInbox + ' items in inbox';\n",
			"  if (numOutbox > 0) outboxCell.firstChild.data = '' + numOutbox + ' items in outbox';\n",

			"  showTableRow (inboxRow, numInbox > 0);\n",
			"  showTableRow (outboxRow, numOutbox > 0);\n",

			"}\n",

			"</script>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<tr",
			" id=\"inboxRow\"",
			" onmouseover=\"this.className='hover';\"",
			" onmouseout=\"this.className='';\"",
			" onclick=\"top.frames.main.location='%j';\"",
			requestContext.resolveApplicationUrl (
				"/inbox"),
			">\n",

			"<td id=\"inboxCell\">-</td>\n",

			"</tr>\n");

		printFormat (
			"<tr",
			" id=\"outboxRow\"",
			" onmouseover=\"this.className='hover';\"",
			" onmouseout=\"this.className='';\"",
			" onclick=\"top.frames.main.location='%j';\"",
			requestContext.resolveApplicationUrl (
				"/outbox"),
			">\n",

			"<td id=\"outboxCell\">-</td>\n",

			"</tr>\n");

	}

}
