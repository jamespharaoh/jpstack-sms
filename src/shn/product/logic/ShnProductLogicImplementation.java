package shn.product.logic;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.media.logic.MediaLogic;
import wbs.platform.media.logic.RawMediaLogic;
import wbs.platform.media.model.MediaRec;

import shn.product.model.ShnProductVariantValueRec;

@SingletonComponent ("shnProductLogic")
public
class ShnProductLogicImplementation
	implements ShnProductLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MediaLogic mediaLogic;

	@SingletonDependency
	RawMediaLogic rawMediaLogic;

	// public implementation

	@Override
	public
	List <ShnProductVariantValueRec> sortVariantValues (
			@NonNull Collection <ShnProductVariantValueRec>
				variantValuesUnsorted) {

		List <ShnProductVariantValueRec> variantValuesSorted =
			new ArrayList<> (
				variantValuesUnsorted);

		Collections.sort (
			variantValuesSorted,
			Ordering.compound (
				ImmutableList.of (

			Ordering.natural ().onResultOf (
				variantValue ->
					variantValue.getType ().getCode ()),

			Ordering.natural ().onResultOf (
				variantValue ->
					variantValue.getCode ())

		)));

		return variantValuesSorted;

	}

	@Override
	public
	MediaRec normaliseImage (
			@NonNull Transaction parentTransaction,
			@NonNull MediaRec originalMedia) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processImage");

		) {

			BufferedImage originalImage =
				rawMediaLogic.readImageRequired (
					transaction,
					originalMedia.getContent ().getData (),
					originalMedia.getMediaType ().getMimeType ());

			BufferedImage processedImage =
				rawMediaLogic.padAndResampleImage (
					originalImage,
					1024l,
					1024l,
					Color.white);

			return mediaLogic.createMediaFromImageRequired (
				transaction,
				rawMediaLogic.writeImage (
					processedImage,
					originalMedia.getMediaType ().getMimeType ()),
				originalMedia.getMediaType ().getMimeType (),
				originalMedia.getFilename ());

		}

	}

}
