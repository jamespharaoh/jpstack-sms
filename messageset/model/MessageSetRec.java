package wbs.sms.messageset.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import wbs.framework.entity.annotations.CollectionField;
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
class MessageSetRec
	implements MinorRecord<MessageSetRec> {

	@GeneratedIdField
	Integer id;

	@ParentTypeField
	ObjectTypeRec parentObjectType;

	@ParentIdField
	Integer parentObjectId;

	@CodeField
	String code;

	@TypeField
	MessageSetTypeRec messageSetType;

	// TODO change to list
	@CollectionField (
		index = "index",
		orderBy = "index")
	Map<Integer,MessageSetMessageRec> messages =
		new LinkedHashMap<Integer,MessageSetMessageRec> ();

	public static
	class MessageSetHooks
		extends AbstractObjectHooks<MessageSetRec> {

		@Inject
		Database database;

		@Inject
		MessageSetTypeDao messageSetTypeDao;

		@Inject
		ObjectTypeDao objectTypeDao;

		Set<Integer> parentObjectTypeIds =
			new HashSet<Integer> ();

		@PostConstruct
		public
		void init () {

			@Cleanup
			Transaction transaction =
				database.beginReadOnly ();

			List<ObjectTypeRec> objectTypes =
				objectTypeDao.findAll ();

			for (ObjectTypeRec objectType : objectTypes) {

				List<MessageSetTypeRec> messageSetTypes =
					messageSetTypeDao.findByParentObjectType (
						objectType);

				if (messageSetTypes.isEmpty ())
					continue;

				parentObjectTypeIds.add (
					objectType.getId ());

			}

		}

		@Override
		public
		void createSingletons (
				ObjectHelper<MessageSetRec> objectHelper,
				ObjectHelper<?> parentHelper,
				Record<?> parent) {

			if (! parentObjectTypeIds.contains (
					parentHelper.objectTypeId ()))
				return;

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			List<MessageSetTypeRec> messageSetTypes =
				messageSetTypeDao.findByParentObjectType (
					parentType);

			for (MessageSetTypeRec messageSetType
					: messageSetTypes) {

				objectHelper.insert (
					new MessageSetRec ()
						.setMessageSetType (messageSetType)
						.setCode (messageSetType.getCode ())
						.setParentObjectType (parentType)
						.setParentObjectId (parent.getId ()));

			}

		}

	}

	@Override
	public
	int compareTo (
			Record<MessageSetRec> otherRecord) {

		MessageSetRec other =
			(MessageSetRec) otherRecord;

		return new CompareToBuilder ()
			.append (getParentObjectType (), other.getParentObjectType ())
			.append (getParentObjectId (), other.getParentObjectId ())
			.append (getCode (), other.getCode ())
			.toComparison ();

	}

}
