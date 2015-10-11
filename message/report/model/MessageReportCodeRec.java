package wbs.sms.message.report.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MessageReportCodeRec
	implements CommonRecord<MessageReportCodeRec> {

	@GeneratedIdField
	Integer id;

	@SimpleField
	Integer status;

	@SimpleField
	Integer statusType;

	@SimpleField
	Integer reason;

	@SimpleField
	Boolean success;

	@SimpleField
	Boolean permanent;

	@SimpleField
	String description;

	@SimpleField
	MessageReportCodeType type;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageReportCodeRec> otherRecord) {

		MessageReportCodeRec other =
			(MessageReportCodeRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getId (),
				other.getId ())

			.toComparison ();

	}

}
