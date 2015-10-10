package wbs.sms.route.core.console;

import wbs.console.part.PagePart;

public
interface RouteSummaryAdditionalPartFactory {

	String[] getSenderCodes ();

	PagePart getPagePart (
			String senderCode);

}