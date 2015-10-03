package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Set;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.html.JqueryScriptRef;
import wbs.platform.console.html.ScriptRef;
import wbs.platform.console.responder.HtmlResponder;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("messageNotProcessedFormResponder")
public
class MessageNotProcessedFormResponder
	extends HtmlResponder {

	// dependencies

	@Inject
	MessageConsoleHelper messageHelper;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.find (
				requestContext.stuffInt (
					"messageId"));

	}

	@Override
	protected
	Set<ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef>builder ()

			.addAll (
				super.scriptRefs ())

			.add (
				JqueryScriptRef.instance)

			.build ();

	}

	@Override
	protected
	void goHeadStuff () {

		super.goHeadStuff ();

		printFormat (
			"<script language=\"JavaScript\">\n");

		printFormat (
			"top.show_inbox (true);\n");

		printFormat (
			"top.frames['main'].location = '%j';\n",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/message.notProcessed",
					"/%u",
					message.getId (),
					"/message.notProcessed.summary")));

		printFormat (
			"</script>\n");

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<h1>Message&mdash;not processed</h1>\n");

		printFormat (
			"<table class=\"details\">");

		printFormat (
			"<tr>\n",
			"<th>ID</th>\n",
			"<td>%h</td>\n",
			message.getId (),
			"</tr>\n");

		if (message.getStatus () != MessageStatus.notProcessed) {

			printFormat (
				"<tr>\n",
				"<th>Error</th>\n",
				"<td>%h</td>\n",
				"Message is not in correct state",
				"</tr>\n");

		} else {

			printFormat (
				"<tr>\n",
				"<th>Actions</th>\n",
				"<td>\n");

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/message.notProcessed.form"),
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"process_again\"",
				" value=\"process again\"",
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"ignore\"",
				" value=\"ignore\"",
				">\n");

			printFormat (
				"<input",
				" type=\"submit\"",
				" name=\"processed_manually\"",
				" value=\"processed manually\"",
				">\n");

			printFormat (
				"</form>\n");

			printFormat (
				"</td>\n",
				"</tr>\n");

		}

		printFormat (
			"</table>\n");

	}

}
