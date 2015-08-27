package wbs.sms.message.stats.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import wbs.framework.entity.annotations.ComponentEntity;
import wbs.framework.entity.annotations.SimpleField;

@Accessors (chain = true)
@Data
@EqualsAndHashCode
@ToString
@ComponentEntity
public
class MessageStats {

	@SimpleField
	Integer inTotal = 0;

	@SimpleField
	Integer outTotal = 0;

	@SimpleField
	Integer outPending = 0;

	@SimpleField
	Integer outCancelled = 0;

	@SimpleField
	Integer outFailed = 0;

	@SimpleField
	Integer outSent = 0;

	@SimpleField
	Integer outSubmitted = 0;

	@SimpleField
	Integer outDelivered = 0;

	@SimpleField
	Integer outUndelivered = 0;

	@SimpleField
	Integer outReportTimedOut = 0;

	@SimpleField
	Integer outHeld = 0;

	@SimpleField
	Integer outBlacklisted = 0;

	@SimpleField
	Integer outManuallyUndelivered = 0;

	public
	MessageStats plusEq (
			MessageStats messageStats) {

		inTotal +=
			messageStats.inTotal;

		outTotal +=
			messageStats.outTotal;

		outPending +=
			messageStats.outPending;

		outCancelled +=
			messageStats.outCancelled;

		outFailed +=
			messageStats.outFailed;

		outSent +=
			messageStats.outSent;

		outSubmitted +=
			messageStats.outSubmitted;

		outDelivered +=
			messageStats.outDelivered;

		outUndelivered +=
			messageStats.outUndelivered;

		outReportTimedOut +=
			messageStats.outReportTimedOut;

		outHeld +=
			messageStats.outHeld;

		outBlacklisted +=
			messageStats.outBlacklisted;

		outManuallyUndelivered +=
			messageStats.outManuallyUndelivered;

		return this;

	}

}
