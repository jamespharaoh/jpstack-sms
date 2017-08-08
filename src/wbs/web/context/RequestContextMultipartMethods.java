package wbs.web.context;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

public
interface RequestContextMultipartMethods
	extends RequestContextCoreMethods {

	default
	ServletRequestContext fileUploadServletRequestContext () {

		State state =
			requestContextMultipartMethodsState ();

		if (
			isNull (
				state.fileUploadServletRequestContext)
		) {

			state.fileUploadServletRequestContext =
				new ServletRequestContext (
					request ());

		}

		return state.fileUploadServletRequestContext;

	}

	default
	State processMultipart () {

		State state =
			requestContextMultipartMethodsState ();

		if (
			isNull (
				state.fileItems)
		) {

			ServletFileUpload fileUpload =
				new ServletFileUpload (
					new DiskFileItemFactory ());

			List <FileItem> fileItemsTemp;

			try {

				fileItemsTemp =
					ImmutableList.<FileItem> copyOf (
						fileUpload.parseRequest (
							request ()));

			} catch (FileUploadException fileUploadException) {

				throw new RuntimeException (
					fileUploadException);

			}

			ImmutableMap.Builder <String, FileItem> fileItemFilesBuilder =
				ImmutableMap.builder ();

			ImmutableMap.Builder <String, String> fileItemFieldsBuilder =
				ImmutableMap.builder ();

			for (
				FileItem fileItem
					: fileItemsTemp
			) {

				if (fileItem.isFormField ()) {

					fileItemFieldsBuilder.put (
						fileItem.getFieldName (),
						fileItem.getString ());

				} else {

					fileItemFilesBuilder.put (
						fileItem.getFieldName (),
						fileItem);

				}

			}

			state.fileItemFiles =
				fileItemFilesBuilder.build ();

			state.fileItemFields =
				fileItemFieldsBuilder.build ();

			state.fileItems =
				fileItemsTemp;

		}

		return state;

	}

	default
	List <FileItem> fileItems () {
		return processMultipart ().fileItems;
	}

	default
	Map <String, FileItem> fileItemFiles () {
		return processMultipart ().fileItemFiles;
	}

	default
	Map <String, String> fileItemFields () {
		return processMultipart ().fileItemFields;
	}

	default
	Boolean isMultipart () {

		return FileUploadBase.isMultipartContent (
			fileUploadServletRequestContext ());

	}

	default
	FileItem fileItemFile (
			@NonNull String fieldName) {

		return fileItemFiles ().get (
			fieldName);

	}

	default
	String fileItemField (
			@NonNull String fieldName) {

		return fileItemFields ().get (
			fieldName);

	}

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_MULTIPART_METHODS_STATE";

	default
	State requestContextMultipartMethodsState () {

		return optionalOrElseRequired (
			optionalCast (
				State.class,
				optionalFromNullable (
					request ().getAttribute (
						STATE_KEY))),
			() -> {

			State state =
				new State ();

			request ().setAttribute (
				STATE_KEY,
				state);

			return state;

		});

	}

	static
	class State {
		ServletRequestContext fileUploadServletRequestContext;
		List <FileItem> fileItems;
		Map <String, FileItem> fileItemFiles;
		Map <String, String> fileItemFields;
	}

}
