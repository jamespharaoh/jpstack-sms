package wbs.sms.keyword.logic;

import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.command.model.CommandRec;
import wbs.sms.keyword.model.KeywordSetFallbackObjectHelper;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("keywordLogic")
public
class KeywordLogicImplementation
	implements KeywordLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	KeywordSetFallbackObjectHelper keywordSetFallbackHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	boolean checkKeyword (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String keyword) {

		return keywordPattern
			.matcher (keyword)
			.matches ();

	}

	@Override
	public
	void createOrUpdateKeywordSetFallback (
			@NonNull Transaction parentTransaction,
			@NonNull KeywordSetRec keywordSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createOrUpdateKeywordSetFallback");

		) {

			// try and update an existing one

			KeywordSetFallbackRec keywordSetFallback =
				keywordSetFallbackHelper.find (
					transaction,
					keywordSet,
					number);

			if (keywordSetFallback != null) {

				keywordSetFallback

					.setTimestamp (
						transaction.now ())

					.setCommand (
						command);

				return;

			}

			// create a new one

			keywordSetFallbackHelper.insert (
				transaction,
				keywordSetFallbackHelper.createInstance ()

				.setKeywordSet (
					keywordSet)

				.setNumber (
					number)

				.setTimestamp (
					transaction.now ())

				.setCommand (
					command)

			);

		}

	}

	// data

	private final static
	Pattern keywordPattern =
		Pattern.compile ("[a-z0-9]+");

}
