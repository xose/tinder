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

/**
 * Presence packet. Presence packets are used to express an entity's current
 * network availability and to notify other entities of that availability.
 * Presence packets are also used to negotiate and manage subscriptions to the
 * presence of other entities.
 * <p>
 * 
 * A presence optionally has a {@link Type}.
 * 
 * @author Matt Tucker
 */
@NotThreadSafe
public class Presence extends Packet {

	/**
	 * Constructs a new Presence.
	 */
	public Presence() {
		super("presence");
	}

	/**
	 * Constructs a new Presence with the specified type.
	 * 
	 * @param type
	 *            the presence type.
	 */
	public Presence(final Presence.Type type) {
		this();
		setType(type);
	}

	public Presence(final Presence.Type type, final JID from, final JID to) {
		this(type);
		setFrom(from);
		setTo(to);
	}

	public Presence(final Element element) {
		super(element);
	}

	/**
	 * Returns the type of this presence. If the presence is "available", the
	 * type will be <tt>null</tt> (in XMPP, no value for the type attribute is
	 * defined as available).
	 * 
	 * @return the presence type or <tt>null</tt> if "available".
	 * @see Type
	 */
	public Type getType() {
		final String type = element.getAttribute("type");

		if (type == null)
			return Type.available;

		return Type.valueOf(type);
	}

	/**
	 * Sets the type of this presence.
	 * 
	 * @param type
	 *            the presence type.
	 * @see Type
	 */
	public void setType(final Type type) {
		if (type == null || type == Type.available) {
			element.removeAttribute("type");
		} else {
			element.setAttribute("type", type.toString());
		}
	}

	/**
	 * Returns the presence "show" value, which specifies a particular
	 * availability status. If the &lt;show&gt; element is not present, this
	 * method will return <tt>null</tt>. The show value can only be set if the
	 * presence type is "avaialble". A <tt>null</tt> show value is used to
	 * represent "available", which is the default.
	 * 
	 * @return the presence show value..
	 * @see Show
	 */
	public Show getShow() {
		final String show = getChildElementText(element, "show");

		return show != null ? Show.valueOf(show) : null;
	}

	/**
	 * Sets the presence "show" value, which specifies a particular availability
	 * status. The show value can only be set if the presence type is
	 * "available". A <tt>null</tt> show value is used to represent "available",
	 * which is the default.
	 * 
	 * @param show
	 *            the presence show value.
	 * @throws IllegalArgumentException
	 *             if the presence type is not available.
	 * @see Show
	 */
	public void setShow(final Show show) {
		if (getType() != Type.available)
			throw new IllegalArgumentException("Cannot set 'show' if 'type' attribute is set.");

		setChildElementText(element, "show", show.toString());
	}

	/**
	 * Returns the status of this presence packet, a natural-language
	 * description of availability status.
	 * 
	 * @return the status.
	 */
	public String getStatus() {
		return getChildElementText(element, "status");
	}

	/**
	 * Sets the status of this presence packet, a natural-language description
	 * of availability status.
	 * 
	 * @param status
	 *            the status.
	 */
	public void setStatus(final String status) {
		setChildElementText(element, "status", status);
	}

	/**
	 * Returns the priority. The valid priority range is -128 through 128. If no
	 * priority element exists in the packet, this method will return the
	 * default value of 0.
	 * 
	 * @return the priority.
	 */
	public int getPriority() {
		try {
			return Integer.parseInt(getChildElementText(element, "priority"));
		} catch (final Exception e) {
			return 0;
		}
	}

	/**
	 * Sets the priority. The valid priority range is -128 through 128.
	 * 
	 * @param priority
	 *            the priority.
	 * @throws IllegalArgumentException
	 *             if the priority is less than -128 or greater than 128.
	 */
	public void setPriority(final int priority) {
		if (priority < -128 || priority > 128)
			throw new IllegalArgumentException("Priority value of " + priority + " is outside the valid range of -128 through 128");

		setChildElementText(element, "priority", Integer.toString(priority));
	}

	@Override
	public Presence clone() {
		return new Presence(element);
	}

	/**
	 * Represents the type of a presence packet. Note: the presence is assumed
	 * to be "available" when the type attribute of the packet is <tt>null</tt>.
	 * The valid types are:
	 * 
	 * <ul>
	 * <li>{@link #unavailable Presence.Type.unavailable} -- signals that the
	 * entity is no longer available for communication.
	 * <li>{@link #subscribe Presence.Type.subscribe} -- the sender wishes to
	 * subscribe to the recipient's presence.
	 * <li>{@link #subscribed Presence.Type.subscribed} -- the sender has
	 * allowed the recipient to receive their presence.
	 * <li>{@link #unsubscribe Presence.Type.unsubscribe} -- the sender is
	 * unsubscribing from another entity's presence.
	 * <li>{@link #unsubscribed Presence.Type.unsubcribed} -- the subscription
	 * request has been denied or a previously-granted subscription has been
	 * cancelled.
	 * <li>{@link #probe Presence.Type.probe} -- a request for an entity's
	 * current presence; SHOULD be generated only by a server on behalf of a
	 * user.
	 * <li>{@link #error Presence.Type.error} -- an error has occurred regarding
	 * processing or delivery of a previously-sent presence stanza.
	 * </ul>
	 */
	public enum Type {

		/**
		 * The sender is available.
		 */
		available,

		/**
		 * Typically short text message used in line-by-line chat interfaces.
		 */
		unavailable,

		/**
		 * The sender wishes to subscribe to the recipient's presence.
		 */
		subscribe,

		/**
		 * The sender has allowed the recipient to receive their presence.
		 */
		subscribed,

		/**
		 * The sender is unsubscribing from another entity's presence.
		 */
		unsubscribe,

		/**
		 * The subscription request has been denied or a previously-granted
		 * subscription has been cancelled.
		 */
		unsubscribed,

		/**
		 * A request for an entity's current presence; SHOULD be generated only
		 * by a server on behalf of a user.
		 */
		probe,

		/**
		 * An error has occurred regarding processing or delivery of a
		 * previously-sent presence stanza.
		 */
		error;
	}

	/**
	 * Represents the presence "show" value. Note: a <tt>null</tt> "show" value
	 * is the default, which means "available". Valid values are:
	 * 
	 * <ul>
	 * <li>{@link #chat Presence.Show.chat} -- the entity or resource is
	 * actively interested in chatting.
	 * <li>{@link #away Presence.Show.away} -- the entity or resource is
	 * temporarily away.
	 * <li>{@link #dnd Presence.Show.dnd} -- the entity or resource is busy (dnd
	 * = "Do Not Disturb").
	 * <li>{@link #xa Presence.Show.xa} -- the entity or resource is away for an
	 * extended period (xa = "eXtended Away").
	 * </ul>
	 */
	public enum Show {

		/**
		 * The entity or resource is actively interested in chatting.
		 */
		chat,

		/**
		 * The entity or resource is temporarily away.
		 */
		away,

		/**
		 * The entity or resource is away for an extended period (xa =
		 * "eXtended Away").
		 */
		xa,

		/**
		 * The entity or resource is busy (dnd = "Do Not Disturb").
		 */
		dnd;
	}
}