package wbs.sms.messageset.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;
import static wbs.framework.utils.etc.NullUtils.ifNull;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.camelToUnderscore;

import java.sql.SQLException;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.builder.Builder;
import wbs.framework.builder.annotations.BuildMethod;
import wbs.framework.builder.annotations.BuilderParent;
import wbs.framework.builder.annotations.BuilderSource;
import wbs.framework.builder.annotations.BuilderTarget;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.fixtures.ModelMetaBuilderHandler;
import wbs.framework.entity.helper.EntityHelper;
import wbs.framework.entity.meta.model.ModelMetaSpec;
import wbs.framework.entity.model.Model;
import wbs.framework.entity.record.GlobalId;
import wbs.platform.object.core.model.ObjectTypeObjectHelper;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.messageset.metamodel.MessageSetTypeSpec;
import wbs.sms.messageset.model.MessageSetTypeObjectHelper;

@PrototypeComponent ("messageSetTypeBuilder")
@ModelMetaBuilderHandler
public
class MessageSetTypeBuilder {

	// dependencies

	@Inject
	Database database;

	@Inject
	EntityHelper entityHelper;

	@Inject
	MessageSetTypeObjectHelper messageSetTypeHelper;

	@Inject
	ObjectTypeObjectHelper objectTypeHelper;

	// builder

	@BuilderParent
	ModelMetaSpec parent;

	@BuilderSource
	MessageSetTypeSpec spec;

	@BuilderTarget
	Model model;

	// build

	@BuildMethod
	public
	void build (
			@NonNull Builder builder) {

		try {

			createMessageSetType ();

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
	void createMessageSetType ()
		throws SQLException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageSetTypeBuilder.createMessageSetType ()",
				this);

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
