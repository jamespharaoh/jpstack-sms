package wbs.sms.command.fixture;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.command.metamodel.CommandTypeSpec;
import wbs.sms.command.model.CommandTypeObjectHelper;

@PrototypeComponent ("commandTypeBuilder")
@ModelMetaBuilderHandler
public
class CommandTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	CommandTypeObjectHelper commandTypeHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	CommandTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			try {

				taskLogger.noticeFormat (
					"Create command type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ()));

				createCommandType (
					taskLogger);

			} catch (Exception exception) {

				throw new RuntimeException (
					stringFormat (
						"Error creating command type %s.%s",
						camelToUnderscore (
							ifNull (
								spec.subject (),
								parent.name ())),
						simplifyToCodeRequired (
							spec.name ())),
					exception);

			}

		}

	}

	private
	void createCommandType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createCommandType");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"CommandTypeBuilder.createCommandType ()",
					this);

		) {

			// lookup parent type

			String parentTypeCode =
				camelToUnderscore (
					ifNull (
						spec.subject (),
						parent.name ()));

			ObjectTypeRec parentType =
				objectTypeHelper.findByCodeRequired (
					GlobalId.root,
					parentTypeCode);

			// create command type

			commandTypeHelper.insert (
				taskLogger,
				commandTypeHelper.createInstance ()

				.setParentType (
					parentType)

				.setCode (
					simplifyToCodeRequired (
						spec.name ()))

				.setDescription (
					spec.description ())

				.setDeleted (
					false)

			);

			// commit transaction

			transaction.commit ();

		}

	}

}
