package wbs.sms.messageset.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class MessageSetRec
	implements MinorRecord<MessageSetRec> {

	// fields

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@TypeField
	MessageSetTypeRec messageSetType;

	// collections

	// TODO change to list
	@CollectionField (
		index = "index",
		orderBy = "index")
	Map<Integer,MessageSetMessageRec> messages =
		new LinkedHashMap<Integer,MessageSetMessageRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageSetRec> otherRecord) {

		MessageSetRec other =
			(MessageSetRec) otherRecord;

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
