/**
 * Copyright (C) 2004-2009 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xmpp.packet;

import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * IQ (Info/Query) packet. IQ packets are used to get and set information on the
 * server, including authentication, roster operations, and creating accounts.
 * Each IQ packet has a specific type that indicates what type of action is
 * being taken: "get", "set", "result", or "error".
 * <p>
 * 
 * IQ packets can contain a single child element that exists in a extended XML
 * namespace.
 */
@NotThreadSafe
public class IQ extends Packet {

	// Sequence and random number generator used for creating unique ID's.
	private static final Random random = new Random();
	private static int sequence = 0;

	/**
	 * Constructs a new IQ with an automatically generated ID and a type of
	 * {@link Type#get IQ.Type.get}.
	 */
	public IQ() {
		this(Type.get, null);
	}

	/**
	 * Constructs a new IQ using the specified type. A packet ID will be
	 * automatically generated.
	 * 
	 * @param type
	 *            the IQ type.
	 */
	public IQ(final Type type) {
		this(type, null);
	}

	/**
	 * Constructs a new IQ using the specified type and ID.
	 * 
	 * @param ID
	 *            the packet ID of the IQ.
	 * @param type
	 *            the IQ type.
	 */
	public IQ(final Type type, final String ID) {
		super("iq");
		setType(type);
		setID(ID != null ? ID : String.valueOf(random.nextInt(1000) + "-" + sequence++));
	}

	/**
	 * Constructs a new IQ using an existing Element. This is useful for parsing
	 * incoming IQ Elements into IQ objects.
	 * 
	 * @param element
	 *            the IQ Element.
	 */
	public IQ(final Element element) {
		super(element);
	}

	/**
	 * Returns the type of this IQ.
	 * 
	 * @return the IQ type.
	 * @see Type
	 */
	public final Type getType() {
		final String type = getAttribute(element, "type");

		return type != null ? Type.valueOf(type) : null;
	}

	/**
	 * Sets the type of this IQ.
	 * 
	 * @param type
	 *            the IQ type.
	 * @see Type
	 */
	public final void setType(final Type type) {
		setAttribute(element, "type", type != null ? type.toString() : null);
	}

	/**
	 * Convenience routine to indicate if this is a request stanza. (get or set)
	 * 
	 * @return True or false if this is a request stanza
	 */
	public final boolean isRequest() {
		final Type type = getType();

		return type != null && (type.equals(Type.get) || type.equals(Type.set));
	}

	/**
	 * Convenience routine to indicate if this is a response stanza. (result or
	 * error)
	 * 
	 * @return True or false if this is a response stanza
	 */
	public final boolean isResponse() {
		final Type type = getType();

		return type != null && (type.equals(Type.result) || type.equals(Type.error));
	}

	/**
	 * Returns the child element of this IQ. IQ packets may have a single child
	 * element in an extended namespace. This is a convenience method to avoid
	 * manipulating the underlying packet's Element instance directly.
	 * <p>
	 * 
	 * An IQ child element in extended namespaces is used to extend the features
	 * of XMPP. Although any valid XML can be included in a child element in an
	 * extended namespace, many common features have been standardized as <a
	 * href="http://xmpp.org/extensions/">XMPP Extension Protocols</a> (XEPs).
	 * 
	 * @return the child element.
	 */
	public Element getIQChildElement() {
		final NodeList elements = element.getChildNodes();
		if (elements.getLength() == 0)
			return null;

		// Search for a child element that is in a different namespace.
		for (int i = 0; i < elements.getLength(); i++) {
			final Element element = (Element) elements.item(i);
			final String namespace = element.getNamespaceURI();
			if (namespace != null && !namespace.equals("jabber:client") && !namespace.equals("jabber:server"))
				return element;
		}
		return null;
	}

