package wbs.web.responder;

import static wbs.utils.etc.Misc.doNothing;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;

import wbs.web.context.RequestContext;

@Accessors (fluent = true)
@PrototypeComponent ("textResponder")
public final
class TextResponder
	extends BufferedTextResponder {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// properties

	@Getter @Setter
	String text;

	@Getter @Setter
	String contentType =
		"text/plain";

	@Getter @Setter
	String filename;

	// property setters

	public
	TextResponder textFormat (
			@NonNull CharSequence ... arguments) {

		return text (
			stringFormatArray (
				arguments));

	}

	// implementation

	@Override
	protected
	void prepare (
			@NonNull Transaction parentTransaction) {

		doNothing ();

	}

	@Override
	protected
	void headers (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"headers");

		) {

			requestContext.contentType (
				contentType);

			if (
				isNotNull (
					filename)
			) {

				requestContext.setHeader (
					"Content-Disposition",
					stringFormat (
						"attachment; filename=%s",
						filename));

			}

		}

	}

	@Override
	protected
	void render (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"render");
		) {

			formatWriter.writeString (
				text);

		}

	}

}
