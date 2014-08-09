package wbs.sms.tracker.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class SmsSimpleTrackerRouteRec
	implements EphemeralRecord<SmsSimpleTrackerRouteRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	SmsSimpleTrackerRec smsSimpleTracker;

	@IdentityReferenceField
	RouteRec route;

	@Override
	public
	int compareTo (
			Record<SmsSimpleTrackerRouteRec> otherRecord) {

		SmsSimpleTrackerRouteRec other =
			(SmsSimpleTrackerRouteRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSmsSimpleTracker (),
				other.getSmsSimpleTracker ())

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
