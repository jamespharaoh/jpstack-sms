package wbs.framework.entity.build;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.model.ModelField;
import wbs.framework.entity.model.ModelImplementationMethods;

@Accessors (fluent = true)
@Data
public
class ModelFieldBuilderTarget {

	ModelImplementationMethods <?, ?> model;

	List <ModelField> fields;
	Map <String, ModelField> fieldsByName;

}
