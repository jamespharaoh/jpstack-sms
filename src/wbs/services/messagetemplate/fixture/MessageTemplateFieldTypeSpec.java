package wbs.services.messagetemplate.fixture;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.data.annotations.DataContent;
import wbs.framework.data.annotations.DataParent;

@Accessors (fluent = true)
@Data
@DataClass ("field")
public
class MessageTemplateFieldTypeSpec {

	@DataParent
	MessageTemplateEntryTypeSpec entryType;

	@DataAttribute (
		required = true)
	String name;

	@DataAttribute
	String description;

	@DataContent
	String value;

}
