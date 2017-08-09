package shn.product.daemon;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.ObjectDaemon;

import shn.product.logic.ShnProductLogic;
import shn.product.model.ShnProductImageObjectHelper;
import shn.product.model.ShnProductImageRec;

@SingletonComponent ("shnProductImageNormaliseObjectDaemonHelper")
public
class ShnProductImageNormaliseObjectDaemon
	implements ObjectDaemon <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ShnProductImageObjectHelper productImageHelper;

	@SingletonDependency
	ShnProductLogic productLogic;

	// details

	@Override
	public
	String backgroundProcessName () {
		return "shn-product-image.normalise";
	}

	@Override
	public
	String itemNameSingular () {
		return "product image";
	}

	@Override
	public
	String itemNamePlural () {
		return "product images";
	}

	@Override
	public
	LogContext logContext () {
		return logContext;
	}

	// public implementation

	@Override
	public
	List <Long> findObjectIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"findObjectIds");

		) {

			return productImageHelper.findIdsPendingImageNormalisationLimit (
				transaction,
				1024l);

		}

	}

	@Override
	public
	void processObject (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long productImageId) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"processObject");

		) {

			ShnProductImageRec productImage =
				productImageHelper.findRequired (
					transaction,
					productImageId);

			productImage

				.setNormalisedMedia (
					productLogic.normaliseImage (
						transaction,
						productImage.getOriginalMedia ()))

				.setNormaliseTime (
					transaction.now ())

			;

			transaction.commit ();

		}

	}

}
