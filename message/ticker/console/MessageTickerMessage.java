package wbs.sms.message.ticker.console;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.Instant;

import lombok.Getter;
import lombok.experimental.Accessors;
import wbs.framework.entity.record.GlobalId;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (fluent = true)
public
class MessageTickerMessage {

	@Getter
	Long messageGeneration;

	@Getter
	Long statusGeneration;

	@Getter
	Long messageId;

	@Getter
	GlobalId routeGlobalId;

	@Getter
	GlobalId serviceParentGlobalId;

	@Getter
	GlobalId affiliateParentGlobalId;

	@Getter
	Instant createdTime;

	@Getter
	String numFrom;

	@Getter
	String numTo;

	@Getter
	String text;

	@Getter
	MessageDirection direction;

	@Getter
	MessageStatus status;

	@Getter
	long charge;

	@Getter
	List<Long> mediaIds =
		new ArrayList<> ();

	MessageTickerMessage () {
	}

	public
	MessageTickerMessage (
			MessageTickerMessage original) {

		messageGeneration =
			original.messageGeneration;

		statusGeneration =
			original.statusGeneration;

		messageId =
			original.messageId;

		routeGlobalId =
			original.routeGlobalId;

		serviceParentGlobalId =
			original.serviceParentGlobalId;

		affiliateParentGlobalId =
			original.affiliateParentGlobalId;

		createdTime =
			original.createdTime;

		numFrom =
			original.numFrom;

		numTo =
			original.numTo;

		text =
			original.text;

		direction =
			original.direction;

		status =
			original.status;

		charge =
			original.charge;

		mediaIds =
			original.mediaIds;

	}

}
