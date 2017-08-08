package wbs.sms.route.router.metamodel;

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
@DataClass ("router-types")
@PrototypeComponent ("routerTypesSpec")
public
class RouterTypesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <RouterTypeSpec> routerTypes =
		new ArrayList<> ();

}
