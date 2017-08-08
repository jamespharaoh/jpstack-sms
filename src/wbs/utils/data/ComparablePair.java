package wbs.utils.data;

import lombok.NonNull;

public
interface ComparablePair <
	Left extends Comparable <?>,
	Right extends Comparable <?>
>
	extends
		Comparable <ComparablePair <Left, Right>>,
		Pair <Left, Right> {

	static <Left extends Comparable <?>, Right extends Comparable <?>>
	ComparablePair <Left, Right> of (
			@NonNull Left left,
			@NonNull Right right) {

		return new ComparablePairImplementation <Left, Right> (
			left,
			right);

	}

}
