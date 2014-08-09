package wbs.sms.messageset.model;

import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
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
class MessageSetTypeRec
	implements MinorRecord<MessageSetTypeRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	ObjectTypeRec parentObjectType;

	@CodeField
	String code;

	// details

	@SimpleField
	String description;

	// children

	@CollectionField (
		key = "type_id")
	Set<MessageSetRec> messageSets;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageSetTypeRec> otherRecord) {

		MessageSetTypeRec other =
			(MessageSetTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// dao methods

	public static
	interface MessageSetTypeDaoMethods {

		List<MessageSetTypeRec> findByParentObjectType (
				ObjectTypeRec parentObjectType);

	}

}
