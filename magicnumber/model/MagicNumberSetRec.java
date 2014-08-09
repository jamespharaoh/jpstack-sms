package wbs.sms.magicnumber.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;
import wbs.platform.scaffold.model.SliceRec;
import wbs.sms.number.format.model.NumberFormatRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class MagicNumberSetRec
	implements MajorRecord<MagicNumberSetRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SliceRec slice;

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	@DeletedField
	Boolean deleted = false;

	// settings

	@ReferenceField (
		nullable = true)
	NumberFormatRec numberFormat;

	// compare to

	@Override
	public
	int compareTo (
			Record<MagicNumberSetRec> otherRecord) {

		MagicNumberSetRec other =
			(MagicNumberSetRec) otherRecord;

		return new CompareToBuilder ()
			.append (getSlice (), other.getSlice ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
