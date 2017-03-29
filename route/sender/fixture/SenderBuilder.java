package wbs.sms.route.sender.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.string.StringUtils.stringFormat;

import java.sql.SQLException;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.builder.Builder;
import wbs.framework.builder.BuilderComponent;
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

import wbs.sms.route.sender.metamodel.SenderSpec;
import wbs.sms.route.sender.model.SenderObjectHelper;

@PrototypeComponent ("senderBuilder")
@ModelMetaBuilderHandler
public
class SenderBuilder
	implements BuilderComponent {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EntityHelper entityHelper;

	@ClassSingletonDependency
	LogContext logContext;

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
	@Override
	public
	void build (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Builder builder) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"build");

		try {

			taskLogger.noticeFormat (
				"Create sender %s",
				simplifyToCodeRequired (
					spec.name ()));

			createSender (
				taskLogger);

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
	void createSender (
			@NonNull TaskLogger parentTaskLogger)
		throws SQLException {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSender");

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"SenderBuilder.createSender ()",
				this);

		// create sender

		senderHelper.insert (
			taskLogger,
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
