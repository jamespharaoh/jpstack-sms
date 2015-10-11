package wbs.sms.message.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.LinkField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.SlaveField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.media.model.MediaRec;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.model.UserRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.delivery.model.DeliveryTypeRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.report.model.MessageReportRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MessageRec
	implements CommonRecord<MessageRec> {

	@GeneratedIdField
	Integer id;

	@SimpleField (
		column = "thread_message_id")
	Integer threadId;

	@SimpleField (
		nullable = true)
	String otherId;

	@ReferenceField
	TextRec text;

	@SimpleField
	String numFrom;

	@SimpleField
	String numTo;

	@SimpleField
	MessageDirection direction;

	@SimpleField
	MessageStatus status;

	@ReferenceField
	NumberRec number;

	@ReferenceField
	RouteRec route;

	@ReferenceField
	ServiceRec service;

	@ReferenceField
	NetworkRec network;

	@ReferenceField
	BatchRec batch;

	@ReferenceField
	AffiliateRec affiliate;

	@SimpleField
	LocalDate date;

	@SimpleField
	Date createdTime;

	@SimpleField (
		nullable = true)
	Date processedTime;

	@SimpleField (
		nullable = true)
	Date networkTime;

	@SimpleField
	Integer charge;

	@SlaveField
	OutboxRec outbox;

	@SimpleField
	Integer pri = 0;

	@ReferenceField (
		nullable = true)
	DeliveryTypeRec deliveryType;

	@ReferenceField (
		nullable = true)
	TextRec subjectText;

	@SimpleField (
		nullable = true)
	Integer ref;

	@ReferenceField
	MessageTypeRec messageType;

	@ReferenceField (
		nullable = true)
	UserRec user;

	@LinkField (
		table = "message_media",
		index = "i")
	List<MediaRec> medias =
		new ArrayList<MediaRec> ();

	@CollectionField
	Set<MessageReportRec> reports =
		new HashSet<MessageReportRec> ();

	@LinkField (
		table = "message_tag",
		element = "tag")
	Set<String> tags =
		new HashSet<String> ();

	@ReferenceField (
		nullable = true)
	CommandRec command;

	// TODO look into this
	@SimpleField (
		nullable = true)
	String adultVerified;

	// TODO should this be here?
	@SimpleField (
		nullable = true)
	String notes;

	@SimpleField (
		nullable = true)
	Integer numAttempts;

	@ReferenceField (
		nullable = true)
	QueueItemRec notProcessedQueueItem;

	// TODO move this elsewhere
	public
	void setId (
			Integer newId) {

		id = newId;

		if (threadId == null)
			threadId = newId;

	}

	@Override
	public
	int compareTo (
			Record<MessageRec> otherRecord) {

		MessageRec other =
			(MessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	public final static
	Comparator<MessageRec> createdTimeComparator =
		new Comparator<MessageRec>() {

		@Override
		public
		int compare (
				MessageRec left,
				MessageRec right) {

			return new CompareToBuilder ()

				.append (
					left.getCreatedTime (),
					right.getCreatedTime ())

				.toComparison ();

		}

	};

	// dao methods

	public static
	interface MessageDaoMethods {

		MessageRec findByOtherId (
				MessageDirection direction,
				RouteRec route,
				String otherId);

		List<MessageRec> findByThreadId (
				int threadId);

		List<MessageRec> findNotProcessed ();

		List<MessageRec> findRecentLimit (
				int maxResults);

		int countNotProcessed ();

		List<ServiceRec> projectServices (
				NumberRec number);

		List<MessageRec> search (
				MessageSearch search);

	}

}