package wbs.sms.message.report.model;

public
interface MessageReportCodeDaoMethods {

	MessageReportCodeRec find (
			MessageReportCodeType type,
			Long status,
			Long statusType,
			Long reason);

}