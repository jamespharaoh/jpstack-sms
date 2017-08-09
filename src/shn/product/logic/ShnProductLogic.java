package shn.product.logic;

import java.util.Collection;
import java.util.List;

import wbs.framework.database.Transaction;

import wbs.platform.media.model.MediaRec;

import shn.product.model.ShnProductVariantValueRec;

public
interface ShnProductLogic {

	List <ShnProductVariantValueRec> sortVariantValues (
			Collection <ShnProductVariantValueRec> variantValues);

	MediaRec normaliseImage (
			Transaction parentTransaction,
			MediaRec originalMedia);

}
