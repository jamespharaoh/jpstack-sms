package wbs.sms.customer.console;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SmsCustomerEndSessionForm {

	String reason;

}
