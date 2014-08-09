package wbs.sms.message.outbox.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity (
	table = "failedmessage")
public
class FailedMessageRec
	implements CommonRecord<FailedMessageRec> {

	// id

	@ForeignIdField (
		field = "message",
		column = "messageid")
	Integer id;

	// identity

	@MasterField
	MessageRec message;

	// details

	@SimpleField
	String error;

	// compare to

	@Override
	public
	int compareTo (
			Record<FailedMessageRec> otherRecord) {

		FailedMessageRec other =
			(FailedMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMessage (),
				other.getMessage ())

			.toComparison ();

	}

}
