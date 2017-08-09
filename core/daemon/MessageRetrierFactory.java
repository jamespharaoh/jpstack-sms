package wbs.sms.core.daemon;

import java.util.Map;

public
interface MessageRetrierFactory {

	Map <String, MessageRetrier> getMessageRetriersByMessageTypeCode ();

}
