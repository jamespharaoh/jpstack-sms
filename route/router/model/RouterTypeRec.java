package wbs.sms.route.router.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
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
@ToString (of = "id" )
@TypeEntity
public
class RouterTypeRec
	implements MinorRecord<RouterTypeRec> {

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

	// compare to

	@Override
	public
	int compareTo (
			Record<RouterTypeRec> otherRecord) {

		RouterTypeRec other =
			(RouterTypeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
