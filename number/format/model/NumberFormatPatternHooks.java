package wbs.sms.number.format.model;

import wbs.framework.object.AbstractObjectHooks;

public
class NumberFormatPatternHooks
	extends AbstractObjectHooks<NumberFormatPatternRec> {

	@Override
	public
	void beforeInsert (
			NumberFormatPatternRec numberFormatPattern) {

		NumberFormatRec numberFormat =
			numberFormatPattern.getNumberFormat ();

		numberFormatPattern.setIndex (
			numberFormat.getNumPatterns ());

		numberFormat.setNumPatterns (
			numberFormat.getNumPatterns () + 1);

	}

}