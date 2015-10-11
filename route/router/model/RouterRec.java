package wbs.sms.route.router.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
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
class RouterRec
	implements MinorRecord<RouterRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	// details

	@TypeField
	RouterTypeRec routerType;

	// compare to

	@Override
	public
	int compareTo (
			Record<RouterRec> otherRecord) {

		RouterRec other =
			(RouterRec) otherRecord;

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

	// dao methods

	public static
	interface RouterDaoMethods {

		List<RouterTypeRec> findByParentObjectType (
				ObjectTypeRec parentObjectType);

	}

}