	/**
	 * Sets the child element of this IQ. IQ packets may have a single child
	 * element in an extended namespace. This is a convenience method to avoid
	 * manipulating this underlying packet's Element instance directly.
	 * <p>
	 * 
	 * A sample use of this method might look like the following:
	 * 
	 * <pre>
	 * IQ iq = new IQ(&quot;time_1&quot;);
	 * iq.setTo(&quot;mary@example.com&quot;);
	 * iq.setType(IQ.Type.GET);
	 * iq.setChildElement(docFactory.createElement(&quot;query&quot;, &quot;jabber:iq:time&quot;));
	 * </pre>
	 * <p>
	 * 
	 * An IQ child element in extended namespaces is used to extend the features
	 * of XMPP. Although any valid XML can be included in a child element in an
	 * extended namespace, many common features have been standardized as <a
	 * href="http://xmpp.org/extensions/">XMPP Extension Protocols</a> (XEPs).
	 * 
	 * @param childElement
	 *            the child element.
	 */
	public void setIQChildElement(final Element childElement) {
		final Element currentChild = getIQChildElement();
		if (currentChild == null) {
			element.appendChild(childElement);
		} else {
			element.replaceChild(childElement, currentChild);
		}
	}

	/**
	 * Sets the child element of this IQ by constructing a new Element with the
	 * given name and namespace. The newly created child element is returned. IQ
	 * packets may have a single child element in an extended namespace. This
	 * method is a convenience method to avoid manipulating the underlying
	 * packet's Element instance directly.
	 * <p>
	 * 
	 * In some cases, additional custom sub-elements must be added to an IQ
	 * child element (called packet extensions). For example, when adding a data
	 * form to an IQ response. See {@link #addExtension(PacketExtension)}.
	 * <p>
	 * 
	 * A sample use of this method might look like the following:
	 * 
	 * <pre>
	 * IQ iq = new IQ(&quot;time_1&quot;);
	 * iq.setTo(&quot;mary@example.com&quot;);
	 * iq.setType(IQ.Type.GET);
	 * iq.setChildElement(&quot;query&quot;, &quot;jabber:iq:time&quot;);
	 * </pre>
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return the newly created child element.
	 */
	public Element setIQChildElement(final String qualifiedName, final String namespaceURI) {
		final Element childElement = element.getOwnerDocument().createElementNS(namespaceURI, qualifiedName);
		setIQChildElement(childElement);
		return childElement;

	}

	/**
	 * Adds the element contained in the PacketExtension to the child element of
	 * the IQ packet. IQ packets, unlike the other packet types, have a unique
	 * child element that holds the packet extensions. If an extension is added
	 * to an IQ packet that does not have a child element then an
	 * IllegalStateException will be thrown.
	 * <p>
	 * 
	 * It is important that this is the first and last time the element
	 * contained in PacketExtension is added to another Packet. Otherwise, a
	 * runtime error will be thrown when trying to add the PacketExtension's
	 * element to the Packet's element. Future modifications to the
	 * PacketExtension will be reflected in this Packet.
	 * <p>
	 * 
	 * Note: packet extensions on IQ packets are only for use in specialized
	 * situations. In most cases, you should only need to set the child element
	 * of the IQ.
	 * 
	 * @param extension
	 *            the PacketExtension whose element will be added to this
	 *            Packet's element.
	 */
	@Override
	public void addExtension(final PacketExtension extension) {
		final Element childElement = getIQChildElement();
		if (childElement == null)
			throw new IllegalStateException("Cannot add packet extension when child element is null");

		// Add the extension to the child element
		PacketExtension.addExtension(childElement, extension);
	}

	/**
	 * Returns a {@link PacketExtension} on the first element found in this
	 * packet's child element for the specified <tt>name</tt> and
	 * <tt>namespace</tt> or <tt>null</tt> if none was found. If the IQ packet
	 * does not have a child element then <tt>null</tt> will be returned.
	 * <p>
	 * 
	 * Note: packet extensions on IQ packets are only for use in specialized
	 * situations. In most cases, you should only need to set the child element
	 * of the IQ.
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return a PacketExtension on the first element found in this packet for
	 *         the specified name and namespace or <tt>null</tt> if none was
	 *         found.
	 */
	@Override
	public PacketExtension getExtension(final String name, final String namespaceURI) {
		final Element childElement = getIQChildElement();
		if (childElement == null)
			return null;

		// Search for extensions in the child element
		return PacketExtension.getExtension(childElement, name, namespaceURI);
	}

