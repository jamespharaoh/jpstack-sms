package wbs.sms.number.lookup.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

public
interface NumberLookupHelper
	extends Helper {

	@Override
	String parentObjectTypeCode ();

	boolean lookupNumber (
			NumberLookupRec numberLookup,
			NumberRec number);

	default
	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			@NonNull NumberLookupRec numberLookup,
			@NonNull List <NumberRec> numbers) {

		List <NumberRec> numbersPresent =
			new ArrayList<> ();

		List <NumberRec> numbersNotPresent =
			new ArrayList<> ();

		for (
			NumberRec number
				: numbers
		) {

			if (
				lookupNumber (
					numberLookup,
					number)
			) {

				numbersPresent.add (
					number);

			} else {

				numbersNotPresent.add (
					number);

			}

		}

		return Pair.of (
			numbersPresent,
			numbersNotPresent);

	}

}
