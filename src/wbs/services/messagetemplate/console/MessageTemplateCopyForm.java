package wbs.services.messagetemplate.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class MessageTemplateCopyForm {

	Long sourceMessageTemplateDatabaseId;

}