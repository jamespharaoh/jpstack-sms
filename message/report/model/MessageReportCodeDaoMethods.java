package wbs.sms.message.report.model;

public
interface MessageReportCodeDaoMethods {

	MessageReportCodeRec find (
			MessageReportCodeType type,
			Integer status,
			Integer statusType,
			Integer reason);

}