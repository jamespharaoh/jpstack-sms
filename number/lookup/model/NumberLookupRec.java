package wbs.sms.number.lookup.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.builder.CompareToBuilder;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.annotations.CodeField;
import wbs.framework.entity.annotations.GeneratedIdField;
import wbs.framework.entity.annotations.MinorEntity;
import wbs.framework.entity.annotations.ParentIdField;
import wbs.framework.entity.annotations.ParentTypeField;
import wbs.framework.entity.annotations.TypeField;
import wbs.framework.object.AbstractObjectHooks;
import wbs.framework.object.ObjectHelper;
import wbs.framework.record.MinorRecord;
import wbs.framework.record.Record;
import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

@Accessors (chain = true)
@Data
@EqualsAndHashCode (of = "id")
@ToString (of = "id")
@MinorEntity
public
class NumberLookupRec
	implements MinorRecord<NumberLookupRec> {

	// id

	@GeneratedIdField
	Integer id;

	// identity

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	// details

	@TypeField
	NumberLookupTypeRec numberLookupType;

	// compare to

	@Override
	public
	int compareTo (
			Record<NumberLookupRec> otherRecord) {

		NumberLookupRec other =
			(NumberLookupRec) otherRecord;

		return new CompareToBuilder ()

			.append (
				getParentObjectType (),
				other.getParentObjectType ())

			.append (
				getParentObjectId (),
				other.getParentObjectId ())

			.append (
				getCode (),
				other.getCode ())

			.toComparison ();

	}

	// object hooks

	public static
	class NumberLookupHooks
		extends AbstractObjectHooks<NumberLookupRec> {

		@Inject
		Database database;

		@Inject
		ObjectTypeDao objectTypeDao;

		@Inject
		NumberLookupTypeDao numberLookupTypeDao;

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		@PostConstruct
		public
		void init () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					this);

			List<ObjectTypeRec> objectTypes =
				objectTypeDao.findAll ();

			for (ObjectTypeRec objectType
					: objectTypes) {

				List<NumberLookupTypeRec> numberLookupTypes =
					numberLookupTypeDao.findByParentObjectType (
						objectType);

				if (numberLookupTypes.isEmpty ())
					continue;

				parentObjectTypeIds.add (
					objectType.getId ());

			}

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<NumberLookupRec> numberLookupHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<NumberLookupTypeRec> numberLookupTypes =
				numberLookupTypeDao.findByParentObjectType (
					parentType);

			for (NumberLookupTypeRec numberLookupType
					: numberLookupTypes) {

				numberLookupHelper.insert (
					new NumberLookupRec ()

					.setParentObjectType (
						parentType)

					.setParentObjectId (
						parent.getId ())

					.setCode (
						numberLookupType.getCode ())

					.setNumberLookupType (
						numberLookupType)

				);

			}

		}

	}

}
