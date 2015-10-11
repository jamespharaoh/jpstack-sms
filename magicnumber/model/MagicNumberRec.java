package wbs.sms.magicnumber.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class MagicNumberRec
	implements CommonRecord<MagicNumberRec> {

	@GeneratedIdField
	Integer id;

	@ParentField
	MagicNumberSetRec magicNumberSet;

	@CodeField
	String number;

	@DeletedField
	Boolean deleted = false;

	@CollectionField
	Set<MagicNumberUseRec> magicNumberUses =
		new HashSet<MagicNumberUseRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<MagicNumberRec> otherRecord) {

		MagicNumberRec other =
			(MagicNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getMagicNumberSet (),
				other.getMagicNumberSet ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}