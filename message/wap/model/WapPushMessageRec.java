package wbs.sms.message.wap.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.platform.text.model.TextRec;
import wbs.sms.message.core.model.MessageRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class WapPushMessageRec
	implements CommonRecord<WapPushMessageRec> {

	@ForeignIdField (
		field = "message")
	Integer id;

	@MasterField
	MessageRec message;

	@ReferenceField
	TextRec textText;

	@ReferenceField
	TextRec urlText;

	// compare to

	@Override
	public
	int compareTo (
			Record<WapPushMessageRec> otherRecord) {

		WapPushMessageRec other =
			(WapPushMessageRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMessage (),
				other.getMessage ())

			.toComparison ();

	}

}
