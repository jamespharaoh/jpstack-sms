package wbs.framework.entity.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChild;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.entity.meta.cachedview.CachedViewSpec;
import wbs.framework.entity.record.Record;
import wbs.framework.object.ObjectHelper;

@Accessors (fluent = true)
@Data
@DataClass
public
class CompositeModelImplementation <DataType>
	implements
		ModelImplementationMethods <
			CompositeModelImplementation <DataType>,
			DataType
		>,
		CompositeModel <DataType> {

	// identity

	@DataAttribute
	Class <DataType> objectClass;

	@DataName
	String objectName;

	@DataAttribute
	String objectTypeCode;

	// fields

	@DataChildren
	List <ModelField> fields =
		new ArrayList<> ();

	@DataChildrenIndex
	Map <String, ModelField> fieldsByName =
		new LinkedHashMap<> ();

	@DataChild
	CachedViewSpec cachedView;

	// public impementation

	@Override
	public
	ModelField field (
			@NonNull String name) {

		return fieldsByName.get (
			name);

	}

	@Override
	public
	String oldObjectName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	String tableName () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField codeField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField deletedField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField descriptionField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField idField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField indexField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField nameField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField parentField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField parentIdField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField parentTypeField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField timestampField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField typeCodeField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean isRoot () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean isRooted () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean canGetParent () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean parentTypeIsFixed () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean create () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Boolean mutable () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Class <? extends Record <?>> parentClassRequired () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Class <? extends ObjectHelper <?>> helperClass () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	Record <?> getParentOrNull (
			@NonNull DataType object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Record <?> getParentType (
			@NonNull DataType object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Long getParentId (
			@NonNull DataType object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String getTypeCode (
			@NonNull DataType object) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String getCode (
			@NonNull DataType record) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String getName (
			@NonNull DataType record) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	String getDescription (
			@NonNull DataType record) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	ModelField masterField () {
		throw new UnsupportedOperationException ();
	}

	@Override
	public
	ModelField typeField () {
		throw new UnsupportedOperationException ();
	}

	// model implementation methods implementation

	@Override
	public
	CompositeModelImplementation <DataType> idField (
			@NonNull ModelField idField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> codeField (
			@NonNull ModelField codeField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> deletedField (
			@NonNull ModelField deletedField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> descriptionField (
			@NonNull ModelField descriptionField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> indexField (
			@NonNull ModelField indexField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> masterField (
			@NonNull ModelField masterField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> nameField (
			@NonNull ModelField nameField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> parentField (
			@NonNull ModelField parentField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> parentIdField (
			@NonNull ModelField parentIdField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> parentTypeField (
			@NonNull ModelField parentTypeField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> timestampField (
			@NonNull ModelField timestampField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> typeCodeField (
			@NonNull ModelField typeCodeField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	CompositeModelImplementation <DataType> typeField (
			@NonNull ModelField typeField) {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	List <ModelField> identityFields () {

		throw new UnsupportedOperationException ();

	}

	@Override
	public
	Set <ModelFieldType> identityFieldTypes () {

		throw new UnsupportedOperationException ();

	}

}
