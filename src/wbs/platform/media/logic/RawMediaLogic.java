package wbs.platform.media.logic;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface RawMediaLogic {

	Optional <BufferedImage> readImage (
			TaskLogger parentTaskLogger,
			byte[] data,
			String mimeType);

	default
	BufferedImage readImageRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType) {

		return optionalGetRequired (
			readImage (
				parentTaskLogger,
				data,
				mimeType));

	}


	byte[] writeImage (
			BufferedImage image,
			String mimeType);

	byte[] writeJpeg (
			BufferedImage image,
			float jpegQuality);

	BufferedImage resampleImageToFit (
			BufferedImage image,
			Long maxWidth,
			Long maxHeight);

	BufferedImage cropAndResampleImage (
			BufferedImage image,
			Long maxWidth,
			Long maxHeight);

	BufferedImage padAndResampleImage (
			BufferedImage image,
			Long maxWidth,
			Long maxHeight,
			Color paddingColour);

	BufferedImage rotateImage90 (
			BufferedImage image);

	BufferedImage rotateImage180 (
			BufferedImage image);

	BufferedImage rotateImage270 (
			BufferedImage image);

	Optional <byte[]> videoConvert (
			TaskLogger parentTaskLogger,
			String profileName,
			byte[] data);

	default
	byte[] videoConvertRequired (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String profileName,
			@NonNull byte[] data) {

		return optionalGetRequired (
			videoConvert (
				parentTaskLogger,
				profileName,
				data));

	}

	Optional <byte[]> videoFrameBytes (
			TaskLogger parentTaskLogger,
			byte[] data);

	Optional <BufferedImage> videoFrame (
			TaskLogger parentTaskLogger,
			byte[] data);


}
