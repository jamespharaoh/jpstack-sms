package wbs.sms.message.core.model;

import lombok.NonNull;

import org.joda.time.DateTime;

import wbs.framework.object.AbstractObjectHooks;

public
class MessageHooks
	extends AbstractObjectHooks<MessageRec> {

	// implementation

	@Override
	public
	void beforeInsert (
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