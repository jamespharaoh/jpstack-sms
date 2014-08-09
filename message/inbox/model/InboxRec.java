package wbs.sms.message.inbox.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class InboxRec
	implements EphemeralRecord<InboxRec> {

	@ForeignIdField (
		field = "message")
	Integer id;

	@MasterField
	MessageRec message;

	@SimpleField
	Integer tries = 0;

	@SimpleField
	Date retryTime = new Date ();

	// compare to

	@Override
	public
	int compareTo (
			Record<InboxRec> otherRecord) {

		InboxRec other =
			(InboxRec) otherRecord;

		return new CompareToBuilder ()
			.append (
				other.getMessage ().getCreatedTime (),
				getMessage ().getCreatedTime ())
			.append (
				other.getId (),
				getId ())
			.toComparison ();

	}

	// dao methods

	public static
	interface InboxDaoMethods {

		int count ();

		List<InboxRec> findRetryLimit (
				int maxResults);

		List<InboxRec> findAllLimit (
				int maxResults);

	}

}
