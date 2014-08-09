package wbs.sms.message.delivery.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class DeliveryRec
	implements EphemeralRecord<DeliveryRec> {

	@GeneratedIdField
	Integer id;

	@ReferenceField
	MessageRec message;

	@SimpleField
	MessageStatus oldMessageStatus;

	@SimpleField
	MessageStatus newMessageStatus;

	@Override
	public
	int compareTo (
			Record<DeliveryRec> otherRecord) {

		DeliveryRec other =
			(DeliveryRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				other.getId (),
				getId ())

			.toComparison ();

	}

	// dao methods

	public static
	interface DeliveryDaoMethods {

		List<DeliveryRec> findAllLimit (
				int maxResults);

	}

}
