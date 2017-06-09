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
import wbs.console.html.ScriptRef;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("messageStatusLinePart")
public
class MessageStatusLinePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// details

	@Override
	public
	Set <ScriptRef> scriptRefs () {

		return ImmutableSet.<ScriptRef> of (

			ConsoleApplicationScriptRef.javascript (
				"/js/sms-messages-status.js")

		);

	}

	// implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			renderInboxRow (
				transaction,
				formatWriter);

			renderOutboxRow (
				transaction,
				formatWriter);

		}

	}

	private
	void renderInboxRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderInboxRow");

		) {

			htmlTableRowOpen (
				formatWriter,

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
				formatWriter,
				"—",
				htmlIdAttribute (
					"inboxCell"));

			htmlTableRowClose (
				formatWriter);

		}

	}

	private
	void renderOutboxRow (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderOutboxRow");

		) {

			htmlTableRowOpen (
				formatWriter,

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
				formatWriter,
				"—",
				htmlIdAttribute (
					"outboxCell"));

			htmlTableRowClose (
				formatWriter);

		}

	}

}
