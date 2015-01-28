package wbs.sms.message.inbox.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class InboxRec
	implements CommonRecord<InboxRec> {

	// id

	@ForeignIdField (
		field = "message")
	Integer id;

	// identity

	@MasterField
	MessageRec message;

	// details

	@SimpleField
	Instant createdTime;

	// statistics

	@SimpleField
	Integer numAttempts = 0;

	// state

	@SimpleField
	InboxState state;

	@SimpleField (
		nullable = true)
	Instant nextAttempt;

	@SimpleField (
		nullable = true)
	String statusMessage;

	// compare to

	@Override
	public
	int compareTo (
			Record<InboxRec> otherRecord) {

		InboxRec other =
			(InboxRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface InboxDaoMethods {

		int countPending ();

		List<InboxRec> findPendingLimit (
				Instant now,
				int maxResults);

		List<InboxRec> findPendingLimit (
				int maxResults);

	}

}
