package wbs.sms.command.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface CommandTypeDaoMethods {

	List<CommandTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}