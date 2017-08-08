package wbs.platform.event.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

@Accessors (fluent = true)
@Data
@DataClass ("event-type")
@PrototypeComponent ("eventTypeSpec")
public
class EventTypeSpec
	implements ModelDataSpec {

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String text;

	@DataAttribute (
		required = true)
	Boolean admin;

}
