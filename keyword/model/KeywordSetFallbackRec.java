package wbs.sms.keyword.model;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CommonEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
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
class KeywordSetFallbackRec
	implements CommonRecord<KeywordSetFallbackRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ReferenceField
	KeywordSetRec keywordSet;

	@ReferenceField
	NumberRec number;

	// details

	@SimpleField
	Date timestamp =
		new Date ();

	@ReferenceField
	CommandRec command;

	@SimpleField (
		nullable = true)
	Integer ref;

	// compare to

	@Override
	public
	int compareTo (
			Record<KeywordSetFallbackRec> otherRecord) {

		KeywordSetFallbackRec other =
			(KeywordSetFallbackRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getKeywordSet (),
				other.getKeywordSet ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

	// dao methods

	public static
	interface KeywordSetFallbackDaoMethods {

		KeywordSetFallbackRec find (
				KeywordSetRec keywordSet,
				NumberRec number);

	}


}
