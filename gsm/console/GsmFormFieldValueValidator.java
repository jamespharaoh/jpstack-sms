package wbs.sms.gsm.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.lessThan;
import static wbs.framework.utils.etc.Misc.moreThan;
import static wbs.framework.utils.etc.Misc.stringFormat;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

import wbs.console.forms.FormFieldValueValidator;
import wbs.framework.application.annotations.PrototypeComponent;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import wbs.sms.gsm.GsmUtils;

@Accessors (fluent = true)
@PrototypeComponent ("gsmFormFieldValueValidator")
public
class GsmFormFieldValueValidator
	implements FormFieldValueValidator<String> {

	// properties

	@Getter @Setter
	Integer minimumLength;

	@Getter @Setter
	Integer maximumLength;

	// implementation

	@Override
	public
	Optional<String> validate (
			@NonNull Optional<String> genericValue) {

		// do nothing if not present

		if (
			isNotPresent (
				genericValue)
		) {
			return Optional.<String>absent ();
		}

		String stringValue =
			genericValue.get ();

		// check validity

		if (
			! GsmUtils.isValidGsm (
				stringValue)
		) {

			return Optional.of (
				stringFormat (
					"You must only use valid GSM characters"));

		}

		// check length

		long gsmLength =
			GsmUtils.length (
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
						maximumLength,
						"GSM characters, this value is %s ",
						gsmLength,
						"GSM characters long"));

			} else if (
				isNull (
					maximumLength)
			) {

				return Optional.of (
					stringFormat (
						"You must use at least %s ",
						minimumLength,
						"GSM characters, this value is %s ",
						gsmLength,
						"GSM characters long"));

			} else if (
				equal (
					minimumLength,
					maximumLength)
			) {

				return Optional.of (
					stringFormat (
						"You must use exactly %s ",
						minimumLength,
						"GSM characters, this value is %s ",
						gsmLength,
						"GSM characters long"));

			} else {

				return Optional.of (
					stringFormat (
						"You must use between %s ",
						minimumLength,
						"and %s ",
						maximumLength,
						"GSM characters, this value is %s ",
						gsmLength,
						"GSM characters long"));

			}

		}

		// all ok

		return Optional.<String>absent ();

	}

}
