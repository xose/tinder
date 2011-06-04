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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.Element;

/**
 * Roster packet. The roster is a list of JIDs (typically other users) that the
 * user wishes to track the presence of. Each roster item is keyed by JID and
 * contains a nickname (optional), subscription type, and list of groups
 * (optional).
 * 
 * @author Matt Tucker
 */
@NotThreadSafe
public class Roster extends IQ {

	/**
	 * Constructs a new Roster with an automatically generated ID and a type of
	 * {@link IQ.Type#get}.
	 */
	public Roster() {
		super();
		setIQChildElement("query", "jabber:iq:roster");
	}

	/**
	 * Constructs a new Roster using the specified type. A packet ID will be
	 * automatically generated.
	 * 
	 * @param type
	 *            the IQ type.
	 */
	public Roster(final Type type) {
		super(type);
		setIQChildElement("query", "jabber:iq:roster");
	}

	/**
	 * Constructs a new Roster using the specified type and ID.
	 * 
	 * @param type
	 *            the IQ type.
	 * @param ID
	 *            the packet ID of the IQ.
	 */
	public Roster(final Type type, final String ID) {
		super(type, ID);
		setIQChildElement("query", "jabber:iq:roster");
	}

	/**
	 * Constructs a new Roster using an existing Element. This is useful for
	 * parsing incoming roster Elements into Roster objects.
	 * 
	 * @param element
	 *            the Roster Element.
	 */
	public Roster(final Element element) {
		super(element);
	}

	/**
	 * Adds a new item to the roster. The name and groups are set to
	 * <tt>null</tt> If the roster packet already contains an item using the
	 * same JID, the information in the existing item will be overwritten with
	 * the new information.
	 * <p>
	 * 
	 * The XMPP specification recommends that if the roster item is associated
	 * with another instant messaging user (human), that the JID be in bare form
	 * (e.g. user@domain). Use the {@link JID#toBareJID() toBareJID()} method
	 * for a bare JID.
	 * 
	 * @param jid
	 *            the JID.
	 * @param subscription
	 *            the subscription type.
	 * @return the newly created item.
	 */
	public Item addItem(final JID jid, final Subscription subscription) {
		if (getType() != IQ.Type.result && getType() != IQ.Type.set)
			throw new IllegalStateException("IQ type must be 'result' or 'set'");
		if (jid == null)
			throw new NullPointerException("JID cannot be null");
		return addItem(jid, null, null, subscription, null);
	}

	/**
	 * Adds a new item to the roster. If the roster packet already contains an
	 * item using the same JID, the information in the existing item will be
	 * overwritten with the new information.
	 * <p>
	 * 
	 * The XMPP specification recommends that if the roster item is associated
	 * with another instant messaging user (human), that the JID be in bare form
	 * (e.g. user@domain). Use the {@link JID#toBareJID() toBareJID()} method
	 * for a bare JID.
	 * 
	 * @param jid
	 *            the JID.
	 * @param name
	 *            the nickname.
	 * @param ask
	 *            the ask type.
	 * @param subscription
	 *            the subscription type.
	 * @param groups
	 *            a Collection of groups.
	 * @return the newly created item.
	 */
	public Item addItem(final JID jid, final String name, final Ask ask, final Subscription subscription, final Collection<String> groups) {
		if (jid == null)
			throw new NullPointerException("JID cannot be null");
		if (subscription == null)
			throw new NullPointerException("Subscription cannot be null");

		Element query = getIQChildElement();
		if (query == null) {
			query = setIQChildElement("query", "jabber:iq:roster");
		}

		Element item = null;
		for (final Element el : getChildElements(query, "item")) {
			if (el.getAttribute("jid").equals(jid.toString())) {
				item = el;
				break;
			}
		}
		if (item == null) {
			item = addChildElement(query, "item");
		}

		item.setAttribute("jid", jid.toBareJID());
		item.setAttribute("name", name);
		if (ask != null) {
			item.setAttribute("ask", ask.toString());
		}
		item.setAttribute("subscription", subscription.toString());
		// Erase existing groups in case the item previously existed.
		for (final Element group : getChildElements(item, "group")) {
			item.removeChild(group);
		}
		// Add in groups.
		if (groups != null) {
			for (final String group : groups) {
				setChildElementText(item, "group", group);
			}
		}
		return new Item(jid, name, ask, subscription, groups);
	}

	/**
	 * Removes an item from this roster.
	 * 
	 * @param jid
	 *            the JID of the item to remove.
	 */
	public void removeItem(final JID jid) {
		final Element query = getIQChildElement();
		if (query != null) {
			for (final Element item : getChildElements(query, "item")) {
				if (item.getAttribute("jid").equals(jid.toString())) {
					query.removeChild(item);
					return;
				}
			}
		}
	}

	/**
	 * Returns an unmodifiable copy of the {@link Item Items} in the roster
	 * packet.
	 * 
	 * @return an unmodifable copy of the {@link Item Items} in the roster
	 *         packet.
	 */
	public Collection<Item> getItems() {
		final Collection<Item> items = new ArrayList<Item>();

		final Element query = getIQChildElement();
		if (query != null) {
			for (final Element item : getChildElements(query, "item")) {
				final String jid = item.getAttribute("jid");
				final String name = item.getAttribute("name");
				final String ask = item.getAttribute("ask");
				final String subscription = item.getAttribute("subscription");
				final Collection<String> groups = new ArrayList<String>();

				for (final Element group : getChildElements(item, "group")) {
					groups.add(group.getTextContent().trim());
				}

				final Ask askStatus = ask != null ? Ask.valueOf(ask) : null;
				final Subscription subStatus = subscription == null ? null : Subscription.valueOf(subscription);
				items.add(new Item(new JID(jid), name, askStatus, subStatus, groups));
			}
		}

		return Collections.unmodifiableCollection(items);
	}

