package wbs.sms.message.inbox.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingOneWrite;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockClose;
import static wbs.web.utils.HtmlScriptUtils.htmlScriptBlockOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.html.ScriptRef;
import wbs.console.misc.JqueryScriptRef;
import wbs.console.responder.ConsoleHtmlResponder;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageNotProcessedFormResponder")
public
class MessageNotProcessedFormResponder
	extends ConsoleHtmlResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		message =
			messageHelper.findFromContextRequired ();

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
	void renderHtmlHeadContents (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlHeadContents");

		super.renderHtmlHeadContents (
			taskLogger);

		htmlScriptBlockOpen ();

		formatWriter.writeLineFormat (
			"top.show_inbox (true);");

		formatWriter.writeLineFormat (
			"top.frames ['main'].location = '%j';",
			requestContext.resolveApplicationUrlFormat (
				"/message.notProcessed",
				"/%u",
				integerToDecimalString (
					message.getId ()),
				"/message.notProcessed.summary"));

		htmlScriptBlockClose ();

	}

	@Override
	public
	void renderHtmlBodyContents (
			@NonNull TaskLogger parentTaskLogger) {

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
