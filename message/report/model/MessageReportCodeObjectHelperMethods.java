package wbs.sms.message.report.model;

public
interface MessageReportCodeObjectHelperMethods {

	MessageReportCodeRec findOrCreate (
			Integer status,
			Integer statusType,
			Integer reason,
			MessageReportCodeType type,
			boolean success,
			boolean permanent,
			String description);

}