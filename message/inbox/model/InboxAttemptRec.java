package wbs.sms.message.inbox.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class InboxAttemptRec
	implements CommonRecord<InboxAttemptRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	InboxRec inbox;

	@IndexField
	Integer index;

	// details

	@SimpleField
	Instant timestamp;

	@SimpleField
	Boolean success;

	// compare to

	@Override
	public
	int compareTo (
			Record<InboxAttemptRec> otherRecord) {

		InboxAttemptRec other =
			(InboxAttemptRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getTimestamp (),
				getTimestamp ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
