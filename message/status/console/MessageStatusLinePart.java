package wbs.sms.message.status.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributeFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.HtmlLink;
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/sms-messages-status.css")

		);

	}

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			ConsoleApplicationScriptRef.javascript (
				"/js/sms-messages-status.js")

		);

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
