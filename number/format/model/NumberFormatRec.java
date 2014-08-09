package wbs.sms.number.format.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.CollectionField;
import wbs.framework.entity.annotations.DescriptionField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.NameField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.MajorRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class NumberFormatRec
	implements MajorRecord<NumberFormatRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@CodeField
	String code;

	// details

	@NameField
	String name;

	@DescriptionField
	String description;

	// statistics

	@SimpleField
	Integer numPatterns = 0;

	// children

	@CollectionField (
		index = "index")
	List<NumberFormatPatternRec> numberFormatPatterns =
		new ArrayList<NumberFormatPatternRec> ();

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberFormatRec> otherRecord) {

		NumberFormatRec other =
			(NumberFormatRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

}
