package wbs.sms.gazetteer.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import wbs.console.forms.types.FormFieldNativeMapping;
import wbs.console.helper.manager.ConsoleObjectManager;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;

@PrototypeComponent ("gazetteerCodeFormFieldNativeMapping")
public
class GazetteerCodeFormFieldNativeMapping <Container>
	implements FormFieldNativeMapping <Container, GazetteerEntryRec, String> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	Optional <String> genericToNative (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <GazetteerEntryRec> genericValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"genericToNative");

		) {

			if (
				optionalIsNotPresent (
					genericValue)
			) {
				return Optional.<String>absent ();
			}

			return Optional.of (
				genericValue.get ().getCode ());

		}

	}

	@Override
	public
	Optional <GazetteerEntryRec> nativeToGeneric (
			@NonNull Transaction parentTransaction,
			@NonNull Container container,
			@NonNull Optional <String> nativeValue) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"nativeToGeneric");

		) {

			if (
				optionalIsNotPresent (
					nativeValue)
			) {
				return Optional.<GazetteerEntryRec> absent ();
			}

			GazetteerRec gazetteer =
				(GazetteerRec)
				objectManager.dereferenceObsolete (
					transaction,
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
					transaction,
					gazetteer,
					nativeValue.get ());

			return Optional.of (
				entry);

		}

	}

}
