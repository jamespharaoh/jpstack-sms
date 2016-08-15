package wbs.framework.object;

import static wbs.framework.utils.etc.Misc.cast;
import static wbs.framework.utils.etc.Misc.doNothing;
import static wbs.framework.utils.etc.Misc.isNotInstanceOf;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.optionalRequired;
import static wbs.framework.utils.etc.StringUtils.stringFormat;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.record.GlobalId;
import wbs.framework.record.Record;
import wbs.framework.utils.etc.BeanLogic;

@Accessors (fluent = true)
@PrototypeComponent ("objectHelperPropertyImplementation")
public
class ObjectHelperPropertyImplementation<RecordType extends Record<RecordType>>
	implements
		ObjectHelperComponent<RecordType>,
		ObjectHelperPropertyMethods<RecordType> {

	// dependencies

	@Inject
	ObjectTypeRegistry objectTypeRegistry;

	// properties

	@Setter
	ObjectModel<RecordType> model;

	@Setter
	ObjectHelper<RecordType> objectHelper;

	@Setter
	ObjectDatabaseHelper<RecordType> objectDatabaseHelper;

	@Setter
	ObjectManager objectManager;

	// public implementation

	@Override
	public
	String getName (
			@NonNull Record<?> objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.getName (
			object);

	}

	@Override
	public
	String getTypeCode (
			@NonNull Record<?> objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.getTypeCode (
			object);

	}

	@Override
	public
	String getCode (
			@NonNull Record<?> objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.getCode (
			object);

	}

	@Override
	public
	String getDescription (
			@NonNull Record<?> objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.getDescription (
			object);

	}

	@Override
	public
	Record<?> getParentObjectType (
			@NonNull Record<?> objectUncast) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.getParentType (
			object);

	}

	@Override
	public
	Long getParentTypeId (
			@NonNull Record<?> object) {

		if (
			! model.objectClass ().isInstance (
				object)
		) {

			throw new IllegalArgumentException ();

		} else if (model.isRoot ()) {

			throw new UnsupportedOperationException ();

		} else if (model.parentTypeId () != null) {

			return model.parentTypeId ();

		} else {

			Record<?> parentType =
				model.getParentType (
					object);

			return (long)
				parentType.getId ();

		}

	}

	@Override
	public
	Long getParentId (
			@NonNull Record<?> object) {

		if (
			isNotInstanceOf (
				model.objectClass (),
				object)
		) {

			throw new IllegalArgumentException ();

		} else if (model.isRoot ()) {

			throw new UnsupportedOperationException ();

		} else if (model.isRooted ()) {

			return 0l;

		} else if (model.canGetParent ()) {

			Record<?> parent =
				getParent (
					object);

			return (long) parent.getId ();

		} else {

			return (long) (Integer)
				BeanLogic.getProperty (
					object,
					model.parentIdField ().name ());

		}

	}

	@Override
	public
	void setParent (
			@NonNull Record<?> objectUncast,
			@NonNull Record<?> parent) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		BeanLogic.setProperty (
			object,
			model.parentField ().name (),
			parent);

		// TODO grand parent etc

	}

	@Override
	public
	GlobalId getParentGlobalId (
			@NonNull Record<?> object) {

		if (model.isRoot ()) {

			return null;

		} else {

			return new GlobalId (
				getParentTypeId (
					object),
				getParentId (
					object));

		}

	}

	@Override
	public
	Record<?> getParent (
			@NonNull Record<?> object) {

		if (model.isRoot ()) {

			return null;

		} else if (model.isRooted ()) {

			ObjectHelper<?> rootHelper =
				objectManager.objectHelperForClassRequired (
					objectTypeRegistry.rootRecordClass ());

			return rootHelper.findRequired (
				0);

		} else if (model.canGetParent ()) {

			Record<?> parent =
				model.getParent (
					object);

			if (parent == null) {

				throw new RuntimeException (
					stringFormat (
						"Failed to get parent of %s with id %s",
						model.objectName (),
						object.getId ()));

			}

			return parent;

		} else {

			Record<?> parentObjectType =
				(Record<?>)
				model.getParentType (
					object);

			Long parentObjectId =
				model.getParentId (
					object);

			if (parentObjectId == null) {

				throw new RuntimeException (
					stringFormat (
						"Failed to get parent id of %s with id %s",
						model.objectName (),
						object.getId ()));

			}

			ObjectHelper<?> parentHelper=
				objectManager.objectHelperForTypeId (
					(long) parentObjectType.getId ());

			if (parentHelper == null) {

				throw new RuntimeException (
					stringFormat (
						"No object helper provider for %s, ",
						parentObjectType.getId (),
						"parent of %s (%s)",
						model.objectName (),
						object.getId ()));

			}

			Optional<? extends Record<?>> parentOptional =
				parentHelper.find (
					parentObjectId);

			if (
				isNotPresent (
					parentOptional)
			) {

				throw new RuntimeException (
					stringFormat (
						"Can't find %s with id %s",
						parentHelper.objectName (),
						parentObjectId));

			}

			return optionalRequired (
				parentOptional);

		}

	}

	@Override
	public
	GlobalId getGlobalId (
			@NonNull Record<?> object) {

		return new GlobalId (
			model.objectTypeId (),
			object.getId ());

	}

	@Override
	public
	Boolean getDeleted (
			@NonNull Record<?> object,
			boolean checkParents) {

		Record<?> currentObject =
			object;

		ObjectHelper<?> currentHelper =
			objectHelper;

		for (;;) {

			// root is never deleted

			if (currentHelper.isRoot ()) {
				return false;
			}

			// check our deleted flag

			try {
	
				boolean deletedProperty =
					(Boolean)
					BeanLogic.getProperty (
						object,
						"deleted");

				if (deletedProperty) {
					return true;
				}
	
			} catch (Exception exception) {

				doNothing ();	

			}

			// try parent

			if (! checkParents) {
				return false;
			}

			if (currentHelper.isRooted ()) {
				return false;
			}

			if (currentHelper.canGetParent ()) {

				currentObject =
					currentHelper.getParent (
						currentObject);

				currentHelper =
					objectManager.objectHelperForObject (
						currentObject);

			} else {

				Record<?> parentType =
					(Record<?>)
					objectHelper.getParentType (
						currentObject);

				Long parentObjectId =
					getParentId (
						currentObject);

				currentHelper =
					objectManager.objectHelperForTypeId (
						(long) parentType.getId ());

				currentObject =
					currentHelper.findRequired (
						parentObjectId);

			}

		}

	}

	@Override
	public
	Object getDynamic (
			@NonNull Record<?> objectUncast,
			@NonNull String name) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		return model.hooks ().getDynamic (
			 object,
			 name);

	}

	@Override
	public
	void setDynamic (
			@NonNull Record<?> objectUncast,
			@NonNull String name,
			@NonNull Object value) {

		@SuppressWarnings ("unchecked")
		RecordType object =
			(RecordType)
			cast (
				model.objectClass (),
				objectUncast);

		model.hooks ().setDynamic (
			object,
			name,
			value);

	}

}