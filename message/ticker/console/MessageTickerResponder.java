package wbs.sms.message.ticker.console;

import java.util.Set;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.context.ConsoleContextScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.responder.HtmlResponder;

import com.google.common.collect.ImmutableSet;

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
				ConsoleContextScriptRef.javascript (
					"/js/rpc.js"))

			.add (
				ConsoleContextScriptRef.javascript (
					"/js/message-ticker.js"))

			.build ();

	}

	@Override
	protected
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script type=\"text/javascript\">\n",
			"  var messageTickerParams = {\n",
			"    reloadMs: %s,\n", reloadMs,
			"    maxEntries: %s\n", maxEntries,
			"  }\n",
			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h2>Message ticker</h1>\n");

		printFormat (
			"<table id=\"tickerTable\" class=\"list\">\n",
			"<tbody>\n");

		printFormat (
			"<tr>\n",
			"<th>&nbsp;</th>\n",
			"<th>Time</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Message</th>\n",
			"<th>S</th>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<td colspan=\"6\">Loading, please wait...</td>\n",
			"</tr>\n");

		printFormat (
			"</tbody>\n",
			"</table>\n");

		printFormat (
			"<script type=\"text/javascript\">\n",
			"  messageTicker.doUpdate ();\n",
			"</script>\n");

	}

}
