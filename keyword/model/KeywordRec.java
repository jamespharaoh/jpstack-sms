package wbs.sms.keyword.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.command.model.CommandRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class KeywordRec
	implements EphemeralRecord<KeywordRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	KeywordSetRec keywordSet;

	@CodeField
	String keyword;

	// details

	@DescriptionField
	String description;

	// settings

	@ReferenceField (
		nullable = true)
	CommandRec command;

	@SimpleField
	Boolean sticky = false;

	@SimpleField
	Boolean leaveIntact = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<KeywordRec> otherRecord) {

		KeywordRec other =
			(KeywordRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getKeywordSet (),
				other.getKeywordSet ())

			.append (
				getKeyword (),
				other.getKeyword ())

			.toComparison ();

	}

}
