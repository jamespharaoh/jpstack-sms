package wbs.sms.message.report.logic;

import org.joda.time.ReadableInstant;

import com.google.common.base.Optional;

import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.report.model.MessageReportCodeRec;
import wbs.sms.route.core.model.RouteRec;

public
interface ReportLogic {

	void deliveryReport (
			MessageRec message,
			Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	MessageRec deliveryReport (
			RouteRec route,
			String otherId,
			Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	void deliveryReport (
			int messageId,
			Optional<MessageStatus> newMessageStatus,
			ReadableInstant timestamp,
			MessageReportCodeRec messageReportCode)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

}
