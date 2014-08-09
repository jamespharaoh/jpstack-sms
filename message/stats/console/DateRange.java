package wbs.sms.message.stats.console;

import java.util.Date;

import lombok.NonNull;

public
class DateRange {

	// TODO use jodatime

	Date start;
	Date end;

	public
	DateRange (
			@NonNull Date newStart,
			@NonNull Date newEnd) {

		start =
			new Date (newStart.getTime ());

		end =
			new Date (newEnd.getTime ());

	}

	public
	Date getStart () {

		return new Date (
			start.getTime ());

	}

	public
	Date getEnd() {

		return new Date (
			end.getTime ());

	}

}
