package wbs.services.messagetemplate.fixture;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("entry")
public
class MessageTemplateEntryTypeSpec {

	@DataParent
	MessageTemplateDatabaseSpec messageTemplateDatabase;

	@DataAttribute
	String name;

	@DataAttribute
	String description;

	@DataChildren (
		direct = true,
		childElement = "field")
	List<MessageTemplateFieldTypeSpec> fieldTypes =
		new ArrayList<MessageTemplateFieldTypeSpec> ();

	@DataChildren (
		direct = true,
		childElement = "parameter")
	List<MessageTemplateParameterSpec> parameters =
		new ArrayList<MessageTemplateParameterSpec> ();

}
