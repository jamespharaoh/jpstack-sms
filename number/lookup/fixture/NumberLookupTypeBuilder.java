package wbs.sms.number.lookup.fixture;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.camelToUnderscore;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;

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

import wbs.sms.number.lookup.metamodel.NumberLookupTypeSpec;
import wbs.sms.number.lookup.model.NumberLookupTypeObjectHelper;

@Log4j
@PrototypeComponent ("numberLookupTypeBuilder")
@ModelMetaBuilderHandler
public
class NumberLookupTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberLookupTypeObjectHelper numberLookupTypeHelper;

	@SingletonDependency
	ObjectTypeObjectHelper objectTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	NumberLookupTypeSpec spec;

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

			log.info (
				stringFormat (
					"Create delivery type %s.%s",
					camelToUnderscore (
						ifNull (
							spec.subject (),
							parent.name ())),
					simplifyToCodeRequired (
						spec.name ())));

			createNumberLookupType (
				taskLogger);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating number lookup type %s.%s",
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
	void createNumberLookupType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createNumberLookupType");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					"NumberLookupTypeBuilder.createNumberLookupType ()",
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

			// create number lookup type

			numberLookupTypeHelper.insert (
				taskLogger,
				numberLookupTypeHelper.createInstance ()

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
