package wbs.sms.message.batch.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.entity.annotations.TypeEntity;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@TypeEntity
public
class BatchTypeRec
	implements MinorRecord<BatchTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ObjectTypeRec subjectObjectType;

	@CodeField
	String code;

	// details

	@ReferenceField
	ObjectTypeRec batchObjectType;

	@SimpleField
	String description;

	// compare to

	@Override
	public
	int compareTo (
			Record<BatchTypeRec> otherRecord) {

		BatchTypeRec other =
			(BatchTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getSubjectObjectType (),
				other.getSubjectObjectType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
