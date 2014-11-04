package wbs.sms.message.batch.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BatchSubjectRec
	implements MinorRecord<BatchSubjectRec> {

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@ReferenceField
	BatchTypeRec type;

	// compare to

	@Override
	public
	int compareTo (
			Record<BatchSubjectRec> otherRecord) {

		BatchSubjectRec other =
			(BatchSubjectRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getParentObjectId (),
				other.getParentObjectId ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}