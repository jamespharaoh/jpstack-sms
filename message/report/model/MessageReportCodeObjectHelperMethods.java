package wbs.sms.message.report.model;

public
interface MessageReportCodeObjectHelperMethods {

	MessageReportCodeRec findOrCreate (
			Long status,
			Long statusType,
			Long reason,
			MessageReportCodeType type,
			boolean success,
			boolean permanent,
			String description);

}