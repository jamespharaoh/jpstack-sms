package wbs.sms.message.core.model;

import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import wbs.framework.object.AbstractObjectHooks;

public
class MessageHooks
	extends AbstractObjectHooks<MessageRec> {

	// dependencies

	@Inject
	MessageDao messageDao;

	// implementation

	@Override
	public
	List<Integer> searchIds (
			Object search) {

		MessageSearch messageSearch =
			(MessageSearch) search;

		return messageDao.searchIds (
			messageSearch);

	}

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