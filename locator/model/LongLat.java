package wbs.sms.locator.model;

import lombok.Value;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Value
public
class LongLat {

	Double longitude;
	Double latitude;

}
