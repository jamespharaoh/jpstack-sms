package wbs.framework.database;

import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ImplicitArgument;

public
interface Transaction
	extends
		TaskLogger,
		TransactionMethods {

	default
	void commit () {

		ownedTransaction ().commit (
			this);

	}

	// implicit argument

	ImplicitArgument <CloseableTransaction, BorrowedTransaction>
		implicitArgument =
			new ImplicitArgument<> (
				Transaction::borrowTransaction);

}
