package wbs.sms.gsm;

import java.nio.ByteBuffer;

public
class TimeStamp {

	private final
	int year, month, day;

	private final
	int hour, minute, second;

	private final
	int tz;

	public
	TimeStamp (
			int newYear,
			int newMonth,
			int newDay,
			int newHour,
			int newMinute,
			int newSecond,
			int newTz) {

		year = newYear;
		month = newMonth;
		day = newDay;
		hour = newHour;
		minute = newMinute;
		second = newSecond;
		tz = newTz;

	}

	public static
	TimeStamp decode (
			ByteBuffer buf)
		throws PduDecodeException {

		return new TimeStamp (
			decodeOctet (buf),
			decodeOctet (buf),
			decodeOctet (buf),
			decodeOctet (buf),
			decodeOctet (buf),
			decodeOctet (buf),
			decodeOctet (buf));

	}

	public static
	int decodeOctet (
			ByteBuffer buf)
		throws PduDecodeException {

		int b =
			buf.get () & 0xff;

		int lowDigit =
			b & 0x0f;

		int highDigit =
			(b & 0xf0) >> 4;

		if (lowDigit > 9 || highDigit > 9) {

			throw new PduDecodeException (
				"Invalid octet: " + b);

		}

		return highDigit * 10 + lowDigit;

	}

	public
	int getDay () {
		return day;
	}

	public
	int getHour () {
		return hour;
	}

	public
	int getMinute () {
		return minute;
	}

	public
	int getMonth () {
		return month;
	}

	public
	int getSecond () {
		return second;
	}

	public
	int getTz () {
		return tz;
	}

	public
	int getYear () {
		return year;
	}

}
