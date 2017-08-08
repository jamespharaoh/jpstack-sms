package wbs.framework.object;

import static wbs.utils.collection.CollectionUtils.singletonList;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.entity.record.Record;

public
interface ObjectHelperCodeMethods <
	RecordType extends Record <RecordType>
> {

	// find one

	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			Record <?> parent,
			List <String> codes);

	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> codes);

	RecordType findByCodeRequired (
			Transaction parentTransaction,
			Record <?> parent,
			List <String> codes);

	RecordType findByCodeRequired (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> codes);

	default
	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			Record <?> parent,
			String ... codes) {

		return findByCode (
			parentTransaction,
			parent,
			Arrays.asList (
				codes));

	}

	default
	RecordType findByCodeRequired (
			Transaction parentTransaction,
			Record <?> parent,
			String ... codes) {

		return findByCodeRequired (
			parentTransaction,
			parent,
			Arrays.asList (
				codes));

	}

	@Deprecated
	default
	RecordType findByCodeOrNull (
			Transaction parentTransaction,
			Record <?> parent,
			String ... codes) {

		return optionalOrNull (
			findByCode (
				parentTransaction,
				parent,
				Arrays.asList (
					codes)));

	}

	default
	Optional <RecordType> findByCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... codes) {

		return findByCode (
			parentTransaction,
			parentGlobalId,
			Arrays.asList (
				codes));

	}

	default
	RecordType findByCodeRequired (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... codes) {

		return findByCodeRequired (
			parentTransaction,
			parentGlobalId,
			Arrays.asList (
				codes));

	}

	default
	RecordType findByCodeOrThrow (
			@NonNull Transaction parentTransaction,
			@NonNull GlobalId parentGlobalId,
			@NonNull List <String> codes,
			@NonNull Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				parentGlobalId,
				codes);

		if (
			optionalIsPresent (
				recordOptional)
		) {

			return recordOptional.get ();

		} else {

			throw (RuntimeException)
				orThrow.get ();

		}

	}

	default
	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			Record <?> parent,
			List <String> codes,
			Supplier <? extends RuntimeException> orThrow) {

		Optional <RecordType> recordOptional =
			findByCode (
				parentTransaction,
				parent,
				codes);

		if (
			optionalIsPresent (
				recordOptional)
		) {

			return recordOptional.get ();

		} else {

			throw (RuntimeException)
				orThrow.get ();

		}

	}

	default
	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String code0,
			Supplier <? extends RuntimeException> orThrow) {

		return findByCodeOrThrow (
			parentTransaction,
			parentGlobalId,
			singletonList (
				code0),
			orThrow);

	}

	default
	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow) {

		return findByCodeOrThrow (
			parentTransaction,
			parentGlobalId,
			ImmutableList.of (
				code0,
				code1),
			orThrow);

	}

	default
	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			Record <?> parent,
			String code0,
			Supplier <? extends RuntimeException> orThrow) {

		return findByCodeOrThrow (
			parentTransaction,
			parent,
			singletonList (
				code0),
			orThrow);

	}

	default
	RecordType findByCodeOrThrow (
			Transaction parentTransaction,
			Record <?> parent,
			String code0,
			String code1,
			Supplier <? extends RuntimeException> orThrow) {

		return findByCodeOrThrow (
			parentTransaction,
			parent,
			ImmutableList.of (
				code0,
				code1),
			orThrow);

	}

	// find many

	List <Optional <RecordType>> findManyByCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> code);

	@Deprecated
	default
	RecordType findByCodeOrNull (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			List <String> codes) {

		return optionalOrNull (
			findByCode (
				parentTransaction,
				parentGlobalId,
				codes));

	}

	@Deprecated
	default
	RecordType findByCodeOrNull (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String ... codes) {

		return optionalOrNull (
			findByCode (
				parentTransaction,
				parentGlobalId,
				Arrays.asList (
					codes)));

	}

	RecordType findByTypeAndCode (
			Transaction parentTransaction,
			Record <?> parent,
			String typeCode,
			String ... code);

	RecordType findByTypeAndCode (
			Transaction parentTransaction,
			GlobalId parentGlobalId,
			String typeCode,
			String ... code);

}
