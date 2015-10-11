package wbs.sms.messageset.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface MessageSetTypeDaoMethods {

	List<MessageSetTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}