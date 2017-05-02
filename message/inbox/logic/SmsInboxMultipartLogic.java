package wbs.sms.message.inbox.logic;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsInboxMultipartLogic {

	InboxMultipartBufferRec insertInboxMultipart (
			Transaction parentTransaction,
			RouteRec route,
			long multipartId,
			long multipartSegMax,
			long multipartSeg,
			String numTo,
			String numFrom,
			Optional <Instant> networkTime,
			Optional <NetworkRec> network,
			Optional <String> oherId,
			String message);

	boolean insertInboxMultipartMessage (
			Transaction parentTransaction,
			InboxMultipartBufferRec inboxMultipartBuffer);

}
