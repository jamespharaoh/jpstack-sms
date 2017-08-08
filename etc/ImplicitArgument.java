package wbs.utils.etc;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

public
class ImplicitArgument <Owned, Borrowed> {

	// state

	private final
	ThreadLocal <Owned> argumentThreadLocal =
		new ThreadLocal <Owned> ();

	private final
	Function <Owned, Borrowed> borrowFunction;

	// constructors

	public
	ImplicitArgument (
			@NonNull Function <Owned, Borrowed> borrowFunction) {

		this.borrowFunction =
			borrowFunction;

	}

	// implementation

	public <Return>
	void store (
			@NonNull Owned argument) {

		if (
			isNotNull (
				argumentThreadLocal.get ())
		) {
			throw new IllegalStateException ();
		}

		argumentThreadLocal.set (
			argument);

	}

	public
	Owned retrieve () {

		Owned argument =
			argumentThreadLocal.get ();

		if (
			isNull (
				argument)
		) {
			throw new IllegalStateException ();
		}

		argumentThreadLocal.set (
			null);

		return argument;

	}

	public <Return>
	Return storeAndInvoke (
			@NonNull Owned argument,
			@NonNull Supplier <Return> supplier) {

		store (
			argument);

		try {

			return supplier.get ();

		} finally {

			retrieve ();

		}

	}

	public
	void storeAndInvokeVoid (
			@NonNull Owned argument,
			@NonNull Runnable runnable) {

		store (
			argument);

		try {

			runnable.run ();

		} finally {

			retrieve ();

		}

	}

	public <Return>
	Return retrieveAndInvoke (
			@NonNull Function <Owned, Return> function) {

		Owned argument =
			retrieve ();

		try {

			return function.apply (
				argument);

		} finally {

			store (
				argument);

		}

	}

	public
	void retrieveAndInvokeVoid (
			@NonNull Consumer <Owned> consumer) {

		Owned argument =
			retrieve ();

		try {

			consumer.accept (
				argument);

		} finally {

			store (
				argument);

		}

	}

	public
	BorrowedArgument <Owned, Borrowed> borrow () {

		return new BorrowedArgument <Owned, Borrowed> (
			argumentThreadLocal,
			borrowFunction);

	}

	// borrowed argument class

	public static
	class BorrowedArgument <Owned, Borrowed>
		implements SafeCloseable {

		// state

		private final
		ThreadLocal <Owned> argumentThreadLocal;

		private final
		Owned ownedArgument;

		private final
		Borrowed borrowedArgument;

		private
		boolean closed;

		// constructors

		private
		BorrowedArgument (
				@NonNull ThreadLocal <Owned> argumentThreadLocal,
				@NonNull Function <Owned, Borrowed> borrowFunction) {

			ownedArgument =
				argumentThreadLocal.get ();

			if (
				isNull (
					ownedArgument)
			) {
				throw new IllegalStateException ();
			}

			argumentThreadLocal.set (
				null);

			this.argumentThreadLocal =
				argumentThreadLocal;

			borrowedArgument =
				borrowFunction.apply (
					ownedArgument);

			this.closed = false;

		}

		// public implementation

		public
		Borrowed get () {

			if (closed) {
				throw new IllegalStateException ();
			}

			return borrowedArgument;

		}

		// closeable implementation

		@Override
		public
		void close () {

			if (closed) {
				return;
			}

			try {

				if (
					isNotNull (
						argumentThreadLocal.get ())
				) {
					throw new IllegalStateException ();
				}

				argumentThreadLocal.set (
					ownedArgument);

			} finally {
				closed = true;
			}

		}

	}

}
