package wbs.platform.event.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

@Accessors (fluent = true)
@Data
@DataClass ("event-types")
@PrototypeComponent ("eventTypesSpec")
public
class EventTypesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List<EventTypeSpec> eventTypes =
		new ArrayList<EventTypeSpec> ();

}
