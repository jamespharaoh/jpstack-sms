package wbs.sms.route.sender.fixture;

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
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.sms.route.sender.metamodel.SenderSpec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@Log4j
@PrototypeComponent ("senderBuilder")
@ModelMetaBuilderHandler
public
class SenderBuilder {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	SenderSpec spec;

	@BuilderTarget
	Model <?> model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		try {

			log.info (
				stringFormat (
					"Create sender %s",
					simplifyToCodeRequired (
						spec.name ())));

			createSender ();

		} catch (Exception exception) {

			throw new RuntimeException (
				stringFormat (
					"Error creating sender %s",
					simplifyToCodeRequired (
						spec.name ())),
				exception);

		}

	}

	private
	void createSender ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SenderBuilder.createSender ()",
				this);

		// create sender

		senderHelper.insert (
			senderHelper.createInstance ()

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
