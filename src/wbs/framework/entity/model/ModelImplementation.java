package wbs.framework.entity.model;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataReference;
import wbs.framework.entity.record.Record;
import wbs.framework.entity.record.RootRecord;
import wbs.framework.object.ObjectHelper;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@Data
@DataClass
public
class ModelImplementation
	implements Model {

	// identity

	@DataAttribute
	Class<? extends Record<?>> objectClass;

	@DataName
	String objectName;

	@DataAttribute
	String objectTypeCode;

	// database stuff

	@DataAttribute
	String tableName;

	@DataAttribute
	Boolean create;

	@DataAttribute
	Boolean mutable;

	// fields

	@DataReference
	ModelField idField;

	@DataReference
	ModelField timestampField;

	@DataReference
	ModelField parentField;

	@DataReference
	ModelField parentTypeField;

	@DataReference
	ModelField parentIdField;

	@DataReference
	ModelField masterField;

	@DataReference
	ModelField typeCodeField;

	@DataReference
	ModelField codeField;

	@DataReference
	ModelField indexField;

	@DataReference
	ModelField nameField;

	@DataReference
	ModelField descriptionField;

	@DataReference
	ModelField deletedField;

	@DataReference
	ModelField typeField;

	@DataChildren
	List<ModelField> fields =
		new ArrayList<ModelField> ();

	@DataChildrenIndex
	Map<String,ModelField> fieldsByName =
		new LinkedHashMap<String,ModelField> ();

	// helper

	@DataAttribute
	Class<? extends ObjectHelper<?>> helperClass;

	// methods

	public
	Long getId (
			@NonNull Record<?> object) {

		return object.getId ();

	}

	@Override
	public
	String getTypeCode (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				typeCodeField.name ());

	}

	@Override
	public
	String getCode (
			@NonNull Record<?> object) {

		if (codeField != null) {

			return (String)
				BeanLogic.getProperty (
					object,
					codeField.name ());

		}

		return Long.toString (
			getId (
				object));

	}

	@Override
	public
	String getName (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				nameField.name ());

	}

	@Override
	public
	String getDescription (
			@NonNull Record<?> object) {

		return (String)
			BeanLogic.getProperty (
				object,
				descriptionField.name ());

	}

	@Override
	public
	Boolean isRoot () {

		return RootRecord.class.isAssignableFrom (
			objectClass ());

	}

	@Override
	public
	Boolean isRooted () {

		return ! isRoot ()
			&& parentTypeField == null
			&& parentField == null;

	}

	@Override
	public
	Boolean canGetParent () {

		return parentField != null;

	}

	@Override
	public
	Boolean parentTypeIsFixed () {

		return parentTypeField == null;

	}

	@Override
	public
	Record<?> getParent (
			@NonNull Record<?> object) {

		if (! canGetParent ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent for %s",
					objectName ()));

		}

		return (Record<?>)
			BeanLogic.getProperty (
				object,
				parentField.name ());

	}

	@Override
	public
	Long getParentId (
			@NonNull Record<?> object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent id for %s",
					objectName ()));

		}

		return (Long)
			BeanLogic.getProperty (
				object,
				parentIdField.name ());

	}

	@Override
	public
	Record<?> getParentType (
			@NonNull Record <?> object) {

		if (parentTypeIsFixed ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent type for %s",
					objectName ()));

		}

		return (Record <?>)
			BeanLogic.getProperty (
				object,
				parentTypeField.name ());

	}

	@Override
	public
	Class <? extends Record <?>> parentClass () {

		if (parentTypeField != null) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectName ()));

		} else if (parentField != null) {

			@SuppressWarnings ("unchecked")
			Class<? extends Record<?>> classTemp =
				(Class<? extends Record<?>>)
				parentField.valueType ();

			return classTemp;

		} else if (isRoot ()) {

			throw new UnsupportedOperationException (
				stringFormat (
					"Can't get parent class for %s",
					objectName ()));

		} else if (isRooted ()) {

			throw new RuntimeException ("TODO");

		} else {

			throw new RuntimeException ();

		}

	}

	@Override
	public
	ModelField field (
			String name) {

		return fieldsByName.get (name);

	}

}
