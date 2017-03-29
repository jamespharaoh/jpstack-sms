package wbs.sms.message.report.logic;

import com.google.common.base.Optional;

import org.joda.time.ReadableInstant;

import wbs.framework.logging.TaskLogger;

import wbs.sms.core.logic.NoSuchMessageException;
import wbs.sms.message.core.logic.InvalidMessageStateException;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsDeliveryReportLogic {

	void deliveryReport (
			TaskLogger parentTaskLogger,
			MessageRec message,
			MessageStatus newMessageStatus,
			Optional <String> theirCode,
			Optional <String> theirDescription,
			Optional <String> extraInformation,
			Optional <ReadableInstant> theirTimestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	MessageRec deliveryReport (
			TaskLogger parentTaskLogger,
			RouteRec route,
			String otherId,
			MessageStatus newMessageStatus,
			Optional <String> theirCode,
			Optional <String> theirDescription,
			Optional <String> extraInformation,
			Optional <ReadableInstant> timestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

	void deliveryReport (
			TaskLogger parentTaskLogger,
			Long messageId,
			MessageStatus newMessageStatus,
			Optional <String> theirCode,
			Optional <String> theirDescription,
			Optional <String> extraInformation,
			Optional <ReadableInstant> timestamp)
		throws
			NoSuchMessageException,
			InvalidMessageStateException;

}
