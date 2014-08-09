package wbs.sms.number.format.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.DeletedField;
import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IndexField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class NumberFormatPatternRec
	implements EphemeralRecord<NumberFormatPatternRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	NumberFormatRec numberFormat;

	@IndexField
	Integer index;

	// details

	@DeletedField
	Boolean deleted = false;

	// settings

	@SimpleField
	String inputPrefix;

	@SimpleField
	String outputPrefix;

	@SimpleField
	Integer minimumLength;

	@SimpleField
	Integer maximumLength;

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberFormatPatternRec> otherRecord) {

		NumberFormatPatternRec other =
			(NumberFormatPatternRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumberFormat (),
				other.getNumberFormat ())

			.append (
				getIndex (),
				other.getIndex ())

			.toComparison ();

	}

	// hooks

	public static
	class NumberFormatPatternHooks
		extends AbstractObjectHooks<NumberFormatPatternRec> {

		@Override
		public
		void beforeInsert (
				NumberFormatPatternRec numberFormatPattern) {

			NumberFormatRec numberFormat =
				numberFormatPattern.getNumberFormat ();

			numberFormatPattern.setIndex (
				numberFormat.getNumPatterns ());

			numberFormat.setNumPatterns (
				numberFormat.getNumPatterns () + 1);

		}

	}

}
