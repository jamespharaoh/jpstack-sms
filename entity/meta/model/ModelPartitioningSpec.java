package wbs.framework.entity.meta.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("partitioning")
@PrototypeComponent ("modelPartitioningSpec")
public
class ModelPartitioningSpec
	implements ModelDataSpec {

	@DataChildren (
		childrenElement = "partition-columns",
		childElement = "column",
		valueAttribute = "name")
	List <String> partitionColumns =
		new ArrayList<> ();

	@DataChildren (
		direct = true,
		childElement = "unique-index")
	List <ModelPartitioningUniqueIndexSpec> uniqueIndexes =
		new ArrayList<> ();

}
