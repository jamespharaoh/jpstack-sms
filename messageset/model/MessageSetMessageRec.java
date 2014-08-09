package wbs.sms.messageset.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class MessageSetMessageRec
	implements EphemeralRecord<MessageSetMessageRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	MessageSetRec messageSet;

	@IndexField
	Integer index;

	// settings

	@ReferenceField
	RouteRec route;

	@SimpleField
	String number;

	@SimpleField
	String message;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageSetMessageRec> otherRecord) {

		MessageSetMessageRec other =
			(MessageSetMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMessageSet (),
				other.getMessage ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

}
