package wbs.sms.message.delivery.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.sql.SQLException;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.ModelMetaBuilderHandler;
import wbs.framework.entity.meta.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.sms.message.delivery.metamodel.DeliveryTypeSpec;
import wbs.sms.message.delivery.model.DeliveryTypeObjectHelper;

@Log4j
@PrototypeComponent ("deliveryTypeBuilder")
@ModelMetaBuilderHandler
public
class DeliveryTypeBuilder {

	// dependencies

	@Inject
	Database database;

	@Inject
	DeliveryTypeObjectHelper deliveryTypeHelper;

	@Inject
	EntityHelper entityHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	DeliveryTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		try {

			log.info (
				stringFormat (
					"Create delivery type %s",
					simplifyToCodeRequired (
						spec.name ())));

			createDeliveryType ();

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
	void createDeliveryType ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"DeliveryTypeBuilder.createDeliveryType ()",
				this);

		// create delivery type

		deliveryTypeHelper.insert (
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
