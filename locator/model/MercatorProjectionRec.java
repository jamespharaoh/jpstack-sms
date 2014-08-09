package wbs.sms.locator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MercatorProjectionRec
	implements
		MajorRecord<MercatorProjectionRec>,
		MercatorProjection {

	@GeneratedIdField
	Integer id;

	@CodeField
	String code;

	@SimpleField
	String description;

	@ReferenceField
	BiaxialEllipsoidRec biaxialEllipsoid;

	@SimpleField
	Double scaleFactor;

	@SimpleField
	Double originLongitude;

	@SimpleField
	Double originLatitude;

	@SimpleField
	Double originEasting;

	@SimpleField
	Double originNorthing;

	// compare to

	@Override
	public
	int compareTo (
			Record<MercatorProjectionRec> otherRecord) {

		MercatorProjectionRec other =
			(MercatorProjectionRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
