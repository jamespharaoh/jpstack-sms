package wbs.sms.message.stats.model;

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ComponentField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity (
	mutable = false)
public
class MessageStatsRec
	implements CommonRecord<MessageStatsRec> {

	@GeneratedIdField
	Integer id;

	@ComponentField
	MessageStatsId messageStatsId;

	@ComponentField
	MessageStats stats;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageStatsRec> otherRecord) {

		MessageStatsRec other =
			(MessageStatsRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

	@Accessors (fluent = true)
	@Data
	public static
	class MessageStatsSearch {

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

}
