package wbs.sms.message.ticker.console;

import java.util.Collection;

public
interface MessageTickerManager {

	int getUpdateTimeMs ();

	void setUpdateTimeMs (
			int updateTimeMs);

	Collection<MessageTickerMessage> getMessages ();

}
