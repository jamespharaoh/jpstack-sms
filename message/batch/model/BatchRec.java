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
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BatchRec
	implements CommonRecord<BatchRec> {

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@ReferenceField
	BatchSubjectRec subject;

	@Override
	public
	int compareTo (
			Record<BatchRec> otherRecord) {

		BatchRec other =
			(BatchRec) otherRecord;

		return new CompareToBuilder ()
			.append (getId (), other.getId ())
			.toComparison ();

	}

}
