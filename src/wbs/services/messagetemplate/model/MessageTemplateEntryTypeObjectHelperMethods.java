package wbs.services.messagetemplate.model;

import java.util.function.Consumer;

import wbs.framework.database.Transaction;

public
interface MessageTemplateEntryTypeObjectHelperMethods {

	MessageTemplateEntryTypeRec findOrCreate (
			Transaction parentTransaction,
			MessageTemplateDatabaseRec messageTemplateDatabase,
			String code,
			Consumer <MessageTemplateEntryTypeRec> consumer);

}
