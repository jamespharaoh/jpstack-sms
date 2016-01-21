package wbs.sms.gazetteer.model;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataAttribute;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("entry")
public
class GazetteerEntryData {

	@DataAttribute
	String name;

	@DataAttribute
	String value;

	@DataAttribute
	Boolean canonical;

}
