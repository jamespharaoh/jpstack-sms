package wbs.framework.object;

import static wbs.utils.etc.Misc.requiredValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelFieldType;
import wbs.framework.entity.record.Record;

@Accessors (fluent = true)
public
class ObjectModelImplementation <RecordType extends Record <RecordType>>
	implements ObjectModel <RecordType> {

	// properties

	@Getter @Setter
	Model <RecordType> model;

	// object model properties

	@Getter @Setter
	Long objectTypeId;

	@Getter @Setter
	String objectTypeCode;

	@Getter @Setter
	Long parentTypeId;

	@Setter
	Class <Record <?>> parentClass;

	@Getter @Setter
	Object daoImplementation;

	@Getter @Setter
	Class <?> daoInterface;

	@Getter @Setter
	ObjectHooks <RecordType> hooks;

	// model properties

	@Override
	public
	Class <? extends Record <?>> parentClassRequired () {

		return requiredValue (
			parentClass);

	}

	@Override
	public
	String objectName () {
		return model.objectName ();
	}

	@Override
	public
	String oldObjectName () {
		return model.oldObjectName ();
	}

	@Override
	public
	Class <RecordType> objectClass () {
		return model.objectClass ();
	}

	@Override
	public
	ModelField activeField () {
		return model.activeField ();
	}

	@Override
	public
	ModelField codeField () {
		return model.codeField ();
	}

	@Override
	public
	ModelField deletedField () {
		return model.deletedField ();
	}

	@Override
	public
	ModelField descriptionField () {
		return model.descriptionField ();
	}

	@Override
	public
	ModelField idField () {
		return model.idField ();
	}

	@Override
	public
	ModelField indexField () {
		return model.indexField ();
	}

	@Override
	public
	ModelField masterField () {
		return model.masterField ();
	}

	@Override
	public
	ModelField nameField () {
		return model.nameField ();
	}

	@Override
	public
	ModelField parentField () {
		return model.parentField ();
	}

	@Override
	public
	ModelField parentIdField () {
		return model.parentIdField ();
	}

	@Override
	public
	ModelField parentTypeField () {
		return model.parentTypeField ();
	}

	@Override
	public
	ModelField timestampField () {
		return model.timestampField ();
	}

	@Override
	public
	ModelField typeCodeField () {
		return model.typeCodeField ();
	}

	@Override
	public
	ModelField typeField () {
		return model.typeField ();
	}

	@Override
	public
	List <ModelField> fields () {
		return model.fields ();
	}

	@Override
	public
	Map <String, ModelField> fieldsByName () {
		return model.fieldsByName ();
	}

	@Override
	public
	ModelField field (
			@NonNull String name) {

		return model.field (
			name);

	}

	@Override
	public
	Boolean isRoot () {
		return model.isRoot ();
	}

	@Override
	public
	Boolean isRooted () {
		return model.isRooted ();
	}

	@Override
	public
	Boolean canGetParent () {
		return model.canGetParent ();
	}

	@Override
	public
	Boolean parentTypeIsFixed () {
		return model.parentTypeIsFixed ();
	}

	@Override
	public
	Boolean create () {
		return model.create ();
	}

	@Override
	public
	Record <?> getParentOrNull (
			@NonNull RecordType object) {

		return model.getParentOrNull (
			object);

	}

	@Override
	public
	Record<?> getParentType (
			@NonNull RecordType object) {

		return model.getParentType (
			object);

	}

	@Override
	public
	Long getParentId (
			@NonNull RecordType object) {

		return model.getParentId (
			object);

	}

	@Override
	public
	String getTypeCode (
			@NonNull RecordType record) {

		return model.getTypeCode (
			record);

	}

	@Override
	public
	String getCode (
			@NonNull RecordType record) {

		return model.getCode (
			record);

	}

	@Override
	public
	String getName (
			@NonNull RecordType record) {

		return model.getName (
			record);

	}

	@Override
	public
	String getDescription (
			@NonNull RecordType record) {

		return model.getDescription (
			record);

	}

	@Override
	public
	String tableName () {
		return model.tableName ();
	}

	@Override
	public
	Boolean mutable () {
		return model.mutable ();
	}

	@Override
	public
	Class <? extends ObjectHelper <?>> helperClass () {
		return model.helperClass ();
	}

	@Override
	public
	CachedViewSpec cachedView () {
		return model.cachedView ();
	}

	@Override
	public
	List <ModelField> identityFields () {
		return model.identityFields ();
	}

	@Override
	public
	Set <ModelFieldType> identityFieldTypes () {
		return model.identityFieldTypes ();
	}

}
