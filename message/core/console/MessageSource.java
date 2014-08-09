package wbs.sms.message.core.console;

import java.util.Date;
import java.util.List;

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

	List<MessageRec> findMessages (
		Date start,
		Date end,
		ViewMode viewMode);

}
