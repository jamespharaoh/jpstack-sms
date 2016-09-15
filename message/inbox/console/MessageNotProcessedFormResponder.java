package wbs.sms.message.inbox.console;

import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.responder.HtmlResponder;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageNotProcessedFormResponder")
public
class MessageNotProcessedFormResponder
	extends HtmlResponder {

	// singleton dependencies

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
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
	void renderHtmlHeadContents () {

		super.renderHtmlHeadContents ();

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
	void renderHtmlBodyContents () {

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
