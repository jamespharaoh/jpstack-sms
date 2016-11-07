package wbs.sms.gazetteer.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.console.forms.FormFieldNativeMapping;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;

@PrototypeComponent ("gazetteerCodeFormFieldNativeMapping")
public
class GazetteerCodeFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, GazetteerEntryRec, String> {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	GazetteerEntryConsoleHelper gazetteerEntryHelper;

	// properties

	@Getter @Setter
	String gazetteerFieldName;

	// implementation

	@Override
	public
	Optional<String> genericToNative (
			@NonNull Container container,
			@NonNull Optional<GazetteerEntryRec> genericValue) {

		if (
			optionalIsNotPresent (
				genericValue)
		) {
			return Optional.<String>absent ();
		}

		return Optional.of (
			genericValue.get ().getCode ());

	}

	@Override
	public
	Optional<GazetteerEntryRec> nativeToGeneric (
			@NonNull Container container,
			@NonNull Optional<String> nativeValue) {

		if (
			optionalIsNotPresent (
				nativeValue)
		) {
			return Optional.<GazetteerEntryRec>absent ();
		}

		GazetteerRec gazetteer =
			(GazetteerRec)
			objectManager.dereference (
				container,
				gazetteerFieldName);

		if (
			isNull (
				gazetteer)
		) {
			throw new RuntimeException ();
		}

		GazetteerEntryRec entry =
			gazetteerEntryHelper.findByCodeRequired (
				gazetteer,
				nativeValue.get ());

		return Optional.of (
			entry);

	}

}
