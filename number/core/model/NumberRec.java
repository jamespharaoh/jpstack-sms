package wbs.sms.number.core.model;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

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
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkObjectHelper;
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

	// dao methods

	public static
	interface NumberDaoMethods {

		List<Integer> searchIds (
				NumberSearch numberSearch);

	}

	// object helper methods

	public static
	interface NumberObjectHelperMethods {

		NumberRec findOrCreate (
				String number);

	}

	// object helper implementation

	public static
	class NumberObjectHelperImplementation
		implements NumberObjectHelperMethods {

		// indirect dependencies

		@Inject
		Provider<NetworkObjectHelper> networkHelperProvider;

		@Inject
		Provider<NumberObjectHelper> numberHelperProvider;

		// implementation

		@Override
		public
		NumberRec findOrCreate (
				String numberString) {

			NetworkObjectHelper networkHelper =
				networkHelperProvider.get ();

			NumberObjectHelper numberHelper =
				numberHelperProvider.get ();

			// find existing

			NumberRec numberRecord =
				numberHelper.findByCode (
					GlobalId.root,
					numberString);

			if (numberRecord != null)
				return numberRecord;

			// create it

			NetworkRec defaultNetwork =
				networkHelper.find (0);

			return numberHelper.insert (
				new NumberRec ()

				.setNumber (
					numberString)

				.setNetwork (
					defaultNetwork)

			);

		}

	}

}
