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

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Element;

/**
 * Message packet.
 * <p>
 * 
 * A message can have one of several {@link Type Types}. For each message type,
 * different message fields are typically used as follows:
 * 
 * <p>
 * <table border="1">
 * <tr>
 * <td>&nbsp;</td>
 * <td colspan="5"><b>Message type</b></td>
 * </tr>
 * <tr>
 * <td><i>Field</i></td>
 * <td><b>Normal</b></td>
 * <td><b>Chat</b></td>
 * <td><b>Group Chat</b></td>
 * <td><b>Headline</b></td>
 * <td><b>Error</b></td>
 * </tr>
 * <tr>
 * <td><i>subject</i></td>
 * <td>SHOULD</td>
 * <td>SHOULD NOT</td>
 * <td>SHOULD NOT</td>
 * <td>SHOULD NOT</td>
 * <td>SHOULD NOT</td>
 * </tr>
 * <tr>
 * <td><i>thread</i></td>
 * <td>OPTIONAL</td>
 * <td>SHOULD</td>
 * <td>OPTIONAL</td>
 * <td>OPTIONAL</td>
 * <td>SHOULD NOT</td>
 * </tr>
 * <tr>
 * <td><i>body</i></td>
 * <td>SHOULD</td>
 * <td>SHOULD</td>
 * <td>SHOULD</td>
 * <td>SHOULD</td>
 * <td>SHOULD NOT</td>
 * </tr>
 * <tr>
 * <td><i>error</i></td>
 * <td>MUST NOT</td>
 * <td>MUST NOT</td>
 * <td>MUST NOT</td>
 * <td>MUST NOT</td>
 * <td>MUST</td>
 * </tr>
 * </table>
 */
@NotThreadSafe
public class Message extends Packet {

	/**
	 * Constructs a new Message.
	 */
	public Message() {
		super("message");
	}

	/**
	 * Constructs a new Message using an existing Element. This is useful for
	 * parsing incoming message Elements into Message objects.
	 * 
	 * @param element
	 *            the message Element.
	 */
	public Message(final Element element) {
		super(element);
	}

	/**
	 * Returns the type of this message
	 * 
	 * @return the message type.
	 * @see Type
	 */
	public Type getType() {
		final String type = getAttribute(element, "type");

		return type != null ? Type.valueOf(type) : Type.normal;
	}

	/**
	 * Sets the type of this message.
	 * 
	 * @param type
	 *            the message type.
	 * @see Type
	 */
	public void setType(final Type type) {
		setAttribute(element, "type", type != null ? type.toString() : null);
	}

	/**
	 * Returns the subject of this message or <tt>null</tt> if there is no
	 * subject..
	 * 
	 * @return the subject.
	 */
	public String getSubject() {
		return getChildElementText(element, "subject");
	}

	/**
	 * Sets the subject of this message.
	 * 
	 * @param subject
	 *            the subject.
	 */
	public void setSubject(final String subject) {
		setChildElementText(element, "subject", subject);
	}

	/**
	 * Returns the body of this message or <tt>null</tt> if there is no body.
	 * 
	 * @return the body.
	 */
	public String getBody() {
		return getChildElementText(element, "body");
	}

	/**
	 * Sets the body of this message.
	 * 
	 * @param body
	 *            the body.
	 */
	public void setBody(final String body) {
		setChildElementText(element, "body", body);
	}

	/**
	 * Returns the thread value of this message, an identifier that is used for
	 * tracking a conversation thread ("instant messaging session") between two
	 * entities. If the thread is not set, <tt>null</tt> will be returned.
	 * 
	 * @return the thread value.
	 */
	public String getThread() {
		return getChildElementText(element, "thread");
	}

	/**
	 * Sets the thread value of this message, an identifier that is used for
	 * tracking a conversation thread ("instant messaging session") between two
	 * entities.
	 * 
	 * @param thread
	 *            thread value.
	 */
	public void setThread(final String thread) {
		setChildElementText(element, "thread", thread);
	}

	/**
	 * Type-safe enumeration for the type of a message. The types are:
	 * 
	 * <ul>
	 * <li>{@link #normal Message.Type.normal} -- (Default) a normal text
	 * message used in email like interface.
	 * <li>{@link #chat Message.Type.cha}t -- a typically short text message
	 * used in line-by-line chat interfaces.
	 * <li>{@link #groupchat Message.Type.groupchat} -- a chat message sent to a
	 * groupchat server for group chats.
	 * <li>{@link #headline Message.Type.headline} -- a text message to be
	 * displayed in scrolling marquee displays.
	 * <li>{@link #error Message.Type.error} -- indicates a messaging error.
	 * </ul>
	 */
	public enum Type {

		/**
		 * (Default) a normal text message used in email like interface.
		 */
		normal,

		/**
		 * Typically short text message used in line-by-line chat interfaces.
		 */
		chat,

		/**
		 * Chat message sent to a groupchat server for group chats.
		 */
		groupchat,

		/**
		 * Text message to be displayed in scrolling marquee displays.
		 */
		headline,

		/**
		 * Indicates a messaging error.
		 */
		error;
	}
}