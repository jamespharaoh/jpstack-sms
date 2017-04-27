package wbs.sms.message.delivery.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.delivery.metamodel.DeliveryTypeSpec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;

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

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			taskLogger.noticeFormat (
				"Create delivery type %s",
				simplifyToCodeRequired (
					spec.name ()));

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

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createDeliveryType");

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"DeliveryTypeBuilder.createDeliveryType ()",
					this);

		) {

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

}
