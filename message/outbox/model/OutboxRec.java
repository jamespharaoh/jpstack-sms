package wbs.sms.message.outbox.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class OutboxRec
	implements EphemeralRecord<OutboxRec> {

	// id

	@ForeignIdField (
		field = "message")
	Integer id;

	// identity

	@MasterField
	MessageRec message;

	// details

	@ReferenceField
	RouteRec route;

	@SimpleField
	Date createdTime;

	@SimpleField
	Integer pri = 0;

	// state

	@SimpleField
	Date retryTime = new Date ();

	@SimpleField
	Integer tries = 0;

	@SimpleField (
		nullable = true)
	Integer remainingTries;

	@SimpleField (
		nullable = true)
	String error;

	@SimpleField
	Boolean dailyFailure = false;

	@SimpleField (
		nullable = true)
	Date sending;

	// compare to

	@Override
	public
	int compareTo (
			Record<OutboxRec> otherRecord) {

		OutboxRec other =
			(OutboxRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
