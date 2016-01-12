package wbs.sms.message.inbox.logic;

import java.util.Date;

import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface InboxMultipartLogic {

	InboxMultipartBufferRec insertInboxMultipart (
			RouteRec route,
			long multipartId,
			long multipartSegMax,
			long multipartSeg,
			String msgTo,
			String msgFrom,
			Date msgNetworkTime,
			NetworkRec msgNetwork,
			String msgOtherId,
			String msgText);

	boolean insertInboxMultipartMessage (
			InboxMultipartBufferRec inboxMultipartBuffer);

}
