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
@DataClass ("collections")
@PrototypeComponent ("modelCollectionsSpec")
public
class ModelCollectionsSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <ModelCollectionSpec> collections =
		new ArrayList<> ();

}
