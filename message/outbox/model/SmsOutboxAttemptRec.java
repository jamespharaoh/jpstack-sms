package wbs.sms.message.outbox.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SmsOutboxAttemptRec
	implements CommonRecord<SmsOutboxAttemptRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MessageRec message;

	@IndexField
	Integer index;

	// details

	@SimpleField
	SmsOutboxAttemptState state;

	@ReferenceField
	RouteRec route;

	@SimpleField
	Instant startTime;

	@SimpleField (
		nullable = true)
	Instant endTime;

	// data

	@SimpleField (
		nullable = true)
	byte[] requestTrace;

	@SimpleField (
		nullable = true)
	byte[] responseTrace;

	@SimpleField (
		nullable = true)
	byte[] errorTrace;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsOutboxAttemptRec> otherRecord) {

		SmsOutboxAttemptRec other =
			(SmsOutboxAttemptRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getStartTime (),
				this.getStartTime ())

			.append (
				other.getId (),
				this.getId ())

			.toComparison ();

	}

}
