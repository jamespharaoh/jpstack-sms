package wbs.utils.data;

import java.util.Map;

import lombok.NonNull;

public
interface Pair <Left, Right>
	extends Map.Entry <Left, Right> {

	Left left ();
	Right right ();

	static <Left, Right>
	Pair <Left, Right> of (
			@NonNull Left left,
			@NonNull Right right) {

		return new PairImplementation <Left, Right> (
			left,
			right);

	}

}
