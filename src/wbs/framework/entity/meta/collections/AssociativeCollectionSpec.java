package wbs.framework.entity.meta.collections;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAncestor;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelCollectionSpec;
import wbs.framework.entity.meta.model.ModelMetaData;
import wbs.framework.entity.meta.model.RecordSpec;

@Accessors (fluent = true)
@Data
@DataClass ("associative-collection")
@PrototypeComponent ("associativeCollectionSpec")
public
class AssociativeCollectionSpec
	implements ModelCollectionSpec {

	@DataAncestor
	RecordSpec model;

	@DataAttribute
	String name;

	@DataAttribute (
		name = "type",
		required = true)
	String typeName;

	@DataAttribute (
		name = "table",
		required = true)
	String tableName;

	@DataAttribute (
		name = "join-column")
	String joinColumnName;

	@DataAttribute (
		name = "foreign-column")
	String foreignColumnName;

	@DataAttribute (
		name = "value-column")
	String valueColumnName;

	@DataAttribute
	String whereSql;

	@DataAttribute
	String orderSql;

	@DataAttribute
	Boolean owned;

}
