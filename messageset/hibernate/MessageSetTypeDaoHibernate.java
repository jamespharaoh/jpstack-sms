package wbs.sms.messageset.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.messageset.model.MessageSetTypeDao;
import wbs.sms.messageset.model.MessageSetTypeRec;

public
class MessageSetTypeDaoHibernate
	extends HibernateDao
	implements MessageSetTypeDao {

	@Override
	public
	List<MessageSetTypeRec> findAll () {

		return findMany (
			"findAll ()",
			MessageSetTypeRec.class,

			createCriteria (
				MessageSetTypeRec.class)

		);

	}

	@Override
	public
	MessageSetTypeRec findRequired (
			@NonNull Long messageSetTypeId) {

		return get (
			MessageSetTypeRec.class,
			(int) (long) messageSetTypeId);

	}

}
