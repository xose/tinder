package org.xmpp.util;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class BaseXML implements Cloneable {

	private static final DocumentBuilder docBuilder;

	static {
		final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

		docBuilderFactory.setIgnoringElementContentWhitespace(true);
		docBuilderFactory.setIgnoringComments(true);

		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			throw new InternalError("Error creating Document Builder");
		}
	}

	protected final Document document;
	protected final Element element;

	/**
	 * Constructs a new Packet with no element data. This method is used by
	 * extensions of this class that require a more optimized path for creating
	 * new packets.
	 */
	protected BaseXML(final String elementName) {
		this(elementName, XMLConstants.NULL_NS_URI);
	}

	/**
	 * Constructs a new Packet with no element data. This method is used by
	 * extensions of this class that require a more optimized path for creating
	 * new packets.
	 */
	protected BaseXML(final String qualifiedName, final String namespaceURI) {
		document = docBuilder.newDocument();
		element = document.createElementNS(namespaceURI, qualifiedName);
	}

	/**
	 * Constructs a new Packet. The TO address contained in the XML Element will
	 * only be validated. In other words, stringprep operations will only be
	 * performed on the TO JID to verify that it is well-formed. The FROM
	 * address is assigned by the server so there is no need to verify it.
	 * 
	 * @param element
	 *            the XML Element that contains the packet contents.
	 */
	protected BaseXML(final Element element) {
		document = docBuilder.newDocument();
		this.element = (Element) document.importNode(element, true);
	}

	protected final static Iterable<Element> getChildElements(final Element element) {
		return getChildElements(element, "*", "*");
	}

	protected final static Iterable<Element> getChildElements(final Element element, final String name) {
		return getChildElements(element, name, XMLConstants.NULL_NS_URI);
	}

	protected final static Iterable<Element> getChildElements(final Element element, final String localName, final String namespaceURI) {
		return new Iterable<Element>() {

			@Override
			public Iterator<Element> iterator() {
				return new Iterator<Element>() {
					private final NodeList childList = element.getElementsByTagNameNS(namespaceURI, localName);
					private int current = 0;

					@Override
					public boolean hasNext() {
						return current < childList.getLength();
					}

					@Override
					public Element next() {
						return (Element) childList.item(current++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Returns the first child element of this packet that matches the given
	 * name and namespace. If no matching element is found, <tt>null</tt> will
	 * be returned. This is a convenience method to avoid manipulating this
	 * underlying packet's Element instance directly.
	 * <p>
	 * 
	 * Child elements in extended namespaces are used to extend the features of
	 * XMPP. Examples include a "user is typing" indicator and invitations to
	 * group chat rooms. Although any valid XML can be included in a child
	 * element in an extended namespace, many common features have been
	 * standardized as <a href="http://xmpp.org/extensions/">XMPP Extension
	 * Protocols</a> (XEPs).
	 * 
	 * @param name
	 *            the element name.
	 * @return the first matching child element, or <tt>null</tt> if there is no
	 *         matching child element.
	 */
	protected final static Element getChildElement(final Element element, final String name) {
		return getChildElement(element, name, XMLConstants.NULL_NS_URI);
	}

	/**
	 * Returns the first child element of this packet that matches the given
	 * name and namespace. If no matching element is found, <tt>null</tt> will
	 * be returned. This is a convenience method to avoid manipulating this
	 * underlying packet's Element instance directly.
	 * <p>
	 * 
	 * Child elements in extended namespaces are used to extend the features of
	 * XMPP. Examples include a "user is typing" indicator and invitations to
	 * group chat rooms. Although any valid XML can be included in a child
	 * element in an extended namespace, many common features have been
	 * standardized as <a href="http://xmpp.org/extensions/">XMPP Extension
	 * Protocols</a> (XEPs).
	 * 
	 * @param name
	 *            the element name.
	 * @param namespace
	 *            the element namespace.
	 * @return the first matching child element, or <tt>null</tt> if there is no
	 *         matching child element.
	 */
	protected final static Element getChildElement(final Element element, final String localName, final String namespaceURI) {
		return (Element) element.getElementsByTagNameNS(namespaceURI, localName).item(0);
	}

	/**
	 * Adds a new child element to this packet with the given name and
	 * namespace. The newly created Element is returned. This is a convenience
	 * method to avoid manipulating this underlying packet's Element instance
	 * directly.
	 * <p>
	 * 
	 * Child elements in extended namespaces are used to extend the features of
	 * XMPP. Examples include a "user is typing" indicator and invitations to
	 * group chat rooms. Although any valid XML can be included in a child
	 * element in an extended namespace, many common features have been
	 * standardized as <a href="http://xmpp.org/extensions/">XMPP Extension
	 * Protocols</a> (XEPs).
	 * 
	 * @param name
	 *            the element name.
	 * @return the newly created child element.
	 */
	protected final static Element addChildElement(final Element element, final String tagName) {
		return addChildElement(element, tagName, XMLConstants.NULL_NS_URI);
	}

	/**
	 * Adds a new child element to this packet with the given name and
	 * namespace. The newly created Element is returned. This is a convenience
	 * method to avoid manipulating this underlying packet's Element instance
	 * directly.
	 * <p>
	 * 
	 * Child elements in extended namespaces are used to extend the features of
	 * XMPP. Examples include a "user is typing" indicator and invitations to
	 * group chat rooms. Although any valid XML can be included in a child
	 * element in an extended namespace, many common features have been
	 * standardized as <a href="http://xmpp.org/extensions/">XMPP Extension
	 * Protocols</a> (XEPs).
	 * 
	 * @param name
	 *            the element name.
	 * @param namespace
	 *            the element namespace.
	 * @return the newly created child element.
	 */
	protected final static Element addChildElement(final Element element, final String qualifiedName, final String namespaceURI) {
		final Element newElement = element.getOwnerDocument().createElementNS(namespaceURI, qualifiedName);
		element.appendChild(newElement);
		return newElement;
	}

	protected final static String getChildElementText(final Element element, final String name) {
		return getChildElementText(element, name, XMLConstants.NULL_NS_URI);
	}

	protected final static String getChildElementText(final Element element, final String qualifiedName, final String namespaceURI) {
		final Element childElement = getChildElement(element, qualifiedName, namespaceURI);

		return childElement != null ? childElement.getTextContent() : null;
	}

	protected final static void setChildElementText(final Element element, final String name, final String text) {
		setChildElementText(element, name, XMLConstants.NULL_NS_URI, text);
	}

	protected final static void setChildElementText(final Element element, final String qualifiedName, final String namespaceURI, final String text) {
		Element childElement = getChildElement(element, qualifiedName, namespaceURI);

		// If text is null, remove the element.
		if (text == null && childElement != null) {
			element.removeChild(childElement);
			return;
		}

		// Do nothing if the new text is null
		if (text == null)
			return;

		if (childElement == null) {
			childElement = addChildElement(element, qualifiedName);
		}
		childElement.setTextContent(text);
	}

	protected final static String getChildElementLang(final Element element, final String name) {
		return getChildElementLang(element, name, XMLConstants.NULL_NS_URI);
	}

	protected final static String getChildElementLang(final Element element, final String qualifiedName, final String namespaceURI) {
		final Element childElement = getChildElement(element, qualifiedName, namespaceURI);

		return getAttribute(childElement, "xml:lang", XMLConstants.XML_NS_URI);
	}

	protected final static void setChildElementLang(final Element element, final String name, final String language) {
		setChildElementLang(element, name, XMLConstants.NULL_NS_URI, language);
	}

	protected final static void setChildElementLang(final Element element, final String qualifiedName, final String namespaceURI, final String language) {
		final Element childElement = getChildElement(element, qualifiedName, namespaceURI);

		setAttribute(childElement, "xml:lang", XMLConstants.XML_NS_URI, language);
	}

	protected final static String getAttribute(final Element element, final String name) {
		return getAttribute(element, name, XMLConstants.NULL_NS_URI);
	}

	protected final static String getAttribute(final Element element, final String qualifiedName, final String namespaceURI) {
		final String value = element.getAttributeNS(namespaceURI, qualifiedName);

		return !value.isEmpty() ? value : null;
	}

	protected final static void setAttribute(final Element element, final String name, final String value) {
		setAttribute(element, name, XMLConstants.NULL_NS_URI, value);
	}

	protected final static void setAttribute(final Element element, final String qualifiedName, final String namespaceURI, final String value) {
		if (value != null) {
			element.setAttributeNS(namespaceURI, qualifiedName, value);
		} else {
			element.removeAttributeNS(namespaceURI, qualifiedName);
		}
	}

	/**
	 * Returns the DOM4J Element that backs the packet. The element is the
	 * definitive representation of the packet and can be manipulated directly
	 * to change packet contents.
	 * 
	 * @return the DOM4J Element that represents the packet.
	 */
	public final Element getElement() {
		return element;
	}

	@Override
	public final String toString() {
		try {
			final Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "no");

			final StringWriter buffer = new StringWriter();
			trans.transform(new DOMSource(element), new StreamResult(buffer));

			return buffer.toString();
		} catch (final TransformerException e) {
			throw new InternalError("Transformer error");
		}
	}

}
