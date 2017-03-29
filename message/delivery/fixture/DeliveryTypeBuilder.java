package wbs.sms.message.delivery.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.Cleanup;
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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.delivery.metamodel.DeliveryTypeSpec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;

@Log4j
@PrototypeComponent ("deliveryTypeBuilder")
@ModelMetaBuilderHandler
public
class DeliveryTypeBuilder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	DeliveryTypeSpec spec;

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
					"Create delivery type %s",
					simplifyToCodeRequired (
						spec.name ())));

			createDeliveryType (
				taskLogger);

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating delivery type %s",
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createDeliveryType (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createDeliveryType");

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"DeliveryTypeBuilder.createDeliveryType ()",
				this);

		// create delivery type

		deliveryTypeHelper.insert (
			taskLogger,
			deliveryTypeHelper.createInstance ()

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
