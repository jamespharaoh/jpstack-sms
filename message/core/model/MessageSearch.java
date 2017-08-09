package wbs.sms.message.core.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.TreeSet;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.interval.TextualInterval;

@Accessors (fluent = true)
@Data
public
class MessageSearch
	implements Serializable {

	Long messageId;

	String number;
	Long numberId;

	Long userId;

	Long serviceSliceId;
	Long serviceParentTypeId;
	Long serviceId;
	Collection<Long> serviceIdIn;

	Long affiliateId;
	Collection<Long> affiliateIdIn;

	Long batchId;
	Collection<Long> batchIdIn;

	Long routeSliceId;
	Long routeId;
	Collection<Long> routeIdIn;

	Long networkId;

	TextualInterval createdTime;

	//Instant createdTimeAfter;
	//Instant createdTimeBefore;

	MessageDirection direction;

	MessageStatus status;
	Collection<MessageStatus> statusIn;
	Collection<MessageStatus> statusNotIn;

	String textContains;

	String textLike;
	String textILike;

	Long maxResults;
	MessageSearchOrder orderBy;

	boolean filter;

	Collection<Long> filterAffiliateIds;
	Collection<Long> filterRouteIds;
	Collection<Long> filterServiceIds;

	public
	MessageSearch () {
	}

	public
	MessageSearch (
			MessageSearch original) {

		this.messageId =
			original.messageId;

		this.number =
			original.number;

		this.numberId =
			original.numberId;

		this.serviceSliceId =
			original.serviceSliceId;

		this.serviceParentTypeId =
			original.serviceParentTypeId;

		this.serviceId =
			original.serviceId;

		if (original.serviceIdIn != null) {

			this.serviceIdIn =
				new TreeSet<> (
					original.serviceIdIn);

		}

		this.affiliateId =
			original.affiliateId;

		if (original.affiliateIdIn != null) {

			this.affiliateIdIn =
				new TreeSet<> (
					original.affiliateIdIn);

		}

		this.batchId =
			original.batchId;

		if (original.batchIdIn != null) {

			this.batchIdIn =
				new TreeSet<> (
					original.batchIdIn);

		}

		this.routeSliceId =
			original.routeSliceId;

		this.routeId =
			original.routeId;

		if (original.routeIdIn != null) {

			this.routeIdIn =
				new TreeSet<> (
					original.routeIdIn);

		}

		this.networkId =
			original.networkId;

		this.createdTime =
			original.createdTime;

		this.direction =
			original.direction;

		this.status =
			original.status;

		if (original.statusIn != null) {

			this.statusIn =
				new TreeSet<> (
					original.statusIn);

		}

		if (original.statusNotIn != null) {

			this.statusNotIn =
				new TreeSet<> (
					original.statusNotIn);

		}

		this.textLike =
			original.textLike;

		this.textILike =
			original.textILike;

		this.maxResults =
			original.maxResults;

		this.orderBy =
			original.orderBy;

		this.filter =
			original.filter;

		if (original.filterAffiliateIds != null) {

			this.filterAffiliateIds =
				new TreeSet<> (
					original.filterAffiliateIds);

		}

		if (original.filterRouteIds != null) {

			this.filterRouteIds =
				new TreeSet<> (
					original.filterRouteIds);

		}

		if (original.filterServiceIds != null) {

			this.filterServiceIds =
				new TreeSet<> (
					original.filterServiceIds);

		}

	}

	public static
	enum MessageSearchOrder {
		createdTime,
		createdTimeDesc,
	}

}
