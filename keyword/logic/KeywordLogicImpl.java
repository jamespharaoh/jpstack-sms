package wbs.sms.keyword.logic;

import java.util.Date;
import java.util.regex.Pattern;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
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

		// try and update an existing one

		KeywordSetFallbackRec keywordSetFallback =
			keywordSetFallbackHelper.find (
				keywordSet,
				number);

		if (keywordSetFallback != null) {

			keywordSetFallback
				.setTimestamp (new Date ())
				.setCommand (command);

			return;

		}

		// create a new one

		keywordSetFallbackHelper.insert (
			new KeywordSetFallbackRec ()
				.setKeywordSet (keywordSet)
				.setNumber (number)
				.setCommand (command));

	}

	// data

	private final static
	Pattern keywordPattern =
		Pattern.compile ("[a-z0-9]+");

}
