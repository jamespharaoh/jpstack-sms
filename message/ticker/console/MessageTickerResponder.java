package wbs.sms.message.ticker.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlIdAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableBodyClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableBodyOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeadClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeadOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.context.ConsoleApplicationScriptRef;
import wbs.console.html.ScriptRef;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("messageTickerResponder")
public
class MessageTickerResponder
	extends HtmlResponder {

	int reloadMs = 1000;
	int maxEntries = 100;

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/rpc.js"))

			.add (
				ConsoleApplicationScriptRef.javascript (
					"/js/message-ticker.js"))

			.build ();

	}

	@Override
	protected
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormatIncreaseIndent (
			"var messageTickerParams = {");

		formatWriter.writeLineFormat (
			"reloadMs: %s,",
			integerToDecimalString (
				reloadMs));

		formatWriter.writeLineFormat (
			"maxEntries: %s",
			integerToDecimalString (
				maxEntries));

		formatWriter.writeLineFormatDecreaseIndent (
			"}");

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

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

		htmlTableRowOpen ();

		htmlTableCellWrite (
			"Loading, please wait...",
			htmlColumnSpanAttribute (6l));

		htmlTableRowClose ();

		htmlTableBodyClose ();

		htmlTableClose ();

		// script

		htmlScriptBlockWrite (
			"messageTicker.doUpdate ();");

	}

}
