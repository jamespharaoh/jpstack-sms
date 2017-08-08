package wbs.services.messagetemplate.api;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass
public
class MessageTemplateSetGetSuccess {

	@DataAttribute
	String status = "success";

	@DataAttribute
	Map<String,String> messages =
		new LinkedHashMap<String,String> ();

}
