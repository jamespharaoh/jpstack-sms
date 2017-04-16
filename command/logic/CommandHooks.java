package wbs.sms.command.logic;

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
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.framework.object.ObjectHelper;
import wbs.framework.object.ObjectHooks;

import wbs.platform.object.core.model.ObjectTypeDao;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.command.model.CommandRec;
import wbs.sms.command.model.CommandTypeDao;
import wbs.sms.command.model.CommandTypeRec;

public
class CommandHooks
	implements ObjectHooks <CommandRec> {

	// singleton dependencies

	@SingletonDependency
	CommandTypeDao commandTypeDao;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeDao objectTypeDao;

	// state

	Map <Long, List <Long>> commandTypeIdsByParentTypeId =
		new HashMap<> ();

	// lifecycle

	@NormalLifecycleSetup
	public
	void init (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"init");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"commandHooks.init ()",
					this);

		) {

			commandTypeIdsByParentTypeId =
				commandTypeDao.findAll ().stream ().collect (
					Collectors.groupingBy (

				commandType ->
					commandType.getParentType ().getId (),

				Collectors.mapping (
					commandType ->
						commandType.getId (),
					Collectors.toList ())

			));

		}

	}

	// implementation

	@Override
	public
	void createSingletons (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ObjectHelper<CommandRec> commandHelper,
			@NonNull ObjectHelper<?> parentHelper,
			@NonNull Record<?> parent) {

		if (
			doesNotContain (
				commandTypeIdsByParentTypeId.keySet (),
				parentHelper.objectTypeId ())
		) {
			return;
		}

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSingletons");

		ObjectTypeRec parentType =
			objectTypeDao.findById (
				parentHelper.objectTypeId ());

		for (
			Long commandTypeId
				: commandTypeIdsByParentTypeId.get (
					parentHelper.objectTypeId ())
		) {

			CommandTypeRec commandType =
				commandTypeDao.findRequired (
					commandTypeId);

			commandHelper.insert (
				taskLogger,
				commandHelper.createInstance ()

				.setCommandType (
					commandType)

				.setCode (
					commandType.getCode ())

				.setParentType (
					parentType)

				.setParentId (
					parent.getId ())

			);

		}

	}

}