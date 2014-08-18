package wbs.sms.magicnumber.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.command.model.CommandRec;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MagicNumberUseRec
	implements CommonRecord<MagicNumberUseRec> {

	// id

	@GeneratedIdField
	Integer id;

	@ParentField
	MagicNumberRec magicNumber;

	@IdentityReferenceField
	NumberRec number;

	// state

	@ReferenceField
	CommandRec command;

	@SimpleField
	Integer refId;

	@SimpleField
	Date lastUseTimestamp;

	// compare to

	@Override
	public
	int compareTo (
			Record<MagicNumberUseRec> otherRecord) {

		MagicNumberUseRec other =
			(MagicNumberUseRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMagicNumber (),
				other.getMagicNumber ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

	public static
	interface MagicNumberUseDaoMethods {

		MagicNumberUseRec find (
				MagicNumberRec magicNumber,
				NumberRec number);

		MagicNumberUseRec findExistingByRef (
				MagicNumberSetRec magicNumberSet,
				NumberRec number,
				CommandRec command,
				Integer ref);

		MagicNumberUseRec findExistingLeastRecentlyUsed (
				MagicNumberSetRec magicNumberSet,
				NumberRec number);

	}

}
