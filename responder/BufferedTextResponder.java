package wbs.web.responder;

import static wbs.utils.etc.IoUtils.writeString;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.LazyFormatWriter;

import wbs.web.context.RequestContext;

public abstract
class BufferedTextResponder
	implements WebResponder {

	// singleton components

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RequestContext requestContext;

	// abstract methods

	protected abstract
	void prepare (
			Transaction parentTransaction);

	protected abstract
	void render (
			Transaction parentTransaction,
			FormatWriter formatWriter);

	protected abstract
	void headers (
			Transaction parentTransaction);

	// implementation

	@Override
	public
	void execute (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"execute");

		) {

			prepare (
				transaction);

			try (

				LazyFormatWriter formatWriter =
					new LazyFormatWriter ();

			) {

				formatWriter.indentString (
					"  ");

				render (
					transaction,
					formatWriter);

				headers (
					transaction);

				send (
					transaction,
					formatWriter.stringParts ());

			}

		}

	}

	// private implementation

	private
	void send (
			@NonNull Transaction parentTransaction,
			@NonNull List <String> stringParts) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"send");

		) {

			try (

				Writer writer =
					requestContext.writer ();

			) {

				for (
					String stringPart
						: stringParts
				) {

					writeString (
						writer,
						stringPart);

				}

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

	}

}