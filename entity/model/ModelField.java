package wbs.framework.entity.model;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataChildrenIndex;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataName;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@ToString (of = { "name" })
@DataClass
public
class ModelField {

	@DataAncestor
	Model <?> model;

	@DataParent
	ModelField parentField;

	@DataName
	String name;

	@DataAttribute
	String label;

	@DataAttribute
	ModelFieldType type;

	@DataAttribute
	boolean parent;

	@DataAttribute
	boolean identity;

	@DataChildren
	List <String> columnNames =
		new ArrayList<> ();

	@DataAttribute
	List <String> columnSqlTypes =
		new ArrayList<> ();

	@DataAttribute
	Class <?> valueType;

	@DataAttribute
	ParameterizedType parameterizedType;

	@DataAttribute
	Class <?> collectionKeyType;

	@DataAttribute
	Class <?> collectionValueType;

	@DataAttribute
	Class <?> hibernateTypeHelper;

	@DataAttribute
	Boolean nullable;

	@DataAttribute
	Boolean cacheable;

	@DataAttribute
	String foreignFieldName;

	@DataAttribute
	String sequenceName;

	@DataAttribute
	String orderSql;

	@DataAttribute
	String whereSql;

	@DataAttribute
	Boolean owned;

	@DataAttribute
	String joinColumnName;

	@DataAttribute
	String foreignColumnName;

	@DataAttribute
	String listIndexColumnName;

	@DataAttribute
	String mappingKeyColumnName;

	@DataAttribute
	String associationTableName;

	@DataAttribute
	String valueColumnName;

	@DataAttribute
	String indexCounterFieldName;

	@DataChildren
	List <ModelField> fields =
		new ArrayList<> ();

	@DataChildrenIndex
	Map <String, ModelField> fieldsByName =
		new LinkedHashMap<> ();

	//Field field;
	//Annotation annotation;

	public
	String columnName () {

		if (columnNames.size () != 1) {

			throw new RuntimeException (
				stringFormat (
					"Field %s has %s columns",
					fullName (),
					integerToDecimalString (
						columnNames.size ())));

		}

		return columnNames.get (0);

	}

	public
	boolean id () {

		return enumInSafe (
			type,
			ModelFieldType.generatedId,
			ModelFieldType.assignedId,
			ModelFieldType.foreignId,
			ModelFieldType.compositeId);

	}

	public
	boolean value () {

		return enumInSafe (
			type,
			ModelFieldType.active,
			ModelFieldType.assignedId,
			ModelFieldType.code,
			ModelFieldType.deleted,
			ModelFieldType.description,
			ModelFieldType.foreignId,
			ModelFieldType.generatedId,
			ModelFieldType.identitySimple,
			ModelFieldType.index,
			ModelFieldType.name,
			ModelFieldType.parentId,
			ModelFieldType.simple,
			ModelFieldType.typeCode);

	}

	public
	boolean reference () {

		return enumInSafe (
			type,
			ModelFieldType.reference,
			ModelFieldType.parent,
			ModelFieldType.grandParent,
			ModelFieldType.greatGrandParent,
			ModelFieldType.parentType,
			ModelFieldType.type,
			ModelFieldType.identityReference);

	}

	public
	boolean composite () {

		return enumInSafe (
			type,
			ModelFieldType.compositeId,
			ModelFieldType.component);

	}

	public
	boolean generatedId () {

		return enumInSafe (
			type,
			ModelFieldType.generatedId);

	}

	public
	boolean assignedId () {

		return enumInSafe (
			type,
			ModelFieldType.assignedId);

	}

	public
	boolean foreignId () {

		return enumInSafe (
			type,
			ModelFieldType.foreignId);

	}

	public
	boolean partner () {

		return enumInSafe (
			type,
			ModelFieldType.master,
			ModelFieldType.slave);

	}

	public
	boolean collection () {

		return enumInSafe (
			type,
			ModelFieldType.collection);

	}

	public
	boolean link () {

		return enumInSafe (
			type,
			ModelFieldType.associative);

	}

	public
	boolean compositeId () {

		return enumInSafe (
			type,
			ModelFieldType.compositeId);

	}

	public
	boolean component () {

		return enumInSafe (
			type,
			ModelFieldType.component);

	}

	public
	String fullName () {

		if (parentField != null) {

			return stringFormat (
				"%s.%s",
				parentField.fullName (),
				name ());

		} else {

			return stringFormat (
				"%s.%s",
				model.objectName (),
				name ());

		}

	}

}
