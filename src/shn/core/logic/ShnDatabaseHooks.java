package shn.core.logic;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.utils.data.Pair;

import shn.core.model.ShnDatabaseRec;

public
class ShnDatabaseHooks
	implements ObjectHooks <ShnDatabaseRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// public implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull ShnDatabaseRec database) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInsert");

		) {

			database

				.setImageNormaliseTime (
					transaction.now ())

			;

		}

	}

	@Override
	public
	List <Pair <Record <?>, String>> verifyData (
			@NonNull Transaction parentTransaction,
			@NonNull ShnDatabaseRec database,
			@NonNull Boolean recurse) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"verifyData");

		) {

			if (database.getDeleted ()) {
				return emptyList ();
			}

			ImmutableList.Builder <Pair <Record <?>, String>> errorsBuilder =
				ImmutableList.builder ();

			if (
				isNull (
					database.getCurrency ())
			) {

				errorsBuilder.add (
					Pair.of (
						database,
						"Must set currency for database"));

			}

			if (
				isNull (
					database.getTimezone ())
			) {

				errorsBuilder.add (
					Pair.of (
						database,
						"Must set timezone for database"));

			}

			return errorsBuilder.build ();

		}

	}

}
