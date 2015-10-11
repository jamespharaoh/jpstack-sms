package wbs.sms.number.core.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class NumberRec
	implements CommonRecord<NumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String number;

	// details

	@ReferenceField
	NetworkRec network;

	@SimpleField (
		nullable = true)
		Date archiveDate;

	@SimpleField
	Boolean free = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberRec> otherRecord) {

		NumberRec other =
			(NumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
