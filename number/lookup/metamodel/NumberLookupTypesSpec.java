package wbs.sms.number.lookup.metamodel;

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
@DataClass ("number-lookup-types")
@PrototypeComponent ("numberLookupTypesSpec")
public
class NumberLookupTypesSpec
	implements ModelDataSpec {

	@DataChildren (
		direct = true)
	List <NumberLookupTypeSpec> numberLookupTypes =
		new ArrayList<> ();

}
