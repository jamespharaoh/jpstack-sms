package wbs.framework.entity.meta.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("unique-index")
@PrototypeComponent ("modelPartitioningUniqueIndexSpec")
public
class ModelPartitioningUniqueIndexSpec
	implements ModelDataSpec {

	@DataParent
	ModelPartitioningSpec partitioning;

	@DataAttribute
	String name;

	@DataChildren (
		direct = true,
		childElement = "column",
		valueAttribute = "name")
	List <String> columnsNames =
		new ArrayList<> ();

}
