package wbs.sms.message.report.model;

import javax.inject.Inject;
import javax.inject.Provider;

public
class MessageReportCodeObjectHelperImplementation
	implements MessageReportCodeObjectHelperMethods {

	@Inject
	Provider<MessageReportCodeObjectHelper> messageReportCodeHelperProvider;

	@Override
	public
	MessageReportCodeRec findOrCreate (
			Integer status,
			Integer statusType,
			Integer reason,
			MessageReportCodeType type,
			boolean success,
			boolean permanent,
			String description) {

		MessageReportCodeObjectHelper messageReportCodeHelper =
			messageReportCodeHelperProvider.get ();

		// TODO move this

		if (description != null && description.length () == 0)
			description = null;

		MessageReportCodeRec reportCode =
			messageReportCodeHelper.find (
				type,
				status,
				statusType,
				reason);

		if (reportCode != null) {

			// update description

			if (description != null) {

				reportCode
					.setDescription (description);

			}

			return reportCode;

		}

		return messageReportCodeHelper.insert (
			messageReportCodeHelper.createInstance ()

			.setPermanent (
				permanent)

			.setReason (
				reason)

			.setStatus (
				status)

			.setStatusType (
				statusType)

			.setSuccess (
				success)

			.setDescription (
				description)

			.setType (
				type)

		);

	}

}