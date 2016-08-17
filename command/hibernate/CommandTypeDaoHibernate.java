package wbs.sms.command.hibernate;

import java.util.List;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.command.model.CommandTypeDao;
import wbs.sms.command.model.CommandTypeRec;

public
class CommandTypeDaoHibernate
	extends HibernateDao
	implements CommandTypeDao {

	@Override
	public
	List<CommandTypeRec> findAll () {

		return findMany (
			"findAll ()",
			CommandTypeRec.class,

			createCriteria (
				CommandTypeRec.class)

		);

	}

	@Override
	public
	CommandTypeRec findRequired (
			@NonNull Long commandTypeId) {

		return get (
			CommandTypeRec.class,
			commandTypeId);

	}

}
