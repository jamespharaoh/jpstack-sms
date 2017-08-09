package wbs.framework.entity.meta.cachedview;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;
import wbs.framework.entity.meta.model.ModelDataSpec;
import wbs.framework.entity.meta.model.RecordSpec;

@Accessors (fluent = true)
@Data
@EqualsAndHashCode (of = { "modelMeta" })
@ToString (of = { "sourceObjectName" })
@DataClass ("cached-view")
@PrototypeComponent ("cachedViewSpec")
public
class CachedViewSpec
	implements ModelDataSpec {

	@DataParent
	RecordSpec modelMeta;

	@DataAttribute (
		name = "source",
		required = true)
	String sourceObjectName;

	@DataChildren (
		childrenElement = "group-fields")
	List <CachedGroupFieldSpec> groupFields;

	@DataChildren (
		childrenElement = "aggregate-fields")
	List <CachedAggregateFieldSpec> aggregateFields;

}
