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

import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.Element;
import org.xmpp.util.BaseXML;

/**
 * An XMPP packet (also referred to as a stanza). Each packet is backed by a
 * DOM4J Element. A set of convenience methods allows easy manipulation of the
 * Element, or the Element can be accessed directly and manipulated.
 * <p>
 * 
 * There are three core packet types:
 * <ul>
 * <li>{@link Message} -- used to send data between users.
 * <li>{@link Presence} -- contains user presence information or is used to
 * manage presence subscriptions.
 * <li>{@link IQ} -- exchange information and perform queries using a
 * request/response protocol.
 * </ul>
 * 
 * @author Matt Tucker
 */
@NotThreadSafe
public class Packet extends BaseXML {

	protected Packet(final String elementName) {
		super(elementName);
	}

	protected Packet(final String qualifiedName, final String namespaceURI) {
		super(qualifiedName, namespaceURI);
	}

	protected Packet(final Element element) {
		super(element);
	}

	/**
	 * Returns the packet ID, or <tt>null</tt> if the packet does not have an
	 * ID. Packet ID's are optional, except for IQ packets.
	 * 
	 * @return the packet ID.
	 */
	public final String getID() {
		return getAttribute(element, "id");
	}

	/**
	 * Sets the packet ID. Packet ID's are optional, except for IQ packets.
	 * 
	 * @param ID
	 *            the packet ID.
	 */
	public final void setID(final String id) {
		setAttribute(element, "id", id);
	}

	/**
	 * Returns the XMPP address (JID) that the packet is from, or <tt>null</tt>
	 * if the "from" attribute is not set. The XMPP protocol often makes the
	 * "from" attribute optional, so it does not always need to be set.
	 * 
	 * @return the XMPP address that the packet is from, or <tt>null</tt> if not
	 *         set.
	 */
	public final JID getFrom() {
		final String from = getAttribute(element, "from");

		return from != null ? new JID(from) : null;
	}

	/**
	 * Sets the XMPP address (JID) that the packet comes from. The XMPP protocol
	 * often makes the "from" attribute optional, so it does not always need to
	 * be set.
	 * 
	 * @param from
	 *            the XMPP address (JID) that the packet comes from.
	 */
	public final void setFrom(final JID from) {
		setAttribute(element, "from", from != null ? from.toString() : null);
	}

	/**
	 * Returns the XMPP address (JID) that the packet is addressed to, or
	 * <tt>null</tt> if the "to" attribute is not set. The XMPP protocol often
	 * makes the "to" attribute optional, so it does not always need to be set.
	 * 
	 * @return the XMPP address (JID) that the packet is addressed to, or
	 *         <tt>null</tt> if not set.
	 */
	public final JID getTo() {
		final String to = getAttribute(element, "to");

		return to != null ? new JID(to) : null;
	}

	/**
	 * Sets the XMPP address (JID) that the packet is address to. The XMPP
	 * protocol often makes the "to" attribute optional, so it does not always
	 * need to be set.
	 * 
	 * @param to
	 *            the XMPP address (JID) that the packet is addressed to.
	 */
	public final void setTo(final JID to) {
		setAttribute(element, "to", to != null ? to.toString() : null);
	}

	/**
	 * Adds the element contained in the PacketExtension to the element of this
	 * packet. It is important that this is the first and last time the element
	 * contained in PacketExtension is added to another Packet. Otherwise, a
	 * runtime error will be thrown when trying to add the PacketExtension's
	 * element to the Packet's element. Future modifications to the
	 * PacketExtension will be reflected in this Packet.
	 * 
	 * @param extension
	 *            the PacketExtension whose element will be added to this
	 *            Packet's element.
	 */
	public void addExtension(final PacketExtension extension) {
		PacketExtension.addExtension(element, extension);
	}

	/**
	 * Returns a {@link PacketExtension} on the first element found in this
	 * packet for the specified <tt>name</tt> and <tt>namespace</tt> or
	 * <tt>null</tt> if none was found.
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return a PacketExtension on the first element found in this packet for
	 *         the specified name and namespace or null if none was found.
	 */
	public PacketExtension getExtension(final String name, final String namespaceURI) {
		return PacketExtension.getExtension(element, name, namespaceURI);
	}

	/**
	 * Deletes the first element whose element name and namespace matches the
	 * specified element name and namespace.
	 * <p>
	 * 
	 * Notice that this method may remove any child element that matches the
	 * specified element name and namespace even if that element was not added
	 * to the Packet using a {@link PacketExtension}.
	 * 
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return true if a child element was removed.
	 */
	public boolean deleteExtension(final String name, final String namespaceURI) {
		return PacketExtension.deleteExtension(element, name, namespaceURI);
	}

	/**
	 * Returns the packet error, or <tt>null</tt> if there is no packet error.
	 * 
	 * @return the packet error.
	 */
	public final PacketError getError() {
		final Element error = getChildElement(element, "error");

		return error != null ? new PacketError(error) : null;
	}

	/**
	 * Sets the packet error. Calling this method will automatically set the
	 * packet "type" attribute to "error".
	 * 
	 * @param error
	 *            the packet error.
	 */
	public final void setError(final PacketError error) {
		if (error == null)
			throw new NullPointerException("Error cannot be null");

		// Force the packet type to "error".
		setAttribute(element, "type", "error");
		// Remove an existing error packet.
		if (getError() != null) {
			element.removeChild(getError().getElement());
		}
		// Add the error element.
		element.appendChild(error.getElement());
	}

	/**
	 * Sets the packet error using the specified condition. Calling this method
	 * will automatically set the packet "type" attribute to "error". This is a
	 * convenience method equivalent to calling:
	 * 
	 * <tt>setError(new PacketError(condition));</tt>
	 * 
	 * @param condition
	 *            the error condition.
	 */
	public final void setError(final PacketError.Condition condition) {
		setError(new PacketError(condition));
	}

	@Override
	public Packet clone() {
		return new Packet(element);
	}

}