package wbs.sms.gsm;

import java.nio.ByteBuffer;

public
class Address {

	public static
	enum TypeOfNumber {

		unknown,
		internationalNumber,
		nationalNumber,
		networkSpecificNumber,
		alphanumeric;

	}

	public static
	enum NumberingPlanIdentification {

		isdn;

	}

	private final
	TypeOfNumber typeOfNumber;

	private final
	NumberingPlanIdentification numberingPlanIdentification;

	private final
	String addressValue;

	public
	Address (
			TypeOfNumber newTypeOfNumber,
			NumberingPlanIdentification newNumberingPlanIdentification,
			String newAddressValue) {

		typeOfNumber =
			newTypeOfNumber;

		numberingPlanIdentification =
			newNumberingPlanIdentification;

		addressValue =
			newAddressValue;

	}

	public
	TypeOfNumber getTypeOfNumber () {

		return typeOfNumber;

	}

	public
	NumberingPlanIdentification getNumberingPlanIdentification () {

		return numberingPlanIdentification;

	}

	public
	String getAddressValue () {

		return addressValue;

	}

	public static
	Address decode (
			ByteBuffer buffer)
		throws PduDecodeException {

		int addressLength =
			buffer.get () & 0xff;

		int typeOfAddress =
			buffer.get () & 0xff;

		TypeOfNumber typeOfNumber =
			typeOfNumber (typeOfAddress);

		NumberingPlanIdentification numberingPlanIdentification =
			numberingPlanIdentification (typeOfAddress);

		if (typeOfNumber == TypeOfNumber.alphanumeric) {

			int numchars =
				addressLength * 4 / 7;

			return new Address (
				typeOfNumber,
				numberingPlanIdentification,
				Gsm.decode (
					Gsm.unpack7bit (
						buffer,
						numchars)));

		} else {

			char[] addressValueChars =
				new char [addressLength];

			int b = 0xff;
			int digit;

			for (int i = 0; i < addressLength; i++) {

				if ((i & 0x01) == 0x00) {

					b = buffer.get() & 0xff;

					digit = b & 0x0f;

					if (digit > 9)
						throw new PduDecodeException (
							"Invalid digit: " + digit);

					addressValueChars [i] =
						(char) ('0' + digit);

				} else {

					digit = (b & 0xf0) >> 4;

					if (digit > 9)
						throw new PduDecodeException (
							"Invalid digit: " + digit);

					addressValueChars [i] =
						(char) ('0' + digit);

				}

			}

			return new Address (
				typeOfNumber,
				numberingPlanIdentification,
				new String (addressValueChars));

		}

	}

	public static
	TypeOfNumber typeOfNumber (
			int typeOfAddress)
		throws PduDecodeException {

		int i = (typeOfAddress & 0x70) >> 4;

		switch (i) {

		case 0x00:
			return TypeOfNumber.unknown;

		case 0x01:
			return TypeOfNumber.internationalNumber;

		case 0x02:
			return TypeOfNumber.nationalNumber;

		case 0x03:
			return TypeOfNumber.networkSpecificNumber;

		case 0x05:
			return TypeOfNumber.alphanumeric;

		default:
			throw new PduDecodeException (
				"Unknown type of number: " + i);

		}

	}

	public static
	NumberingPlanIdentification numberingPlanIdentification (
			int typeOfAddress)
		throws PduDecodeException {

		if (((typeOfAddress & 0x70) >> 4) >= 3)
			return null;

		int i =
			typeOfAddress & 0x0f;

		switch (i) {

		case 0x01:
			return NumberingPlanIdentification.isdn;

		default:
			throw new PduDecodeException(
					"Unknown numbering plan identification: " + i);

		}

	}

}
