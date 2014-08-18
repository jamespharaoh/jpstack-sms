package wbs.sms.magicnumber.logic;

import java.util.Collection;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.platform.text.model.TextRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.message.batch.model.BatchRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.router.model.RouterRec;

public
interface MagicNumberLogic {

	MagicNumberRec allocateMagicNumber (
			MagicNumberSetRec magicNumberSet,
			NumberRec number,
			CommandRec command,
			int ref);

	MessageRec sendMessage (
			MagicNumberSetRec magicNumberSet,
			NumberRec number,
			CommandRec magicCommand,
			int magicRef,
			Integer threadId,
			TextRec messageText,
			RouterRec router,
			ServiceRec service,
			BatchRec batch,
			AffiliateRec affiliate);

	Integer sendMessage (
			MagicNumberSetRec magicNumberSet,
			NumberRec number,
			CommandRec magicCommand,
			Integer magicRef,
			Integer threadId,
			Collection<TextRec> parts,
			RouterRec router,
			ServiceRec service,
			BatchRec batch,
			AffiliateRec affiliate);

}
