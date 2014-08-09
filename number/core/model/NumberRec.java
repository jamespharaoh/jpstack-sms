package wbs.sms.number.core.model;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

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
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.record.CommonRecord;
import wbs.framework.record.Record;
import wbs.sms.network.model.NetworkRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@CommonEntity
public
class NumberRec
	implements CommonRecord<NumberRec> {

	@GeneratedIdField
	Integer id;

	@CodeField
	String number;

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
			.append (getNumber (), other.getNumber ())
			.toComparison ();

	}

	// dao methods

	public static
	interface NumberDaoMethods {

		List<Integer> searchIds (
				NumberSearch numberSearch);

	}

	// object hooks

	public static
	class NumberHooks
		extends AbstractObjectHooks<NumberRec> {

		@Inject
		NumberDao numberDao;

		@Override
		public
		List<Integer> searchIds (
				Object search) {

			NumberSearch numberSearch =
				(NumberSearch) search;

			return numberDao.searchIds (
				numberSearch);

		}

	}

}
