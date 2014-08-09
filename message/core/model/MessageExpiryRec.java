package wbs.sms.message.core.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.ForeignIdField;
import wbs.framework.entity.annotations.MasterField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class MessageExpiryRec
	implements EphemeralRecord<MessageExpiryRec> {

	@ForeignIdField (
		field = "message")
		Integer id;

	@MasterField
	MessageRec message;

	@SimpleField
	Date expiryTime;

	// compare to

	@Override
	public
	int compareTo (
			Record<MessageExpiryRec> otherRecord) {

		MessageExpiryRec other =
			(MessageExpiryRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getExpiryTime (),
				getExpiryTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface MessageExpiryDaoMethods {

		List<MessageExpiryRec> findPendingLimit (
				int maxResults);

	}

}
