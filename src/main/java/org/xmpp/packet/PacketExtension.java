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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import net.jcip.annotations.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xmpp.util.BaseXML;

/**
 * A packet extension represents a child element of a Packet for a given
 * qualified name. The PacketExtension acts as a wrapper on a child element the
 * same way Packet does for a whole element. The wrapper provides an easy way to
 * handle the packet extension.
 * <p>
 * 
 * Subclasses of this class can be registered using the static variable
 * <tt>registeredExtensions</tt>. The registration process associates the new
 * subclass with a given qualified name (ie. element name and namespace). This
 * information will be used by {@link Packet#getExtension(String, String)} for
 * locating the corresponding PacketExtension subclass to return for the
 * requested qualified name. Each PacketExtension must have a public constructor
 * that takes an Element instance as an argument.
 * 
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class PacketExtension extends BaseXML {

	private static final Logger log = LoggerFactory.getLogger(PacketExtension.class);

	/**
	 * Subclasses of PacketExtension should register the element name and
	 * namespace that the subclass is using.
	 */
	protected static final Map<QName, Class<? extends PacketExtension>> registeredExtensions = new ConcurrentHashMap<QName, Class<? extends PacketExtension>>();

	/**
	 * Returns the extension class to use for the specified element name and
	 * namespace. For instance, the DataForm class should be used for the
	 * element "x" and namespace "jabber:x:data".
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 * @return the extension class to use for the specified element name and
	 *         namespace.
	 */
	public static final Class<? extends PacketExtension> getExtensionClass(final String localName, final String namespaceURI) {
		return registeredExtensions.get(new QName(namespaceURI, localName));
	}

	/**
	 * Constructs a new Packet extension using the specified name and namespace.
	 * 
	 * @param name
	 *            the child element name.
	 * @param namespace
	 *            the child element namespace.
	 */
	protected PacketExtension(final String qualifiedName, final String namespaceURI) {
		super(qualifiedName, namespaceURI);
	}

	/**
	 * Constructs a new PacketExtension.
	 * 
	 * @param element
	 *            the XML Element that contains the packet extension contents.
	 */
	protected PacketExtension(final Element element) {
		super(element);
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
	public static final void addExtension(final Element element, final PacketExtension extension) {
		element.appendChild(extension.getElement());
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
	public static final PacketExtension getExtension(final Element element, final String name, final String namespaceURI) {
		final Element extension = getChildElement(element, name, namespaceURI);
		if (extension == null)
			return null;

		final Class<? extends PacketExtension> extensionClass = PacketExtension.getExtensionClass(name, namespaceURI);

		// If a specific PacketExtension implementation has been registered, use
		// that
		if (extensionClass != null) {
			try {
				final Constructor<? extends PacketExtension> constructor = extensionClass.getDeclaredConstructor(Element.class);
				return constructor.newInstance(extension);
			} catch (final Exception e) {
				log.warn("Packet extension (name " + name + ", namespace " + namespaceURI + ") cannot be found.", e);
				return null;
			}
		}

		return new PacketExtension(extension);
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
	public static final boolean deleteExtension(final Element element, final String name, final String namespaceURI) {
		final Element extension = getChildElement(element, name, namespaceURI);
		if (extension == null)
			return false;

		return element.removeChild(extension) != null;
	}

}
