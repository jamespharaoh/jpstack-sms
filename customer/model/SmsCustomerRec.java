package wbs.sms.customer.model;

import static wbs.framework.utils.etc.Misc.generateSixDigitCode;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.Instant;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MajorEntity;
import wbs.framework.entity.annotations.ParentField;
import wbs.framework.entity.annotations.ReferenceField;
import wbs.framework.entity.annotations.SimpleField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.number.core.model.NumberRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MajorEntity
public
class SmsCustomerRec
	implements CommonRecord<SmsCustomerRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentField
	SmsCustomerManagerRec smsCustomerManager;

	@CodeField
	String code;

	// details

	@ReferenceField
	NumberRec number;

	@SimpleField
	Instant createdTime;

	// state

	@SimpleField (
		nullable = true)
	Instant lastActionTime;

	@ReferenceField (
		nullable = true)
	SmsCustomerSessionRec activeSession;

	// statistics

	@SimpleField
	Integer numSessions = 0;

	// compare to

	@Override
	public
	int compareTo (
			Record<SmsCustomerRec> otherRecord) {

		SmsCustomerRec other =
			(SmsCustomerRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getNumber (),
				other.getNumber ())

			.append (
				getSmsCustomerManager (),
				other.getSmsCustomerManager ())

			.toComparison ();

	}

	// dao methods

	public static
	interface SmsCustomerDaoMethods {

		List<Integer> searchIds (
				SmsCustomerSearch search);

		SmsCustomerRec find (
				SmsCustomerManagerRec manager,
				NumberRec number);

	}

	// helper methods

	public static
	interface SmsCustomerObjectHelperMethods {

		SmsCustomerRec findOrCreate (
				SmsCustomerManagerRec manager,
				NumberRec number);

	}

	// helper implementation

	public static
	class SmsCustomerObjectHelperImplementation
		implements SmsCustomerObjectHelperMethods {

		// dependencies

		@Inject
		Database database;

		// indirect dependencies

		@Inject
		Provider<SmsCustomerObjectHelper> smsCustomerHelperProvider;

		// implementation

		@Override
		public
		SmsCustomerRec findOrCreate (
				SmsCustomerManagerRec manager,
				NumberRec number) {

			Transaction transaction =
				database.currentTransaction ();

			SmsCustomerObjectHelper smsCustomerHelper =
				smsCustomerHelperProvider.get ();

			SmsCustomerRec customer =
				smsCustomerHelper.find (
					manager,
					number);

			if (customer != null)
				return customer;

			customer =
				smsCustomerHelper.insert (
					new SmsCustomerRec ()

				.setSmsCustomerManager (
					manager)

				.setNumber (
					number)

				.setCode (
					generateSixDigitCode ())

				.setCreatedTime (
					transaction.now ())

			);

			return customer;

		}

	}

	// object hooks

	public static
	class SmsCustomerHooks
		extends AbstractObjectHooks<SmsCustomerRec> {

		@Inject
		SmsCustomerDao smsCustomerDao;

		@Override
		public
		List<Integer> searchIds (
				Object searchObject) {

			SmsCustomerSearch search =
				(SmsCustomerSearch)
				searchObject;

			return smsCustomerDao.searchIds (
				search);

		}

	}

}
