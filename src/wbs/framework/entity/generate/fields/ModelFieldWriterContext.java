package wbs.framework.entity.generate.fields;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.entity.meta.model.ModelMetaSpec;

@Accessors (fluent = true)
@Data
public
class ModelFieldWriterContext {

	ModelMetaSpec modelMeta;

	String recordClassName;

}
