package wbs.sms.route.router.model;

import java.util.List;

import wbs.platform.object.core.model.ObjectTypeRec;

public
interface RouterDaoMethods {

	List<RouterTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType);

}