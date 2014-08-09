package wbs.sms.number.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;

// TODO move this please!

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class ChatUserNumberReportRec
	implements MinorRecord<ChatUserNumberReportRec> {

	@ForeignIdField (
		field = "number")
	Integer id;

	@MasterField
	NumberRec number;

	@SimpleField (
		nullable = true)
	Date lastSuccess;

	@SimpleField (
		nullable = true)
	Date firstFailure;

	@SimpleField (
		nullable = true)
	Date permanentFailureReceived;

	@SimpleField
	Integer permanentFailureCount = 0;

	@Override
	public
	int compareTo (
			Record<ChatUserNumberReportRec> otherRecord) {

		ChatUserNumberReportRec other =
			(ChatUserNumberReportRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
