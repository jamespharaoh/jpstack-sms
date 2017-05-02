package wbs.sms.core.daemon;

import wbs.framework.database.Transaction;

import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

public
interface MessageRetrier {

	MessageRec messageRetry (
			Transaction parentTransaction,
			MessageRec retry,
			RouteRec rec,
			TextRec textRec);

}
