package wbs.sms.spendlimit.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class SmsSpendLimiterNumberSearch
	implements Serializable {

	Long smsSpendLimiterId;

	String numberLike;

	/*
	Long totalSpend;
	LocalDate lastSpendDate;
	Long lastDailySpend;
	*/

}
