package wbs.sms.message.ticker.console;

import java.util.Collection;

import wbs.framework.database.Transaction;

public
interface MessageTickerManager {

	Collection <MessageTickerMessage> getMessages (
			Transaction parentTransaction);

}
