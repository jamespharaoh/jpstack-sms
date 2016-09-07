package wbs.sms.message.core.console;

import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableSet;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.utils.TextualInterval;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (fluent = true)
@PrototypeComponent ("messageSourceImpl")
public
class MessageSourceImplementation
	implements MessageSource {

	// dependencies

	@Inject
	MessageObjectHelper messageHelper;

	@Getter @Setter
	MessageSearch searchTemplate;

	// implementation

	@Override
	public
	List<MessageRec> findMessages (
			Interval interval,
			ViewMode viewMode) {

		MessageSearch search =
			new MessageSearch (
				searchTemplate)

			.createdTime (
				TextualInterval.forInterval (
					DateTimeZone.UTC,
					interval))

			.orderBy (
				MessageSearchOrder.createdTime);

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

				.addAll (
					MessageStatus.goodStatus)

				.addAll (
					MessageStatus.badStatus)

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
