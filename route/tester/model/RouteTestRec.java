package wbs.sms.route.tester.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
@CommonEntity
public
class RouteTestRec
	implements CommonRecord<RouteTestRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	RouteTesterRec routeTester;

	@ReferenceField
	RouteRec route;

	// TODO index

	// details

	@SimpleField
	Date sentTime;

	@ReferenceField
	MessageRec sentMessage;

	@SimpleField (
		nullable = true)
	Date returnedTime;

	@ReferenceField (
		nullable = true)
	MessageRec returnedMessage;

	// compare to

	@Override
	public
	int compareTo (
			Record<RouteTestRec> otherRecord) {

		RouteTestRec other =
			(RouteTestRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
