package wbs.sms.message.report.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MessageReportRec
	implements CommonRecord<MessageReportRec> {

	// TODO this is a big old mess

	@GeneratedIdField
	Integer id;

	@ParentField
	MessageRec message;

	@SimpleField
	Date receivedTime = new Date ();

	@SimpleField
	MessageStatus newMessageStatus;

	@SimpleField (
		nullable = true)
	String code;

	@ReferenceField (
		column = "report_code_id",
		nullable = true)
	MessageReportCodeRec messageReportCode;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageReportRec> otherRecord) {

		MessageReportRec other =
			(MessageReportRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getReceivedTime (),
				getReceivedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

}
