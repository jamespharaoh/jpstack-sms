package wbs.sms.message.outbox.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@Data
@MinorEntity (
	create = false,
	mutable = false)
public
class RouteOutboxSummaryRec
	implements MinorRecord<RouteOutboxSummaryRec> {

	// id

	@ForeignIdField (
		field = "route")
	Integer id;

	// identity

	@MasterField
	RouteRec route;

	// statistics

	@SimpleField
	Integer numMessages;

	@SimpleField
	Date oldestTime;

	// compare to

	@Override
	public
	int compareTo (
			Record<RouteOutboxSummaryRec> otherRecord) {

		RouteOutboxSummaryRec other =
			(RouteOutboxSummaryRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getRoute (),
				other.getRoute ())

			.toComparison ();

	}

}
