package wbs.sms.message.stats.model;

import java.util.List;

public
interface MessageStatsDaoMethods {

	List<MessageStatsRec> search (
			MessageStatsSearch search);

}