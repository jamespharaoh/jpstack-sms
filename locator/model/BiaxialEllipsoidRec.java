package wbs.sms.locator.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class BiaxialEllipsoidRec
	implements
		BiaxialEllipsoid,
		MajorRecord<BiaxialEllipsoidRec> {

	@GeneratedIdField
	Integer id;

	@CodeField
	String code;

	@SimpleField
	String description;

	@SimpleField
	Double semiMajorAxisA;

	@SimpleField
	Double semiMajorAxisB;

	// compare to

	@Override
	public
	int compareTo (
			Record<BiaxialEllipsoidRec> otherRecord) {

		BiaxialEllipsoidRec other =
			(BiaxialEllipsoidRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
