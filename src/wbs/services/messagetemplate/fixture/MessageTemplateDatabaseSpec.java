package wbs.services.messagetemplate.fixture;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("message-template-database")
public
class MessageTemplateDatabaseSpec {

	@DataAttribute
	String name;

	@DataAttribute
	String description;

	@DataChildren (
		direct = true)
	List<MessageTemplateEntryTypeSpec> entryTypes =
		new ArrayList<MessageTemplateEntryTypeSpec> ();

}
