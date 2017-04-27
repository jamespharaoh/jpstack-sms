package wbs.sms.messageset.logic;

import static wbs.utils.etc.Misc.doesNotContain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.messageset.model.MessageSetTypeDao;
import wbs.sms.messageset.model.MessageSetTypeRec;

public
class MessageSetHooks
	implements ObjectHooks <MessageSetRec> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageSetTypeDao messageSetTypeDao;

	@SingletonDependency
	ObjectTypeDao objectTypeDao;

	// state

	Map <Long, List <Long>> messageSetTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"privHooks.init ()",
					this);

		) {

			messageSetTypeIdsByParentTypeId =
				messageSetTypeDao.findAll ().stream ().collect (
					Collectors.groupingBy (

				messageSetType ->
					messageSetType.getParentType ().getId (),

				Collectors.mapping (
					messageSetType ->
						messageSetType.getId (),
					Collectors.toList ())

			));

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper<MessageSetRec> messageSetHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				messageSetTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createSingletons");

		) {

			ObjectTypeRec parentType =
				objectTypeDao.findById (
					parentHelper.objectTypeId ());

			for (
				Long messageSetTypeId
					: messageSetTypeIdsByParentTypeId.get (
						parentHelper.objectTypeId ())
			) {

				MessageSetTypeRec messageSetType =
					messageSetTypeDao.findRequired (
						messageSetTypeId);

				messageSetHelper.insert (
					taskLogger,
					messageSetHelper.createInstance ()

					.setMessageSetType (
						messageSetType)

					.setCode (
						messageSetType.getCode ())

					.setParentType (
						parentType)

					.setParentId (
						parent.getId ())

				);

			}

		}

	}

}