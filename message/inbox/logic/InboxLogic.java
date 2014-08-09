package wbs.sms.message.inbox.logic;

import java.util.Date;
import java.util.List;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface InboxLogic {

	MessageRec inboxInsert (
			String otherId,
			TextRec text,
			Object numFrom,
			String numTo,
			RouteRec route,
			NetworkRec network,
			Date networkTime,
			List<MediaRec> medias,
			String avStatus,
			String subject);

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
			InboxMultipartBufferRec imb);

	void inboxProcessed (
			MessageRec message,
			ServiceRec service,
			AffiliateRec affiliate,
			CommandRec command);

	void inboxNotProcessed (
			MessageRec message,
			ServiceRec service,
			AffiliateRec affiliate,
			CommandRec command,
			String information);

}
