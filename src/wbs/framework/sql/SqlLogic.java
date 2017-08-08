package wbs.framework.sql;

import static wbs.utils.string.StringUtils.stringFormatArray;

public
interface SqlLogic {

	String quoteIdentifier (
			String identifier);

	default
	String quoteIdentifierFormat (
			String ... identifierArguments) {

		return quoteIdentifier (
			stringFormatArray (
				identifierArguments));

	}

	String quoteString (
			String value);

	default
	String quoteStringFormat (
			String ... valueArguments) {

		return quoteString (
			stringFormatArray (
				valueArguments));

	}

}
