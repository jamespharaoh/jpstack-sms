package wbs.sms.gsm.console;

import static wbs.sms.gsm.GsmUtils.gsmStringIsValid;
import static wbs.sms.gsm.GsmUtils.gsmStringLength;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.types.FormFieldValueValidator;

import wbs.framework.component.annotations.PrototypeComponent;

@Accessors (fluent = true)
@PrototypeComponent ("gsmFormFieldValueValidator")
public
class GsmFormFieldValueValidator
	implements FormFieldValueValidator <String> {

	// properties

	@Getter @Setter
	Integer minimumLength;

	@Getter @Setter
	Integer maximumLength;

	// implementation

	@Override
	public
	Optional <String> validate (
			@NonNull Optional <String> genericValue) {

		// do nothing if not present

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return Optional.absent ();
		}

		String stringValue =
			genericValue.get ();

		// check validity

		if (
			! gsmStringIsValid (
				stringValue)
		) {

			return Optional.of (
				stringFormat (
					"You must only use valid GSM characters"));

		}

		// check length

		long gsmLength =
			gsmStringLength (
				stringValue);

		if (

			(

				isNotNull (
					minimumLength)

				&& lessThan (
					gsmLength,
					minimumLength)

			) || (

				isNotNull (
					maximumLength)

				&& moreThan (
					gsmLength,
					maximumLength)

			)

		) {

			if (
				isNull (
					minimumLength)
			) {

				return Optional.of (
					stringFormat (
						"You must use no more than %s ",
						integerToDecimalString (
							maximumLength),
						"GSM characters, this value is %s ",
						integerToDecimalString (
							gsmLength),
						"GSM characters long"));

			} else if (
				isNull (
					maximumLength)
			) {

				return Optional.of (
					stringFormat (
						"You must use at least %s ",
						integerToDecimalString (
							minimumLength),
						"GSM characters, this value is %s ",
						integerToDecimalString (
							gsmLength),
						"GSM characters long"));

			} else if (
				integerEqualSafe (
					minimumLength,
					maximumLength)
			) {

				return Optional.of (
					stringFormat (
						"You must use exactly %s ",
						integerToDecimalString (
							minimumLength),
						"GSM characters, this value is %s ",
						integerToDecimalString (
							gsmLength),
						"GSM characters long"));

			} else {

				return Optional.of (
					stringFormat (
						"You must use between %s ",
						integerToDecimalString (
							minimumLength),
						"and %s ",
						integerToDecimalString (
							maximumLength),
						"GSM characters, this value is %s ",
						integerToDecimalString (
							gsmLength),
						"GSM characters long"));

			}

		}

		// all ok

		return Optional.<String>absent ();

	}

}
