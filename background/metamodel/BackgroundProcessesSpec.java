package wbs.platform.background.metamodel;

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
@DataClass ("background-processes")
@PrototypeComponent ("backgroundProcessesSpec")
public
class BackgroundProcessesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <BackgroundProcessSpec> backgroundProcesses =
		new ArrayList<> ();

}
