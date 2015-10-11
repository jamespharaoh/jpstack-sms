package wbs.sms.number.list.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class NumberListNumberRec
	implements EphemeralRecord<NumberListNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	NumberListRec numberList;

	@IdentityReferenceField
	NumberRec number;

	// state

	@SimpleField
	Boolean present = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberListNumberRec> otherRecord) {

		NumberListNumberRec other =
			(NumberListNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumberList (),
				other.getNumberList ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
