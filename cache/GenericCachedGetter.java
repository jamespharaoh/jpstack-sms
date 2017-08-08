package wbs.utils.cache;

import static wbs.utils.time.TimeUtils.earlierThan;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

public
class GenericCachedGetter <Context, Type>
	implements CachedGetter <Context, Type> {

	// state

	private final
	BiFunction <Context, Function <Context, Type>, Type> getWrapper;

	private final
	Function <Context, Type> provider;

	private final
	Duration reloadFrequency;

	private
	Type value;

	private
	Instant lastReload =
		millisToInstant (
			0l);

	// constructors

	public
	GenericCachedGetter (
			@NonNull BiFunction <Context, Function <Context, Type>, Type>
				getWrapper,
			@NonNull Function <Context, Type> provider,
			@NonNull Duration reloadFrequency) {

		this.getWrapper =
			getWrapper;

		this.provider =
			provider;

		this.reloadFrequency =
			reloadFrequency;

	}

	// implementation

	@Override
	public
	Type get (
			@NonNull Context outerContext) {

		return getWrapper.apply (
			outerContext,
			getContext -> {

			if (
				earlierThan (
					lastReload.plus (
						reloadFrequency),
					Instant.now ())
			) {

				value =
					provider.apply (
						getContext);

				lastReload =
					Instant.now ();

			}

			return value;

		});

	}

}