	/**
	 * Deletes the first element whose element name and namespace matches the
	 * specified element name and namespace in this packet's child element. If
	 * the IQ packet does not have a child element then this method does nothing
	 * and returns <tt>false</tt>.
	 * <p>
	 * 
	 * Notice that this method may remove any child element that matches the
	 * specified element name and namespace even if that element was not added
	 * to the Packet using a {@link PacketExtension}.
	 * <p>
	 * 
	 * Note: packet extensions on IQ packets are only for use in specialized
	 * situations. In most cases, you should only need to set the child element
	 * of the IQ.
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return true if a child element was removed.
	 */
	@Override
	public boolean deleteExtension(final String name, final String namespaceURI) {
		final Element childElement = getIQChildElement();
		if (childElement == null)
			return false;

		// Delete extensions in the child element
		return PacketExtension.deleteExtension(childElement, name, namespaceURI);
	}

	/**
	 * Convenience method to create a new {@link Type#result IQ.Type.result} IQ
	 * based on a {@link Type#get IQ.Type.get} or {@link Type#set IQ.Type.set}
	 * IQ. The new packet will be initialized with:
	 * <ul>
	 * 
	 * <li>The sender set to the recipient of the originating IQ.
	 * <li>The recipient set to the sender of the originating IQ.
	 * <li>The type set to {@link Type#result IQ.Type.result}.
	 * <li>The id set to the id of the originating IQ.
	 * </ul>
	 * 
	 * @param iq
	 *            the {@link Type#get IQ.Type.get} or {@link Type#set
	 *            IQ.Type.set} IQ packet.
	 * @throws IllegalArgumentException
	 *             if the IQ packet does not have a type of {@link Type#get
	 *             IQ.Type.get} or {@link Type#set IQ.Type.set}.
	 * @return a new {@link Type#result IQ.Type.result} IQ based on the
	 *         originating IQ.
	 */
	public static IQ createResultIQ(final IQ iq) {
		if (!iq.isRequest())
			throw new IllegalArgumentException("IQ must be of type 'set' or 'get'. Original IQ: " + iq.toString());

		final IQ result = new IQ(Type.result, iq.getID());
		result.setFrom(iq.getTo());
		result.setTo(iq.getFrom());
		return result;
	}

	/**
	 * Type-safe enumeration to represent the type of the IQ packet. The types
	 * are:
	 * 
	 * <ul>
	 * <li>IQ.Type.get -- the IQ is a request for information or requirements.
	 * <li>IQ.Type.set -- the IQ provides required data, sets new values, or
	 * replaces existing values.
	 * <li>IQ.Type.result -- the IQ is a response to a successful get or set
	 * request.
	 * <li>IQ.Type.error -- an error has occurred regarding processing or
	 * delivery of a previously-sent get or set.
	 * </ul>
	 * 
	 * If {@link #get IQ.Type.get} or {@link #set IQ.Type.set} is received the
	 * response must be {@link #result IQ.Type.result} or {@link #error
	 * IQ.Type.error}. The id of the originating {@link #get IQ.Type.get} of
	 * {@link #set IQ.Type.set} IQ must be preserved when sending
	 * {@link #result IQ.Type.result} or {@link #error IQ.Type.error}.
	 */
	public enum Type {

		/**
		 * The IQ is a request for information or requirements.
		 */
		get,

		/**
		 * The IQ provides required data, sets new values, or replaces existing
		 * values.
		 */
		set,

		/**
		 * The IQ is a response to a successful get or set request.
		 */
		result,

		/**
		 * An error has occurred regarding processing or delivery of a
		 * previously-sent get or set.
		 */
		error;

	}
}