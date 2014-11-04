package wbs.sms.tracker.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SmsSimpleTrackerRec
	implements MajorRecord<SmsSimpleTrackerRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	@DescriptionField
	String description;

	@ReferenceField
	SmsTrackerRec defaultSmsTracker;

	@SimpleField
	Integer failureTotalSecsMin;

	@SimpleField
	Integer failureCountMin;

	@SimpleField
	Integer failureSingleSecsMin;

	@SimpleField
	Integer sinceScanSecsMax;

	@CollectionField
	Set<SmsSimpleTrackerRouteRec> smsSimpleTrackerRoutes =
		new LinkedHashSet<SmsSimpleTrackerRouteRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsSimpleTrackerRec> otherRecord) {

		SmsSimpleTrackerRec other =
			(SmsSimpleTrackerRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSlice (),
				other.getSlice ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// dao methods

	public static
	interface SmsSimpleTrackerDaoMethods {

		List<MessageRec> findMessages (
				SmsSimpleTrackerRec smsSimpleTracker,
				NumberRec number);

	}

}
