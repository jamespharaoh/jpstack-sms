package wbs.web.utils;

import static wbs.utils.collection.CollectionUtils.listLastItemRequired;
import static wbs.utils.collection.CollectionUtils.listSliceAllButLastItemRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithSpace;
import static wbs.web.utils.HtmlAttributeUtils.htmlStyleAttribute;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.utils.string.FormatWriter;

import wbs.web.utils.HtmlAttributeUtils.HtmlAttribute;
import wbs.web.utils.HtmlAttributeUtils.ToHtmlAttribute;

public
class HtmlStyleUtils {

	public static
	void htmlStyleBlockOpen (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<style type=\"text/css\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlStyleBlockClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</style>");

	}

	public static
	void htmlStyleRuleOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull List <String> selectors) {

		for (
			String selector
				: listSliceAllButLastItemRequired (
					selectors)
		) {

			formatWriter.writeLineFormat (
				"%s,",
				selector);

		}

		formatWriter.writeLineFormat (
			"%s {",
			listLastItemRequired (
				selectors));

		formatWriter.increaseIndent ();

	}

	public static
	void htmlStyleRuleOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... selectors) {

		htmlStyleRuleOpen (
			formatWriter,
			Arrays.asList (
				selectors));

	}

	public static
	void htmlStyleRuleClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"}");

	}

	public static
	void htmlStyleRuleWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlStyleRule rule) {

		htmlStyleRuleOpen (
			formatWriter,
			rule.selectors ());

		rule.entries.forEach (
			entry ->
				htmlStyleRuleEntryWrite (
					formatWriter,
					entry));

		htmlStyleRuleClose (
			formatWriter);

	}

	public static
	void htmlStyleRuleWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String selector0,
			@NonNull HtmlStyleRuleEntry ... entries) {

		htmlStyleRuleOpen (
			formatWriter,
			selector0);

		Arrays.asList (entries).forEach (
			entry ->
				htmlStyleRuleEntryWrite (
					formatWriter,
					entry));

		htmlStyleRuleClose (
			formatWriter);

	}

	public static
	void htmlStyleRuleWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull HtmlStyleRuleEntry ... entries) {

		htmlStyleRuleOpen (
			formatWriter,
			selector0,
			selector1);

		Arrays.asList (entries).forEach (
			entry ->
				htmlStyleRuleEntryWrite (
					formatWriter,
					entry));

		htmlStyleRuleClose (
			formatWriter);

	}

	public static
	void htmlStyleRuleWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull String selector2,
			@NonNull HtmlStyleRuleEntry ... entries) {

		htmlStyleRuleOpen (
			formatWriter,
			selector0,
			selector1,
			selector2);

		Arrays.asList (entries).forEach (
			entry ->
				htmlStyleRuleEntryWrite (
					formatWriter,
					entry));

		htmlStyleRuleClose (
			formatWriter);

	}

	public static
	void htmlStyleRuleWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull String selector2,
			@NonNull String selector3,
			@NonNull HtmlStyleRuleEntry ... entries) {

		htmlStyleRuleOpen (
			formatWriter,
			selector0,
			selector1,
			selector2,
			selector3);

		Arrays.asList (entries).forEach (
			entry ->
				htmlStyleRuleEntryWrite (
					formatWriter,
					entry));

		htmlStyleRuleClose (
			formatWriter);

	}

	public static
	void htmlStyleRuleEntryWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlStyleRuleEntry entry) {

		formatWriter.writeLineFormat (
			"%h: %h;",
			entry.name (),
			joinWithSpace (
				entry.values ()));

	}

	public static
	void htmlStyleRuleEntryWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value) {

		formatWriter.writeLineFormat (
			"%h: %h;",
			name,
			value);

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull Iterable <String> selectors,
			@NonNull Iterable <HtmlStyleRuleEntry> entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.copyOf (
					selectors))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull Iterable <String> selectors,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.copyOf (
					selectors))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull String selector0,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.of (
					selector0))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.of (
					selector0,
					selector1))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull String selector2,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.of (
					selector0,
					selector1,
					selector2))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull String selector2,
			@NonNull String selector3,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.of (
					selector0,
					selector1,
					selector2,
					selector3))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRule htmlStyleRule (
			@NonNull String selector0,
			@NonNull String selector1,
			@NonNull String selector2,
			@NonNull String selector3,
			@NonNull String selector4,
			@NonNull HtmlStyleRuleEntry ... entries) {

		return new HtmlStyleRule ()

			.selectors (
				ImmutableList.of (
					selector0,
					selector1,
					selector2,
					selector3,
					selector4))

			.entries (
				ImmutableList.copyOf (
					entries));

	}

	public static
	HtmlStyleRuleEntry htmlStyleRuleEntry (
			@NonNull String name,
			@NonNull String ... values) {

		return new HtmlStyleRuleEntry ()

			.name (
				name)

			.values (
				ImmutableList.copyOf (
					values));

	}

	@Accessors (fluent = true)
	@Data
	public static
	class HtmlStyleRule {
		List <String> selectors;
		List <HtmlStyleRuleEntry> entries;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class HtmlStyleRuleEntry
		implements
			ToHtmlAttribute,
			ToHtmlStyleRuleEntry {

		String name;
		List <String> values;

		@Override
		public
		Optional <HtmlAttribute> htmlAttribute () {

			return optionalOf (
				htmlStyleAttribute (
					this));

		}

		@Override
		public
		Optional <HtmlStyleRuleEntry> htmlStyleRuleEntry () {

			return optionalOf (
				this);

		}

	}

	public static
	interface ToHtmlStyleRuleEntry {

		Optional <HtmlStyleRuleEntry> htmlStyleRuleEntry ();

	}

}
