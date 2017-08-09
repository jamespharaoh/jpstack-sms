package wbs.sms.message.stats.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface MessageStatsDaoMethods {

	List <MessageStatsRec> search (
			Transaction parentTransaction,
			MessageStatsSearch search);

}