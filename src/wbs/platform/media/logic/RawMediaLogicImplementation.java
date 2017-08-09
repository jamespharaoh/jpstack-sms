package wbs.platform.media.logic;

import static wbs.utils.etc.Misc.iterable;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.runFilter;
import static wbs.utils.etc.Misc.runFilterAdvanced;
import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.io.RuntimeIoException;

@SingletonComponent ("rawMediaLogicImplementation")
public
class RawMediaLogicImplementation
	implements RawMediaLogic {

	// singleton components

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Optional <BufferedImage> readImage (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data,
			@NonNull String mimeType) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"readImage");

		) {

			for (
				ImageReader imageReader
					: iterable (
						ImageIO.getImageReadersByMIMEType (
							mimeType))
			) {

				taskLogger.debugFormat (
					"Attempt to read image of type %s with %s bytes with %s",
					mimeType,
					integerToDecimalString (
						data.length),
					imageReader.toString ());

				imageReader.setInput (
					new ByteArrayImageInputStream (
						data));

				try {

					return Optional.of (
						imageReader.read (0));

				} catch (IOException exception) {

					taskLogger.warningFormatException (
						exception,
						"Failed to read image of type %s with %s bytes",
						mimeType,
						integerToDecimalString (
							data.length));

				}

			}

			taskLogger.warningFormat (
				"Exhausted options to read image of type %s with %s bytes",
				mimeType,
				integerToDecimalString (
					data.length));

			return optionalAbsent ();

		}

	}

	@Override
	public
	byte[] writeImage (
			@NonNull BufferedImage image,
			@NonNull String mimeType) {

		for (
			ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						mimeType))
		) {

			try (

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

				MemoryCacheImageOutputStream imageOutputStream =
					new MemoryCacheImageOutputStream (
						byteArrayOutputStream);

			) {

				imageWriter.setOutput (
					imageOutputStream);

				try {

					imageWriter.write (
						new IIOImage (
							image,
							null,
							null));

					return byteArrayOutputStream.toByteArray ();

				} catch (IOException exception) {

				}

			} catch (IOException ioException) {

				throw new RuntimeException (
					ioException);

			}

		}

		throw new RuntimeException ();

	}

	@Override
	public
	byte[] writeJpeg (
			@NonNull BufferedImage image,
			float jpegQuality) {

		for (
			ImageWriter imageWriter
				: iterable (
					ImageIO.getImageWritersByMIMEType (
						"image/jpeg"))
		) {

			try (

				ByteArrayOutputStream byteArrayOutputStream =
					new ByteArrayOutputStream ();

				MemoryCacheImageOutputStream imageOutputStream =
					new MemoryCacheImageOutputStream (
							byteArrayOutputStream);

			) {

				imageWriter.setOutput (
					imageOutputStream);

				ImageWriteParam imageWriteParam =
					imageWriter.getDefaultWriteParam ();

				imageWriteParam.setCompressionMode (
					ImageWriteParam.MODE_EXPLICIT);

				imageWriteParam.setCompressionQuality (
					jpegQuality);

				try {

					imageWriter.write (
						null,
						new IIOImage (
							image,
							null,
							null),
						imageWriteParam);

					return byteArrayOutputStream.toByteArray ();

				} catch (IOException exception) {

				}

			} catch (IOException ioException) {

				throw new RuntimeIoException (
					ioException);

			}

		}

		throw new RuntimeException ();

	}

	@Override
	public
	BufferedImage resampleImageToFit (
			@NonNull BufferedImage image,
			@NonNull Long maxWidth,
			@NonNull Long maxHeight) {

		// same image if already fits

		if (

			lessThan (
				image.getWidth (),
				maxWidth)

			&& lessThan (
				image.getHeight (),
				maxHeight)

		) {

			return image;

		}

		// start with max target size

		long targetWidth =
			image.getWidth ();

		long targetHeight =
			image.getHeight ();

		// reduce height to maintain ratio

		if (targetWidth > maxWidth) {

			targetWidth =
				maxWidth;

			targetHeight =
				image.getHeight () * maxWidth / image.getWidth ();

		}

		// reduce width to maintain ratio

		if (targetHeight > maxHeight) {

			targetHeight =
				maxHeight;

			targetWidth =
				image.getWidth () * maxHeight / image.getHeight ();

		}

		// determine image type

		int imageType =
			image.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		// resample image

		BufferedImage targetImage =
			new BufferedImage (
				toJavaIntegerRequired (
					targetWidth),
				toJavaIntegerRequired (
					targetHeight),
				imageType);

		Graphics2D graphics =
			targetImage.createGraphics ();

		graphics.drawImage (
			image,
			0,
			0,
			toJavaIntegerRequired (
				targetWidth),
			toJavaIntegerRequired (
				targetHeight),
			0,
			0,
			image.getWidth (),
			image.getHeight (),
			null);

		graphics.dispose ();

		return targetImage;

	}

	@Override
	public
	BufferedImage cropAndResampleImage (
			@NonNull BufferedImage sourceImage,
			@NonNull Long targetWidth,
			@NonNull Long targetHeight) {

		// same image if already fits

		if (

			integerEqualSafe (
				sourceImage.getWidth (),
				targetWidth)

			&& integerEqualSafe (
				sourceImage.getHeight (),
				targetHeight)

		) {
			return sourceImage;
		}

		// select crop type

		long sourceRatio =
			sourceImage.getWidth ()
				* targetHeight;

		long targetRatio =
			targetWidth
				* sourceImage.getHeight ();

		long sourceWidth;
		long sourceHeight;

		if (
			integerEqualSafe (
				sourceRatio,
				targetRatio)
		) {

			// keep same ratio

			sourceWidth =
				sourceImage.getWidth ();

			sourceHeight =
				sourceImage.getHeight ();

		} else if (
			moreThan (
				sourceRatio,
				targetRatio)
		) {

			// reduce width

			sourceWidth =
				sourceImage.getHeight ()
					* targetWidth
					/ targetHeight;

			sourceHeight =
				sourceImage.getHeight ();

		} else if (
			lessThan (
				sourceRatio,
				targetRatio)
		) {

			// reduce height

			sourceWidth =
				sourceImage.getWidth ();

			sourceHeight =
				sourceImage.getWidth ()
					* targetHeight
					/ targetWidth;

		} else {

			throw shouldNeverHappen ();

		}

		long sourceOffsetHorizontal =
			(sourceImage.getWidth () - sourceWidth) / 2l;

		long sourceOffsetVertical =
			(sourceImage.getHeight () - sourceHeight) / 2l;

		// determine image type

		int imageType =
			sourceImage.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		// resample image

		BufferedImage targetImage =
			new BufferedImage (
				toJavaIntegerRequired (
					targetWidth),
				toJavaIntegerRequired (
					targetHeight),
				imageType);

		Graphics2D graphics =
			targetImage.createGraphics ();

		graphics.drawImage (
			sourceImage,
			0,
			0,
			toJavaIntegerRequired (
				targetWidth),
			toJavaIntegerRequired (
				targetHeight),
			toJavaIntegerRequired (
				sourceOffsetHorizontal),
			toJavaIntegerRequired (
				sourceOffsetVertical),
			toJavaIntegerRequired (
				sourceOffsetHorizontal + sourceWidth),
			toJavaIntegerRequired (
				sourceOffsetVertical + sourceHeight),
			null);

		graphics.dispose ();

		return targetImage;

	}

	@Override
	public
	BufferedImage padAndResampleImage (
			@NonNull BufferedImage sourceImage,
			@NonNull Long targetWidth,
			@NonNull Long targetHeight,
			@NonNull Color paddingColour) {

		// same image if already fits

		if (

			integerEqualSafe (
				sourceImage.getWidth (),
				targetWidth)

			&& integerEqualSafe (
				sourceImage.getHeight (),
				targetHeight)

		) {
			return sourceImage;
		}

		// select crop type

		long sourceRatio =
			sourceImage.getWidth ()
				* targetHeight;

		long targetRatio =
			targetWidth
				* sourceImage.getHeight ();

		long scaleWidth;
		long scaleHeight;

		if (
			integerEqualSafe (
				sourceRatio,
				targetRatio)
		) {

			// keep same ratio

			scaleWidth =
				targetWidth;

			scaleHeight =
				targetHeight;

		} else if (
			moreThan (
				sourceRatio,
				targetRatio)
		) {

			// calclate height

			scaleWidth =
				targetWidth;

			scaleHeight =
				targetWidth
					* sourceImage.getHeight ()
					/ sourceImage.getWidth ();

		} else if (
			lessThan (
				sourceRatio,
				targetRatio)
		) {

			// calculate width

			scaleWidth =
				targetWidth
					* sourceImage.getWidth ()
					/ sourceImage.getHeight ();

			scaleHeight =
				targetHeight;

		} else {

			throw shouldNeverHappen ();

		}

		long offsetHorizontal =
			(targetWidth - scaleWidth) / 2l;

		long offsetVertical =
			(targetHeight - scaleHeight) / 2l;

		// determine image type

		int imageType =
			sourceImage.getType ();

		if (imageType == BufferedImage.TYPE_CUSTOM) {

			imageType =
				BufferedImage.TYPE_INT_RGB;

		}

		// pad and resample image

		BufferedImage targetImage =
			new BufferedImage (
				toJavaIntegerRequired (
					targetWidth),
				toJavaIntegerRequired (
					targetHeight),
				imageType);

		Graphics2D graphics =
			targetImage.createGraphics ();

		graphics.setColor (
			paddingColour);

		graphics.fillRect (
			0,
			0,
			toJavaIntegerRequired (
				targetWidth),
			toJavaIntegerRequired (
				targetHeight));

		graphics.drawImage (
			sourceImage,
			toJavaIntegerRequired (
				offsetHorizontal),
			toJavaIntegerRequired (
				offsetVertical),
			toJavaIntegerRequired (
				offsetHorizontal + targetWidth),
			toJavaIntegerRequired (
				offsetVertical + targetHeight),
			0,
			0,
			sourceImage.getWidth (),
			sourceImage.getHeight (),
			null);

		graphics.dispose ();

		return targetImage;

	}

	@Override
	public
	BufferedImage rotateImage90 (
			@NonNull BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getHeight (),
				image.getWidth (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			image.getHeight(),
			0);

		graphics.rotate (
			Math.PI / 2);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	@Override
	public
	BufferedImage rotateImage180 (
			BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getWidth (),
				image.getHeight (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			image.getWidth (),
			image.getHeight ());

		graphics.rotate (
			Math.PI);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	@Override
	public
	BufferedImage rotateImage270 (
			@NonNull BufferedImage image) {

		BufferedImage newImage =
			new BufferedImage (
				image.getHeight (),
				image.getWidth (),
				image.getType ());

		Graphics2D graphics =
			newImage.createGraphics ();

		graphics.translate (
			0,
			image.getWidth ());

		graphics.rotate (
			- Math.PI / 2);

		graphics.drawImage (
			image,
			0,
			0,
			null);

		graphics.dispose ();

		return newImage;

	}

	@Override
	public
	Optional <byte[]> videoConvert (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull String profileName,
			@NonNull byte[] data) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoConvert");

		) {

			FfmpegProfile ffmpegProfile =
				FfmpegProfileData.profiles.get (
					profileName);

			try {

				return optionalOf (
					runFilterAdvanced (
						taskLogger,
						data,
						"",
						"." + ffmpegProfile.fileExtension,
						ffmpegProfile.toFfmpeg ()));

			} catch (Exception exception) {

				return optionalAbsent ();

			}

		}

	}

	@Override
	public
	Optional <byte[]> videoFrameBytes (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoFrameBytes");

		) {

			return optionalOf (
				runFilter (
					taskLogger,
					data,
					".mp4",
					".mjpeg",
					"ffmpeg",
					"-y",
					"-i",
					"<in>",
					"-vcodec",
					"mjpeg",
					"-vframes",
					"1",
					"<out>"));

		} catch (InterruptedException interruptedException) {

			Thread.currentThread ().interrupt ();

			return optionalAbsent ();

		}

	}

	@Override
	public
	Optional <BufferedImage> videoFrame (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull byte[] data) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"videoFrame");

		) {

			Optional <byte[]> videoFrameBytesOptional =
				videoFrameBytes (
					taskLogger,
					data);

			if (
				optionalIsNotPresent (
					videoFrameBytesOptional)
			) {
				return optionalAbsent ();
			}

			return readImage (
				taskLogger,
				videoFrameBytesOptional.get (),
				"image/jpeg");

		}

	}

}
