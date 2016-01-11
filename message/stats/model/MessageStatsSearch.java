package wbs.sms.message.stats.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

@Accessors (fluent = true)
@Data
public
class MessageStatsSearch
	implements Serializable {

	LocalDate dateAfter;
	LocalDate dateBefore;

	Integer routeId;
	Integer serviceId;
	Integer affiliateId;
	Integer batchId;
	Integer networkId;

	Collection<Integer> routeIdIn;
	Collection<Integer> serviceIdIn;
	Collection<Integer> affiliateIdIn;
	Collection<Integer> batchIdIn;
	Collection<Integer> networkIdIn;

	Boolean filter = false;
	Collection<Integer> filterServiceIds;
	Collection<Integer> filterAffiliateIds;
	Collection<Integer> filterRouteIds;

}
