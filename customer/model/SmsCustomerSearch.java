package wbs.sms.customer.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
public
class SmsCustomerSearch
	implements Serializable {

	Long smsCustomerManagerId;

	String numberLike;

}
