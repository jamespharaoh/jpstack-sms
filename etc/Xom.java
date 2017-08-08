package wbs.utils.etc;

import nu.xom.Attribute;
import nu.xom.Element;

public
class Xom {

	public static
	Element xomElem (
			String type,
			Object... children) {

		Element element =
			new Element (
				type);

		xomAppend (
			element,
			children);

		return element;

	}

	public static
	Attribute xomAttr (
			String name,
			String value) {

		return new Attribute (
			name,
			value);

	}

	public static
	Attribute xomAttr (
			String name,
			String value,
			String url) {

		int colonPosition =
			name.indexOf(':');

		String prefix =
			name.substring (
				0,
				colonPosition);

		String localName =
			name.substring (
				colonPosition + 1);

		Attribute attribute =
			new Attribute (
				localName,
				value);

		attribute.setNamespace (
			prefix,
			url);

		return attribute;

	}

	public static
	void xomAppend (
			Element element,
			Object... children) {

		for (Object child
				: children) {

			if (child instanceof String) {

				element.appendChild (
					(String)
					child);

			} else if (child instanceof Attribute) {

				element.addAttribute (
					(Attribute)
					child);

			} else if (child instanceof Element) {

				element.appendChild (
					(Element)
					child);

			} else if (child instanceof XomNamespace) {

				XomNamespace namespace =
					(XomNamespace)
					child;

				element.addNamespaceDeclaration (
					namespace.prefix,
					namespace.url);

			} else {

				throw new RuntimeException (
					child.getClass ().getName ());

			}

		}

	}

	public static
	XomNamespace xomNamespace (
			String prefix,
			String url) {

		XomNamespace namespace =
			new XomNamespace ();

		namespace.prefix = prefix;
		namespace.url = url;

		return namespace;

	}

	public static
	class XomNamespace {
		String prefix;
		String url;
	}

}
