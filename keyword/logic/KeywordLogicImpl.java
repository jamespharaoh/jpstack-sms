package wbs.sms.keyword.logic;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.regex.Pattern;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.command.model.CommandRec;
import wbs.sms.keyword.model.KeywordSetFallbackObjectHelper;
import wbs.sms.keyword.model.KeywordSetFallbackRec;
import wbs.sms.keyword.model.KeywordSetRec;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("keywordLogic")
public
class KeywordLogicImpl
	implements KeywordLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	KeywordSetFallbackObjectHelper keywordSetFallbackHelper;

	// implementation

	@Override
	public
	boolean checkKeyword (
			String keyword) {

		return keywordPattern
			.matcher (keyword)
			.matches ();

	}

	@Override
	public
	void createOrUpdateKeywordSetFallback (
			KeywordSetRec keywordSet,
			NumberRec number,
			CommandRec command) {

		Transaction transaction =
			database.currentTransaction ();

		// try and update an existing one

		KeywordSetFallbackRec keywordSetFallback =
			keywordSetFallbackHelper.find (
				keywordSet,
				number);

		if (keywordSetFallback != null) {

			keywordSetFallback

				.setTimestamp (
					instantToDate (
						transaction.now ()))

				.setCommand (
					command);

			return;

		}

		// create a new one

		keywordSetFallbackHelper.insert (
			keywordSetFallbackHelper.createInstance ()

			.setKeywordSet (
				keywordSet)

			.setNumber (
				number)

			.setTimestamp (
				instantToDate (
					transaction.now ()))

			.setCommand (
				command)

		);

	}

	// data

	private final static
	Pattern keywordPattern =
		Pattern.compile ("[a-z0-9]+");

}
