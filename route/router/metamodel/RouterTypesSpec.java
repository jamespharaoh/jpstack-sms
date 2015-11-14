package wbs.sms.route.router.metamodel;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("router-types")
@PrototypeComponent ("routerTypesSpec")
@ModelMetaData
public
class RouterTypesSpec {

	@DataChildren (
		direct = true)
	List<RouterTypeSpec> routerTypes =
		new ArrayList<RouterTypeSpec> ();

}
