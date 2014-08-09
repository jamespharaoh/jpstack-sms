package wbs.sms.message.inbox.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class InboxMultipartBufferRec
	implements EphemeralRecord<InboxMultipartBufferRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	RouteRec route;

	@SimpleField
	Integer multipartId;

	@SimpleField
	Integer multipartSegMax;

	@SimpleField
	Integer multipartSeg;

	@SimpleField
	Date timestamp = new Date ();

	@SimpleField
	String msgTo;

	@SimpleField
	String msgFrom;

	@SimpleField
	Date msgNetworkTime;

	@ReferenceField
	NetworkRec msgNetwork;

	@SimpleField
	String msgOtherId;

	@SimpleField
	String msgText;

	// compare to

	@Override
	public
	int compareTo (
			Record<InboxMultipartBufferRec> otherRecord) {

		InboxMultipartBufferRec other =
			(InboxMultipartBufferRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	public static
	interface InboxMultipartBufferDaoMethods {

		List<InboxMultipartBufferRec> findByOtherId (
				RouteRec route,
				String otherId);

		List<InboxMultipartBufferRec> findRecent (
				InboxMultipartBufferRec inboxMultipartBuffer,
				Date timestamp);

	}

}
