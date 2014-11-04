package wbs.sms.network.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.AssignedIdField;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class NetworkRec
	implements MajorRecord<NetworkRec> {

	// id

	@AssignedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	// settings

	@ReferenceField (
		nullable = true)
	NetworkRec virtualNetworkOfNetwork;

	@SimpleField
	Boolean shortMultipartMessages = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<NetworkRec> otherRecord) {

		NetworkRec other =
			(NetworkRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
