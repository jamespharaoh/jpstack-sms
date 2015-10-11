package wbs.sms.message.core.model;

import org.joda.time.DateTime;

import wbs.framework.object.AbstractObjectHooks;

public
class MessageHooks
	extends AbstractObjectHooks<MessageRec> {

	@Override
	public
	void beforeInsert (
			MessageRec message) {

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