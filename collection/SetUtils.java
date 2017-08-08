package wbs.utils.collection;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public
class SetUtils {

	public static <Type>
	Set <Type> emptySet () {

		return ImmutableSet.of ();

	}

}
