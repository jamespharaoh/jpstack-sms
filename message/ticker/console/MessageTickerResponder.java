package wbs.sms.message.ticker.console;

import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableBodyOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeadOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.responder.ConsoleHtmlResponder;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("messageTickerResponder")
public
class MessageTickerResponder
	extends ConsoleHtmlResponder {

	// constants

	int reloadMs = 1000;
	int maxEntries = 100;

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	protected
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/js-cookie-2.0.4.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/async.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/message-ticker.js"))

			.build ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

		// heading

		htmlHeadingTwoWrite (
			"Message ticker");

		// table

		htmlTableOpenList (
			htmlIdAttribute (
				"tickerTable"));

		htmlTableHeadOpen ();

		htmlTableHeaderRowWrite (
			"",
			"Time",
			"From",
			"To",
			"Message",
			"S");

		htmlTableHeadClose ();

		htmlTableBodyOpen ();

		htmlTableRowOpen (
			htmlClassAttribute (
				"message-ticker-loading"));

		htmlTableCellWrite (
			"Loading, please wait...",
			htmlColumnSpanAttribute (6l));

		htmlTableRowClose ();

		htmlTableBodyClose ();

		htmlTableClose ();

	}

}
