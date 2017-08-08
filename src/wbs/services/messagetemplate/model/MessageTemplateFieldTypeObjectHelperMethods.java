package wbs.services.messagetemplate.model;

import java.util.function.Consumer;

import wbs.framework.database.Transaction;

public
interface MessageTemplateFieldTypeObjectHelperMethods {

	MessageTemplateFieldTypeRec findOrCreate (
			Transaction parentTransaction,
			MessageTemplateEntryTypeRec messageTemplateEntryType,
			String code,
			Consumer <MessageTemplateFieldTypeRec> consumer);

}
