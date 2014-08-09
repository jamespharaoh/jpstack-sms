package wbs.sms.modempoll.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class ModemPollQueueRec
	implements EphemeralRecord<ModemPollQueueRec> {

	@GeneratedIdField
	Integer id;

	@SimpleField
	String pdu;

	@SimpleField
	Date createdTime = new Date ();

	@SimpleField
	Integer tries = 0;

	@SimpleField
	Date retryTime = new Date ();

	@SimpleField
	String error;

	// compare to

	@Override
	public
	int compareTo (
			Record<ModemPollQueueRec> otherRecord) {

		ModemPollQueueRec other =
			(ModemPollQueueRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getCreatedTime (),
				getCreatedTime ())

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface ModemPollQueueDaoMethods {

		ModemPollQueueRec findNext ();

	}

}
