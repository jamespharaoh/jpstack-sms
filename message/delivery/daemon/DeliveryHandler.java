package wbs.sms.message.delivery.daemon;

import java.util.Collection;

public interface DeliveryHandler {

	Collection<String> getDeliveryTypeCodes ();

	void handle (
			int deliveryNoticeQueueId,
			Integer ref);

}
