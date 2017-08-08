package wbs.services.messagetemplate.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.database.Transaction;

import wbs.platform.scaffold.model.SliceRec;

import wbs.services.messagetemplate.model.MessageTemplateDatabaseRec;
import wbs.services.messagetemplate.model.MessageTemplateSetRec;

public
interface MessageTemplateLogic {

	MessageTemplateDatabaseRec readMessageTemplateDatabaseFromClasspath (
			Transaction parentTransaction,
			SliceRec slice,
			String resourceName);

	Optional <String> lookupFieldValue (
			Transaction parentTransaction,
			MessageTemplateSetRec messageTemplateSet,
			String entryCode,
			String fieldCode,
			Map <String, String> mappings);

	default
	String lookupFieldValueRequired (
			@NonNull Transaction parentTransaction,
			@NonNull MessageTemplateSetRec messageTemplateSet,
			@NonNull String entryCode,
			@NonNull String fieldCode,
			@NonNull Map <String, String> mappings) {

		return optionalGetRequired (
			lookupFieldValue (
				parentTransaction,
				messageTemplateSet,
				entryCode,
				fieldCode,
				mappings));

	}

}
