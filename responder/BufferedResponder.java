package wbs.web.responder;

import static wbs.utils.collection.CollectionUtils.arrayLength;
import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.BorrowedOutputStream;
import wbs.utils.io.RuntimeIoException;

import wbs.web.context.RequestContext;

public abstract
class BufferedResponder
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
			OutputStream outputStream);

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

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

			) {

				render (
					transaction,
					byteArrayOutputStream);

				byte[] bytes =
					byteArrayOutputStream.toByteArray ();

				headers (
					transaction);

				send (
					transaction,
					bytes);

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

	}

	// private implementation

	private
	void send (
			@NonNull Transaction parentTransaction,
			@NonNull byte[] bytes) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"send");

		) {

			requestContext.setHeader (
				"Content-Length",
				integerToDecimalString (
					arrayLength (
						bytes)));

			try (

				BorrowedOutputStream outputStream =
					requestContext.outputStream ();

			) {

				writeBytes (
					outputStream,
					bytes);

			}

		}

	}

}
