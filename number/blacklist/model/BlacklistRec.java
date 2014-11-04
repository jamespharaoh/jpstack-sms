package wbs.sms.number.blacklist.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class BlacklistRec
	implements CommonRecord<BlacklistRec> {

	@GeneratedIdField
	Integer id;

	@CodeField
	String number;

	@SimpleField
	String reason;

	// compare to

	@Override
	public int compareTo (
			Record<BlacklistRec> otherRecord) {

		BlacklistRec other =
			(BlacklistRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

}
