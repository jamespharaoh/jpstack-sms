package wbs.sms.message.inbox.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.utils.web.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

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

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			requestContext.resolveApplicationUrl (
				stringFormat (
					"/message.notProcessed",
					"/%u",
					message.getId (),
					"/message.notProcessed.summary")));

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents () {

		// heading

		htmlHeadingOneWrite (
			"Message—not processed");

		// table open

		htmlTableOpenDetails ();

		// id

		htmlTableDetailsRowWrite (
			"ID",
			integerToDecimalString (
				message.getId ()));

		if (
			enumNotEqualSafe (
				message.getStatus (),
				MessageStatus.notProcessed)
		) {

			// error

			htmlTableDetailsRowWrite (
				"Error",
				"Message is not in correct state");

		} else {

			// actions

			htmlTableDetailsRowWriteHtml (
				"Actions",
				() -> {

				htmlFormOpenPostAction (
					requestContext.resolveLocalUrl (
						"/message.notProcessed.form"));

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"process_again\"",
					" value=\"process again\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"ignore\"",
					" value=\"ignore\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"processed_manually\"",
					" value=\"processed manually\"",
					">");

				htmlFormClose ();

			});

		}

		// table close

		htmlTableClose ();

	}

}
