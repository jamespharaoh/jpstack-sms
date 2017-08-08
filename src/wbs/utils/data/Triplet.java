package wbs.utils.data;

import lombok.NonNull;

public
interface Triplet <First, Second, Third> {

	First first ();
	Second second ();
	Third third ();

	static <First, Second, Third>
	Triplet <First, Second, Third> of (
			@NonNull First first,
			@NonNull Second second,
			@NonNull Third third) {

		return new TripletImplementation <First, Second, Third> (
			first,
			second,
			third);

	}

}
