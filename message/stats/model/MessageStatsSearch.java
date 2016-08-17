package wbs.sms.message.stats.model;

import java.io.Serializable;
import java.util.Collection;

import org.joda.time.LocalDate;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class MessageStatsSearch
	implements Serializable {

	LocalDate dateAfter;
	LocalDate dateBefore;

	Collection<Long> routeIdIn;
	Collection<Long> serviceIdIn;
	Collection<Long> affiliateIdIn;
	Collection<Long> batchIdIn;
	Collection<Long> networkIdIn;

	Boolean filter = false;

	Collection<Long> filterServiceIds;
	Collection<Long> filterAffiliateIds;
	Collection<Long> filterRouteIds;

	Boolean group = false;

	Boolean groupByDate = false;
	Boolean groupByMonth = false;

	Boolean groupByRoute = false;
	Boolean groupByService = false;
	Boolean groupByAffiliate = false;
	Boolean groupByBatch = false;
	Boolean groupByNetwork = false;

}
