package wbs.sms.magicnumber.console;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.forms.EntityFinder;
import wbs.sms.magicnumber.model.MagicNumberSetRec;

@SingletonComponent ("magicNumberSetEntityFinder")
public
class MagicNumberSetEntityFinder
	implements EntityFinder<MagicNumberSetRec> {

	@Inject
	MagicNumberSetConsoleHelper magicNumberSetHelper;

	@Override
	public
	MagicNumberSetRec findEntity (
			int id) {

		return magicNumberSetHelper.find (
			id);

	}

	@Override
	public
	List<MagicNumberSetRec> findEntities () {

		return magicNumberSetHelper.findAll ();

	}

}
