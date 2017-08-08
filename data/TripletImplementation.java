package wbs.utils.data;

import lombok.NonNull;

public final
class TripletImplementation <First, Second, Third>
	implements Triplet <First, Second, Third> {

	// state

	private final
	First first;

	private final
	Second second;

	private final
	Third third;

	// constructors;

	public
	TripletImplementation (
			@NonNull First first,
			@NonNull Second second,
			@NonNull Third third) {

		this.first = first;
		this.second = second;
		this.third = third;

	}

	// accessors

	@Override
	public
	First first () {
		return first;
	}

	@Override
	public
	Second second () {
		return second;
	}

	@Override
	public
	Third third () {
		return third;
	}

}
