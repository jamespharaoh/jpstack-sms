package wbs.sms.messageset.fixture;

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
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;

import wbs.sms.messageset.metamodel.MessageSetTypeSpec;
import wbs.sms.messageset.model.MessageSetTypeObjectHelper;

@PrototypeComponent ("messageSetTypeBuilder")
@ModelMetaBuilderHandler
public
class MessageSetTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageSetTypeObjectHelper messageSetTypeHelper;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	MessageSetTypeSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

		try {

			createMessageSetType (
				taskLogger);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating message set type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createMessageSetType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createMessageSetType");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"MessageSetTypeBuilder.createMessageSetType ()",
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

			// create message set type

			messageSetTypeHelper.insert (
				taskLogger,
				messageSetTypeHelper.createInstance ()

				.setParentType (
					parentType)

				.setCode (
					simplifyToCodeRequired (
						spec.name ()))

				.setDescription (
					spec.description ())

			);

			// commit transaction

			transaction.commit ();

		}

	}

}
