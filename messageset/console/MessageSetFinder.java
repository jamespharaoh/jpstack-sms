package wbs.sms.messageset.console;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.database.Transaction;

import wbs.sms.messageset.model.MessageSetRec;

public
interface MessageSetFinder {

	MessageSetRec findMessageSet (
			Transaction parentTransaction,
			ConsoleRequestContext requestContext);

}
