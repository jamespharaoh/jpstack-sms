package wbs.sms.message.stats.logic;

import wbs.sms.message.stats.model.MessageStatsData;

public
interface MessageStatsLogic {

	void addTo (
			MessageStatsData target,
			MessageStatsData difference);

}
