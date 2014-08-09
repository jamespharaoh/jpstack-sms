package wbs.sms.command.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.command.model.CommandTypeDao;
import wbs.sms.command.model.CommandTypeRec;

public
class CommandTypeDaoHibernate
	extends HibernateDao
	implements CommandTypeDao {

	@Override
	public
	List<CommandTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			CommandTypeRec.class,

			createQuery (
				"FROM CommandTypeRec ct " +
				"WHERE ct.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}
