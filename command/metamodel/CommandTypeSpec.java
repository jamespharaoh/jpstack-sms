package wbs.sms.command.metamodel;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelDataSpec;

import wbs.utils.string.StringFormat;

@Accessors (fluent = true)
@Data
@DataClass ("command-type")
@PrototypeComponent ("commandTypeSpec")
public
class CommandTypeSpec
	implements ModelDataSpec {

	@DataAttribute (
		format = StringFormat.hyphenated)
	String subject;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute (
		required = true)
	String description;

}
