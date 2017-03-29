package wbs.sms.message.core.logic;

import lombok.NonNull;

import org.joda.time.DateTime;

import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHooks;

import wbs.sms.message.core.model.MessageRec;

public
class MessageHooks
	implements ObjectHooks<MessageRec> {

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull MessageRec message) {

		// set date

		if (message.getDate () == null) {

			if (message.getCreatedTime () == null)
				throw new RuntimeException ();

			message.setDate (
				new DateTime (
					message.getCreatedTime ()
				).toLocalDate ());

		}

	}

}