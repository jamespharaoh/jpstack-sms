package wbs.sms.customer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SmsCustomerRec
	implements CommonRecord<SmsCustomerRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SmsCustomerManagerRec smsCustomerManager;

	@CodeField
	String code;

	// details

	@ReferenceField
	NumberRec number;

	@SimpleField
	Instant createdTime;

	// state

	@SimpleField (
		nullable = true)
	Instant lastActionTime;

	@ReferenceField (
		nullable = true)
	SmsCustomerSessionRec activeSession;

	// statistics

	@SimpleField
	Integer numSessions = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsCustomerRec> otherRecord) {

		SmsCustomerRec other =
			(SmsCustomerRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.append (
				getSmsCustomerManager (),
				other.getSmsCustomerManager ())

			.toComparison ();

	}

}
