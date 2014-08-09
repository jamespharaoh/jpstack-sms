package wbs.sms.network.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class NetworkPrefixRec
	implements EphemeralRecord<NetworkPrefixRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String prefix;

	// details

	@ReferenceField
	NetworkRec network;

	@Override
	public
	int compareTo (
			Record<NetworkPrefixRec> otherRecord) {

		NetworkPrefixRec other =
			(NetworkPrefixRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getPrefix (),
				other.getPrefix ())

			.toComparison ();

	}

}
