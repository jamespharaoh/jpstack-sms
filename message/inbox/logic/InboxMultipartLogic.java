package wbs.sms.message.inbox.logic;

import java.util.Date;

import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface InboxMultipartLogic {

	/**
	 * Inserts part of a multipart message into the multipart buffer. If this is
	 * the last part then the parts are assembled and inserted into the inbox.
	 *
	 * @param route
	 * @param multipartId
	 * @param multipartSegMax
	 * @param multipartSeg
	 * @param msgTo
	 * @param msgFrom
	 * @param msgNetworkTime
	 * @param msgNetwork
	 * @param msgOtherId
	 * @param msgText
	 */
	InboxMultipartBufferRec insertInboxMultipart (
			RouteRec route,
			int multipartId,
			int multipartSegMax,
			int multipartSeg,
			String msgTo,
			String msgFrom,
			Date msgNetworkTime,
			NetworkRec msgNetwork,
			String msgOtherId,
			String msgText);

	boolean insertInboxMultipartMessage (
			InboxMultipartBufferRec inboxMultipartBuffer);

}
