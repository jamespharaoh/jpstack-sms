package wbs.sms.number.format.logic;

import lombok.NonNull;
import wbs.framework.object.ObjectHooks;
import wbs.sms.number.format.model.NumberFormatPatternRec;
import wbs.sms.number.format.model.NumberFormatRec;

public
class NumberFormatPatternHooks
	implements ObjectHooks<NumberFormatPatternRec> {

	@Override
	public
	void beforeInsert (
			@NonNull NumberFormatPatternRec numberFormatPattern) {

		NumberFormatRec numberFormat =
			numberFormatPattern.getNumberFormat ();

		numberFormatPattern.setIndex (
			numberFormat.getNumPatterns ());

		numberFormat.setNumPatterns (
			numberFormat.getNumPatterns () + 1);

	}

}