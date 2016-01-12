package wbs.sms.message.core.model;

import java.util.List;

import wbs.platform.service.model.ServiceRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

public
interface MessageDaoMethods {

	MessageRec findByOtherId (
			MessageDirection direction,
			RouteRec route,
			String otherId);

	List<MessageRec> findByThreadId (
			long threadId);

	List<MessageRec> findNotProcessed ();

	List<MessageRec> findRecentLimit (
			long maxResults);

	long countNotProcessed ();

	List<ServiceRec> projectServices (
			NumberRec number);

	List<Integer> searchIds (
			MessageSearch search);

}