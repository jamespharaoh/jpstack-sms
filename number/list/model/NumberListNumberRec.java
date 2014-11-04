package wbs.sms.number.list.model;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.entity.annotations.EphemeralEntity;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.IdentityReferenceField;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.record.EphemeralRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@EphemeralEntity
public
class NumberListNumberRec
	implements EphemeralRecord<NumberListNumberRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	NumberListRec numberList;

	@IdentityReferenceField
	NumberRec number;

	// state

	@SimpleField
	Boolean present = false;

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberListNumberRec> otherRecord) {

		NumberListNumberRec other =
			(NumberListNumberRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumberList (),
				other.getNumberList ())

			.append (
				getNumber (),
				other.getNumber ())

			.toComparison ();

	}

	// object helper methods

	public static
	interface NumberListNumberObjectHelperMethods {

		NumberListNumberRec find (
				NumberListRec numberList,
				NumberRec number);

		NumberListNumberRec findOrCreate (
				NumberListRec numberList,
				NumberRec number);

	}

	// object helper implementation

	public static
	class NumberListNumberObjectHelperImplementation
		implements NumberListNumberObjectHelperMethods {

		@Inject
		Provider<NumberListNumberObjectHelper> numberListNumberHelper;

		@Override
		public
		NumberListNumberRec find (
				@NonNull NumberListRec numberList,
				@NonNull NumberRec number) {

			return numberListNumberHelper.get ().findByNumberListAndNumber (
				numberList.getId (),
				number.getId ());

		}

		@Override
		public
		NumberListNumberRec findOrCreate (
				@NonNull NumberListRec numberList,
				@NonNull NumberRec number) {

			// find existing

			NumberListNumberRec numberListNumber =
				numberListNumberHelper.get ().find (
					numberList,
					number);

			if (numberListNumber != null)
				return numberListNumber;

			// create new

			numberListNumber =
				numberListNumberHelper.get ().insert (
					new NumberListNumberRec ()
						.setNumberList (numberList)
						.setNumber (number)
						.setPresent (false));

			// return

			return numberListNumber;

		}

	}

	// dao

	public static
	interface NumberListNumberDaoMethods {

		NumberListNumberRec findByNumberListAndNumber (
				int numberListId,
				int numberId);

	}

}
