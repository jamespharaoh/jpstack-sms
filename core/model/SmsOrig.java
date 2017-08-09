package wbs.sms.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@EqualsAndHashCode
@ToString
public
class SmsOrig {

	@Getter
	SmsOrigType type;

	@Getter
	String value;

	public
	SmsOrig (
			SmsOrigType type,
			String value) {

		this.type =
			type;

		this.value =
			value;

	}

}