	/**
	 * Item in a roster, which represents an individual contact. Each contact
	 * has a JID, an optional nickname, a subscription type, and can belong to
	 * one ore more groups.
	 */
	public static class Item {

		private final JID jid;
		private final String name;
		private final Ask ask;
		private final Subscription subscription;
		private final Collection<String> groups;

		/**
		 * Constructs a new roster item.
		 * 
		 * @param jid
		 *            the JID.
		 * @param name
		 *            the nickname.
		 * @param ask
		 *            the ask state.
		 * @param subscription
		 *            the subscription state.
		 * @param groups
		 *            the item groups.
		 */
		private Item(final JID jid, final String name, final Ask ask, final Subscription subscription, final Collection<String> groups) {
			this.jid = jid;
			this.name = name;
			this.ask = ask;
			this.subscription = subscription;
			this.groups = groups;
		}

		/**
		 * Returns the JID associated with this item. The JID is the "key" in
		 * the list of items that make up a roster. There can only be a single
		 * item per JID in a roster.
		 * 
		 * @return the JID associated with this item.
		 */
		public final JID getJID() {
			return jid;
		}

		/**
		 * Returns the nickname associated with this item. If no nickname
		 * exists, <tt>null</tt> is returned.
		 * 
		 * @return the nickname, or <tt>null</tt> if it doesn't exist.
		 */
		public final String getName() {
			return name;
		}

		/**
		 * Returns the ask state of this item.
		 * 
		 * @return the ask state of this item.
		 */
		public final Ask getAsk() {
			return ask;
		}

		/**
		 * Returns the subscription state of this item.
		 * 
		 * @return the subscription state of this item.
		 */
		public final Subscription getSubscription() {
			return subscription;
		}

		/**
		 * Returns a Collection of the groups defined in this item. If no groups
		 * are defined, an empty Collection is returned.
		 * 
		 * @return the groups in this item.
		 */
		public final Collection<String> getGroups() {
			if (groups == null)
				return Collections.emptyList();
			return groups;
		}

		@Override
		public final String toString() {
			final StringBuffer buf = new StringBuffer();
			buf.append("<item ");
			buf.append("jid=\"").append(jid).append("\"");
			if (name != null) {
				buf.append(" name=\"").append(name).append("\"");
			}
			buf.append(" subscrption=\"").append(subscription).append("\"");
			if (groups == null || groups.isEmpty()) {
				buf.append("/>");
			} else {
				buf.append(">\n");
				for (final String group : groups) {
					buf.append("  <group>").append(group).append("</group>\n");
				}
				buf.append("</item>");
			}
			return buf.toString();
		}
	}

	@Override
	public Roster clone() {
		return new Roster(element);
	}

	/**
	 * Type-safe enumeration for the roster subscription type. Valid subcription
	 * types:
	 * 
	 * <ul>
	 * <li>{@link #none Roster.Subscription.none} -- the user does not have a
	 * subscription to the contact's presence information, and the contact does
	 * not have a subscription to the user's presence information.
	 * <li>{@link #to Roster.Subscription.to} -- the user has a subscription to
	 * the contact's presence information, but the contact does not have a
	 * subscription to the user's presence information.
	 * <li>{@link #from Roster.Subscription.from} -- the contact has a
	 * subscription to the user's presence information, but the user does not
	 * have a subscription to the contact's presence information.
	 * <li>{@link #both Roster.Subscription.both} -- both the user and the
	 * contact have subscriptions to each other's presence information.
	 * <li>{@link #remove Roster.Subscription.remove} -- the user is removing a
	 * contact from his or her roster.
	 * </ul>
	 */
	public enum Subscription {

		/**
		 * The user does not have a subscription to the contact's presence
		 * information, and the contact does not have a subscription to the
		 * user's presence information.
		 */
		none,

		/**
		 * The user has a subscription to the contact's presence information,
		 * but the contact does not have a subscription to the user's presence
		 * information.
		 */
		to,

		/**
		 * The contact has a subscription to the user's presence information,
		 * but the user does not have a subscription to the contact's presence
		 * information.
		 */
		from,

		/**
		 * Both the user and the contact have subscriptions to each other's
		 * presence information.
		 */
		both,

		/**
		 * The user is removing a contact from his or her roster. The user's
		 * server will 1) automatically cancel any existing presence
		 * subscription between the user and the contact, 2) remove the roster
		 * item from the user's roster and inform all of the user's available
		 * resources that have requested the roster of the roster item removal,
		 * 3) inform the resource that initiated the removal of success and 4)
		 * send unavailable presence from all of the user's available resources
		 * to the contact.
		 */
		remove;
	}

	/**
	 * Type-safe enumeration for the roster ask type. Valid ask types:
	 * 
	 * <ul>
	 * <li>{@link #subscribe Roster.Ask.subscribe} -- the roster item has been
	 * asked for permission to subscribe to their presence but no response has
	 * been received.
	 * <li>{@link #unsubscribe Roster.Ask.unsubscribe} -- the roster owner has
	 * asked to the roster item to unsubscribe from it's presence but has not
	 * received confirmation.
	 * </ul>
	 */
	public enum Ask {

		/**
		 * The roster item has been asked for permission to subscribe to their
		 * presence but no response has been received.
		 */
		subscribe,

		/**
		 * The roster owner has asked to the roster item to unsubscribe from
		 * it's presence but has not received confirmation.
		 */
		unsubscribe;
	}
}