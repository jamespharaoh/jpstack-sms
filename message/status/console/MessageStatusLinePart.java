package wbs.sms.message.status.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	void goHeadStuff () {

		printFormat (
			"<style type=\"text/css\">\n",
			"#inboxRow, #outboxRow, #notProRow { display: none; cursor: pointer; }\n",
			"</style>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",

			"function updateMessage (numInbox, numOutbox, numNotPro) {\n",

			"  var inboxCell = document.getElementById ('inboxCell');\n",
			"  var outboxCell = document.getElementById ('outboxCell');\n",
			"  var notProCell = document.getElementById ('notProCell');\n",

			"  var inboxRow = document.getElementById ('inboxRow');\n",
			"  var outboxRow = document.getElementById ('outboxRow');\n",
			"  var notProRow = document.getElementById ('notProRow');\n",

			"  if (numInbox > 0) inboxCell.firstChild.data = '' + numInbox + ' items in inbox';\n",
			"  if (numOutbox > 0) outboxCell.firstChild.data = '' + numOutbox + ' items in outbox';\n",
			"  if (numNotPro > 0) notProCell.firstChild.data = '' + numNotPro + ' items not processed';\n",

			"  showTableRow (inboxRow, numInbox > 0);\n",
			"  showTableRow (outboxRow, numOutbox > 0);\n",
			"  showTableRow (notProRow, numNotPro > 0);\n",

			"}\n",

			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

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

		printFormat (
			"<tr",
			" id=\"notProRow\"",
			" onmouseover=\"this.className='hover';\"",
			" onmouseout=\"this.className='';\"",
			" onclick=\"top.frames.main.location='%j';\"",
			requestContext.resolveApplicationUrl (
				"/messageNotProcessed"),
			">\n",

			"<td id=\"notProCell\">-</td>\n",

			"</tr>\n");

	}

}
