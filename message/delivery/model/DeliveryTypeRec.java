package wbs.sms.message.delivery.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.TypeEntity;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@TypeEntity
public
class DeliveryTypeRec
	implements MajorRecord<DeliveryTypeRec> {

	@GeneratedIdField
	Integer id;

	@CodeField
	String code;

	@SimpleField
	String description;

	// compare to

	@Override
	public
	int compareTo (
			Record<DeliveryTypeRec> otherRecord) {

		DeliveryTypeRec other =
			(DeliveryTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
