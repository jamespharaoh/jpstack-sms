package wbs.sms.customer.model;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.object.AbstractObjectHooks;

public
class SmsCustomerHooks
	extends AbstractObjectHooks<SmsCustomerRec> {

	@Inject
	SmsCustomerDao smsCustomerDao;

	@Override
	public
	List<Integer> searchIds (
			Object searchObject) {

		SmsCustomerSearch search =
			(SmsCustomerSearch)
			searchObject;

		return smsCustomerDao.searchIds (
			search);

	}

}