package shn.product.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface ShnProductImageDaoMethods {

	List <Long> findIdsPendingImageNormalisationLimit (
			Transaction parentTransaction,
			Long maxResults);

}
