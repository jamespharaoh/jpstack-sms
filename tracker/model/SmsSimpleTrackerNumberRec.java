package wbs.sms.tracker.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class SmsSimpleTrackerNumberRec
	implements CommonRecord<SmsSimpleTrackerNumberRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	SmsSimpleTrackerRec smsSimpleTracker;

	@ReferenceField
	NumberRec number;

	@SimpleField (
		nullable = true)
	Date lastScan;

	@SimpleField
	Boolean blocked;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsSimpleTrackerNumberRec> otherRecord) {

		SmsSimpleTrackerNumberRec other =
			(SmsSimpleTrackerNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.append (
				getSmsSimpleTracker (),
				other.getSmsSimpleTracker ())

			.append (
				getId (),
				other.getId ())

			.toComparison ();


	}

}
