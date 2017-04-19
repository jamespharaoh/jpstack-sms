package wbs.sms.message.core.console;

import java.util.List;

import org.joda.time.Interval;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;

public
interface MessageSource {

	static
	enum ViewMode {
		all,
		in,
		out,
		sent,
		delivered,
		undelivered
	};

	List <MessageRec> findMessages (
			TaskLogger parentTaskLogger,
			Interval interval,
			ViewMode viewMode);

}
