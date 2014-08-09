package wbs.sms.number.format.logic;

import static wbs.framework.utils.etc.Misc.doesNotStartWith;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.moreThan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.format.model.NumberFormatPatternRec;
import wbs.sms.number.format.model.NumberFormatRec;

@Accessors (fluent = true)
@SingletonComponent ("numberFormatLogicImpl")
public
class NumberFormatLogicImpl
	implements NumberFormatLogic {

	@Override
	public
	String parse (
			@NonNull NumberFormatRec numberFormat,
			@NonNull String number)
		throws WbsNumberFormatException {

		// remove ignorable characters

		number =
			ignorableCharactersPattern
				.matcher (number)
				.replaceAll ("");

		// try each pattern

		for (NumberFormatPatternRec numberFormatPattern
				: numberFormat.getNumberFormatPatterns ()) {

			if (numberFormatPattern.getDeleted ())
				continue;

			if (
				lessThan (
					number.length (),
					numberFormatPattern.getMinimumLength ())
			) {
				continue;
			}

			if (
				moreThan (
					number.length (),
					numberFormatPattern.getMaximumLength ())
			) {
				continue;
			}

			if (
				doesNotStartWith (
					number,
					numberFormatPattern.getInputPrefix ())
			) {
				continue;
			}

			return joinWithSeparator (
				"",
				numberFormatPattern.getOutputPrefix (),
				number.substring (
					numberFormatPattern.getInputPrefix ().length ()));

		}

		// error if nothing matched

		throw new WbsNumberFormatException (
			"The number specified does not match any recognised format");

	}

	@Override
	public
	List<String> parseLines (
			NumberFormatRec numberFormat,
			String numbers)
		throws WbsNumberFormatException {

		String[] lines =
			lineSplitterPattern.split (numbers);

		List<String> numberList =
			new ArrayList<String> ();

		for (
			int lineNumber = 0;
			lineNumber < lines.length;
			lineNumber ++
		) {

			String number1 =
				ignorableCharactersPattern
					.matcher (lines [lineNumber])
					.replaceAll ("");

			if (number1.length () == 0)
				continue;

			String number2 =
				parse (
					numberFormat,
					number1);

			numberList.add (
				number2);

		}

		return numberList;

	}

	Pattern lineSplitterPattern =
		Pattern.compile ("\\n|\\r");

	Pattern ignorableCharactersPattern =
		Pattern.compile ("[ -()]+");

}
