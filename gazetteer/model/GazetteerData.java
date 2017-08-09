package wbs.sms.gazetteer.model;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

@Accessors (fluent = true)
@Data
@DataClass ("gazetteer")
public
class GazetteerData {

	@DataChildren (
		direct = true,
		childElement = "entry")
	List<GazetteerEntryData> entries;

}
