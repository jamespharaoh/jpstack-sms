package wbs.sms.message.stats.model;

import java.util.List;

import wbs.sms.message.stats.model.MessageStatsRec.MessageStatsSearch;

public
interface MessageStatsDaoMethods {

	List<MessageStatsRec> search (
			MessageStatsSearch search);

}