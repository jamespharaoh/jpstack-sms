package wbs.sms.messageset.console;

import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.messageset.model.MessageSetRec;

public
interface MessageSetFinder {

	MessageSetRec findMessageSet (
			ConsoleRequestContext requestContext);

}
