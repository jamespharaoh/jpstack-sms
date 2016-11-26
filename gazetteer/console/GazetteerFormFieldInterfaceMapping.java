package wbs.sms.gazetteer.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.ResultUtils.errorResult;
import static wbs.utils.etc.ResultUtils.errorResultFormat;
import static wbs.utils.etc.ResultUtils.successResult;
import static wbs.utils.string.CodeUtils.simplifyToCode;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringIsEmpty;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.forms.FormFieldInterfaceMapping;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;

import fj.data.Either;

@Accessors (fluent = true)
@PrototypeComponent ("gazetteerFormFieldInterfaceMapping")
public
class GazetteerFormFieldInterfaceMapping <Container>
	implements FormFieldInterfaceMapping <
		Container,
		GazetteerEntryRec,
		String
	> {

	// singleton dependencies

	@SingletonDependency
	GazetteerEntryConsoleHelper gazetteerEntryHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	// properties

	@Getter @Setter
	String gazetteerFieldName;

	// implementation

	@Override
	public
	Either <Optional <GazetteerEntryRec>, String> interfaceToGeneric (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<String> interfaceValue) {

		if (

			optionalIsNotPresent (
				interfaceValue)

			|| stringIsEmpty (
				interfaceValue.get ())

		) {

			return successResult (
				optionalAbsent ());

		}

		GazetteerRec gazetteer =
			(GazetteerRec)
			objectManager.dereferenceObsolete (
				container,
				gazetteerFieldName);

		if (
			isNull (
				gazetteer)
		) {

			return errorResultFormat (
				"You must configure a gazetteer first");

		}

		Optional<String> codeOptional =
			simplifyToCode (
				interfaceValue.get ());

		if (
			optionalIsNotPresent (
				codeOptional)
		) {

			return errorResult (
				stringFormat (
					"Location not found"));

		}

		Optional<GazetteerEntryRec> entryOptional =
			gazetteerEntryHelper.findByCode (
				gazetteer,
				codeOptional.get ());

		if (
			optionalIsNotPresent (
				entryOptional)
		) {

			return errorResult (
				stringFormat (
					"Location not found"));

		}

		GazetteerEntryRec entry =
			entryOptional.get ();

		return successResult (
			Optional.of (
				entry));

	}

	@Override
	public
	Either<Optional<String>,String> genericToInterface (
			@NonNull Container container,
			@NonNull Map<String,Object> hints,
			@NonNull Optional<GazetteerEntryRec> genericValue) {

		if (
			optionalIsPresent (
				genericValue)
		) {

			return successResult (
				Optional.of (
					genericValue.get ().getName ()));

		} else {

			return successResult (
				Optional.<String>absent ());

		}

	}

}
