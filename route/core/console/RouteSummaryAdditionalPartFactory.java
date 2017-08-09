package wbs.sms.route.core.console;

import wbs.console.part.PagePart;

import wbs.framework.database.Transaction;

public
interface RouteSummaryAdditionalPartFactory {

	String[] getSenderCodes ();

	PagePart getPagePart (
			Transaction parentTransaction,
			String senderCode);

}