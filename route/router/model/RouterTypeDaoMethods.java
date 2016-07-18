package wbs.sms.route.router.model;

import java.util.List;

public
interface RouterTypeDaoMethods {

	RouterTypeRec findRequired (
			Long id);

	List<RouterTypeRec> findAll ();

}