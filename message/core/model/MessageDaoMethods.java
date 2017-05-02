package wbs.sms.message.core.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.model.RouteRec;

public
interface MessageDaoMethods {

	MessageRec findByOtherId (
			Transaction parentTransaction,
			MessageDirection direction,
			RouteRec route,
			String otherId);

	List <MessageRec> findByThreadId (
			Transaction parentTransaction,
			Long threadId);

	List <MessageRec> findNotProcessed (
			Transaction parentTransaction);

	List <MessageRec> findRecentLimit (
			Transaction parentTransaction,
			Long maxResults);

	Long countNotProcessed (
			Transaction parentTransaction);

	List <ServiceRec> projectServices (
			Transaction parentTransaction,
			NumberRec number);

	List <Long> searchIds (
			Transaction parentTransaction,
			MessageSearch search);

}