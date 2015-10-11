package wbs.sms.message.inbox.model;

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
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class InboxMultipartLogRec
	implements CommonRecord<InboxMultipartLogRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	RouteRec route;

	@SimpleField
	String msgFrom;

	@SimpleField
	Integer multipartId;

	@SimpleField
	Integer multipartSegMax;

	@SimpleField
	Date timestamp = new Date ();

	// compare to

	@Override
	public
	int compareTo (
			Record<InboxMultipartLogRec> otherRecord) {

		InboxMultipartLogRec other =
			(InboxMultipartLogRec) otherRecord;

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
