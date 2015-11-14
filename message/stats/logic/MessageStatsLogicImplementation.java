package wbs.sms.message.stats.logic;

import lombok.NonNull;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.stats.model.MessageStatsData;

@SingletonComponent ("messageStatsLogic")
public
class MessageStatsLogicImplementation
	implements MessageStatsLogic {

	@Override
	public
	void addTo (
			@NonNull MessageStatsData target,
			@NonNull MessageStatsData difference) {

		target

			.setInTotal (
				+ target.getInTotal ()
				+ difference.getInTotal ())

			.setOutTotal (
				+ target.getOutTotal ()
				+ difference.getOutTotal ())

			.setOutPending (
				+ target.getOutPending ()
				+ difference.getOutPending ())

			.setOutCancelled (
				+ target.getOutCancelled ()
				+ difference.getOutCancelled ())

			.setOutFailed (
				+ target.getOutFailed ()
				+ difference.getOutFailed ())

			.setOutSent (
				+ target.getOutSent ()
				+ difference.getOutSent ())

			.setOutSubmitted (
				+ target.getOutSubmitted ()
				+ difference.getOutSubmitted ())

			.setOutDelivered (
				+ target.getOutDelivered ()
				+ difference.getOutDelivered ())

			.setOutUndelivered (
				+ target.getOutUndelivered ()
				+ difference.getOutUndelivered ())

			.setOutReportTimedOut (
				+ target.getOutReportTimedOut ()
				+ difference.getOutReportTimedOut ())

			.setOutHeld (
				+ target.getOutHeld ()
				+ difference.getOutHeld ())

			.setOutBlacklisted (
				+ target.getOutBlacklisted ()
				+ difference.getOutBlacklisted ())

			.setOutManuallyUndelivered (
				+ target.getOutManuallyUndelivered ()
				+ difference.getOutManuallyUndelivered ());

	}

}
