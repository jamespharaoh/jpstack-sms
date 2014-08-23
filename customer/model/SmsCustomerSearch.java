package wbs.sms.customer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class SmsCustomerSearch {
	Integer smsCustomerManagerId;
	String numberLike;
}
