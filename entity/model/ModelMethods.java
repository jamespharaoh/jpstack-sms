package wbs.framework.entity.model;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;
import java.util.Map;
import java.util.Set;

import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

public
interface ModelMethods <DataType> {

	// identity

	String objectName ();
	Class <DataType> objectClass ();
	String objectTypeCode ();

	String oldObjectName ();
	String tableName ();

	// fields

	ModelField activeField ();
	ModelField codeField ();
	ModelField deletedField ();
	ModelField descriptionField ();
	ModelField idField ();
	ModelField indexField ();
	ModelField masterField ();
	ModelField nameField ();
	ModelField parentField ();
	ModelField parentIdField ();
	ModelField parentTypeField ();
	ModelField timestampField ();
	ModelField typeCodeField ();
	ModelField typeField ();

	List <ModelField> fields ();
	Map <String, ModelField> fieldsByName ();

	List <ModelField> identityFields ();
	Set <ModelFieldType> identityFieldTypes ();

	ModelField field (
			String name);

	// misc parameters

	Boolean isRoot ();
	Boolean isRooted ();
	Boolean canGetParent ();
	Boolean parentTypeIsFixed ();

	Boolean create ();
	Boolean mutable ();

	Class <? extends Record <?>> parentClassRequired ();
	Class <? extends ObjectHelper <?>> helperClass ();

	// other information

	CachedViewSpec cachedView ();

	// property accessors

	Record <?> getParentOrNull (
			DataType object);

	default
	Record <?> getParentOrNullGeneric (
			Record <?> object) {

		return getParentOrNull (
			objectClass ().cast (
				object));

	}

	Record <?> getParentType (
			DataType object);

	default
	Record <?> getParentTypeGeneric (
			Record <?> object) {

		return genericCastUnchecked (
			objectClass ().cast (
				object));

	}

	Long getParentId (
			DataType object);

	String getTypeCode (
			DataType object);

	default
	String getTypeCodeGeneric (
			Record <?> object) {

		return getTypeCode (
			objectClass ().cast (
				object));

	}

	String getCode (
			DataType record);

	default
	String getCodeGeneric (
			Record <?> record) {

		return getCode (
			objectClass ().cast (
				record));

	}

	String getName (
			DataType record);

	String getDescription (
			DataType record);

}
