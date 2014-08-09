package wbs.sms.message.stats.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.entity.annotations.ComponentEntity;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = {
	"service",
	"route",
	"affiliate",
	"batch",
	"network",
	"date"
})
@ToString (of = {
	"service",
	"route",
	"affiliate",
	"batch",
	"network",
	"date"
})
@ComponentEntity
public
class MessageStatsId {

	@ReferenceField
	ServiceRec service;

	@ReferenceField
	RouteRec route;

	@ReferenceField
	AffiliateRec affiliate;

	@ReferenceField
	BatchRec batch;

	@ReferenceField
	NetworkRec network;

	@SimpleField
	LocalDate date;

}
