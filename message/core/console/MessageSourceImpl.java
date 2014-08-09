package wbs.sms.message.core.console;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.message.core.model.MessageStatus;

import com.google.common.collect.ImmutableSet;

@Accessors (fluent = true)
@PrototypeComponent ("messageSourceImpl")
public
class MessageSourceImpl
	implements MessageSource {

	@Inject
	MessageObjectHelper messageHelper;

	@Getter @Setter
	MessageSearch searchTemplate;

	@Override
	public
	List<MessageRec> findMessages (
			Date start,
			Date end,
			ViewMode viewMode) {

		MessageSearch search =
			new MessageSearch (searchTemplate)
				.createdTimeAfter (start)
				.createdTimeBefore (end)
				.orderBy (MessageSearchOrder.createdTime);

		switch (viewMode) {

		case in:

			search.direction (
				MessageDirection.in);

			break;

		case out:

			search.direction (
				MessageDirection.out);

			break;

		case sent:

			search.direction (
				MessageDirection.out);

			search.statusNotIn (
				ImmutableSet.<MessageStatus>builder ()
					.addAll (MessageStatus.goodStatus)
					.addAll (MessageStatus.badStatus)
					.build ());

			break;

		case delivered:

			search.direction (
				MessageDirection.out);

			search.statusIn (
				MessageStatus.goodStatus);

			break;

		case undelivered:

			search.direction (
				MessageDirection.out);

			search.statusIn (
				MessageStatus.badStatus);

			break;

		default:

			// do nothing

		}

		return messageHelper.search (
			search);

	}

}
