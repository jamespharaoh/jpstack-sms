package wbs.sms.message.inbox.logic;

import java.util.List;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsInboxLogic {

	MessageRec inboxInsert (
			Optional <String> otherId,
			TextRec text,
			NumberRec numFrom,
			String numTo,
			RouteRec route,
			Optional <NetworkRec> network,
			Optional <Instant> networkTime,
			List <MediaRec> medias,
			Optional <String> avStatus,
			Optional <String> subject);

	InboxAttemptRec inboxProcessed (
			InboxRec inbox,
			Optional<ServiceRec> service,
			Optional<AffiliateRec> affiliate,
			CommandRec command);

	InboxAttemptRec inboxNotProcessed (
			InboxRec inbox,
			Optional<ServiceRec> service,
			Optional<AffiliateRec> affiliate,
			Optional<CommandRec> command,
			String statusMessage);

	InboxAttemptRec inboxProcessingFailed (
			InboxRec inbox,
			String statusMessage);

}
