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
 * A packet error. Errors must have a type and condition. Optionally, they can
 * include explanation text.
 * 
 * @author Matt Tucker
 */
@NotThreadSafe
public class PacketError extends BaseXML {

	private static final String ERROR_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-stanzas";

	/**
	 * Construcs a new PacketError with the specified condition. The error type
	 * will be set to the default for the specified condition.
	 * 
	 * @param condition
	 *            the error condition.
	 */
	public PacketError(final Condition condition) {
		this(condition, condition.getDefaultType(), null, null);
	}

	/**
	 * Constructs a new PacketError with the specified condition and type.
	 * 
	 * @param condition
	 *            the error condition.
	 * @param type
	 *            the error type.
	 */
	public PacketError(final Condition condition, final Type type) {
		this(condition, type, null, null);
	}

	/**
	 * Constructs a new PacketError.
	 * 
	 * @param type
	 *            the error type.
	 * @param condition
	 *            the error condition.
	 * @param text
	 *            the text description of the error.
	 */
	public PacketError(final Condition condition, final Type type, final String text) {
		this(condition, type, text, null);
	}

	/**
	 * Constructs a new PacketError.
	 * 
	 * @param type
	 *            the error type.
	 * @param condition
	 *            the error condition.
	 * @param text
	 *            the text description of the error.
	 * @param lang
	 *            the language code of the error description (e.g. "en").
	 */
	public PacketError(final Condition condition, final Type type, final String text, final String lang) {
		super("error");
		setCondition(condition);
		setType(type);
		setText(text, lang);
	}

	/**
	 * Constructs a new PacketError using an existing Element. This is useful
	 * for parsing incoming error Elements into PacketError objects.
	 * 
	 * @param element
	 *            the error Element.
	 */
	public PacketError(final Element element) {
		super(element);
	}

	/**
	 * Returns the error type.
	 * 
	 * @return the error type.
	 * @see Type
	 */
	public final Type getType() {
		final String type = getAttribute(element, "type");

		return type != null ? Type.fromXMPP(type) : null;
	}

	/**
	 * Sets the error type.
	 * 
	 * @param type
	 *            the error type.
	 * @see Type
	 */
	public final void setType(final Type type) {
		setAttribute(element, "type", type.toString());
	}

	/**
	 * Returns the error condition.
	 * 
	 * @return the error condition.
	 * @see Condition
	 */
	public Condition getCondition() {
		for (final Element el : getChildElements(element)) {
			if (ERROR_NAMESPACE.equals(el.getNamespaceURI()) && !el.getTagName().equals("text"))
				return Condition.fromXMPP(el.getTagName());
		}

		// Looking for XMPP condition failed. See if a legacy error code exists,
		// which can be mapped into an XMPP error condition.
		try {
			final String code = getAttribute(element, "code");
			return Condition.fromLegacyCode(Integer.parseInt(code));
		} catch (final Exception e) {
			// Ignore -- unable to map legacy code into a valid condition so
			// return null.
		}

		return null;
	}

	/**
	 * Sets the error condition.
	 * 
	 * @param condition
	 *            the error condition.
	 * @see Condition
	 */
	public void setCondition(final Condition condition) {
		if (condition == null)
			throw new NullPointerException("Condition cannot be null");

		// Set the error code for legacy support.
		element.setAttribute("code", Integer.toString(condition.getLegacyCode()));

		// Delete current condition.
		for (final Element el : getChildElements(element)) {
			if (ERROR_NAMESPACE.equals(el.getNamespaceURI()) && !el.getTagName().equals("text")) {
				element.removeChild(element);
			}
		}

		addChildElement(element, condition.toString(), ERROR_NAMESPACE);
	}

	/**
	 * Returns a text description of the error, or <tt>null</tt> if there is no
	 * text description.
	 * 
	 * @return the text description of the error.
	 */
	public String getText() {
		return getChildElementText(element, "text");
	}

	/**
	 * Sets the text description of the error.
	 * 
	 * @param text
	 *            the text description of the error.
	 */
	public void setText(final String text) {
		setChildElementText(element, "text", text);
	}

	/**
	 * Sets the text description of the error. Optionally, a language code can
	 * be specified to indicate the language of the description.
	 * 
	 * @param text
	 *            the text description of the error.
	 * @param lang
	 *            the language code of the description, or <tt>null</tt> to
	 *            specify no language code.
	 */
	public void setText(final String text, final String lang) {
		setChildElementText(element, "text", text);
		setChildElementLang(element, "text", lang);
	}

	/**
	 * Returns the text description's language code, or <tt>null</tt> if there
	 * is no language code associated with the description text.
	 * 
	 * @return the language code of the text description, if it exists.
	 */
	public String getTextLang() {
		return getChildElementLang(element, "text");
	}

	/**
	 * Sets an application-specific error condition.
	 * 
	 * @param name
	 *            the name of the application-specific error condition.
	 */
	public void setApplicationCondition(final String name) {
		setApplicationCondition(name, null);
	}

	/**
	 * Sets an application-specific error condition. Optionally, a
	 * application-specific namespace can be specified to define its own
	 * application-specific error .
	 * 
	 * @param name
	 *            the name of the application-specific error condition.
	 * @param namespaceURI
	 *            the namespace of the application.
	 */
	public void setApplicationCondition(final String name, final String namespaceURI) {
		if (ERROR_NAMESPACE.equals(namespaceURI))
			throw new IllegalArgumentException();

		// Delete current condition.
		for (final Element el : getChildElements(element)) {
			if (!ERROR_NAMESPACE.equals(el.getNamespaceURI())) {
				element.removeChild(element);
			}
		}

		// If name is null, clear the application condition.
		if (name == null)
			return;

		addChildElement(element, name, namespaceURI != null ? namespaceURI : "urn:xmpp:errors");
	}

	/**
	 * Returns the name of the application-specific error condition, or
	 * <tt>null</tt> if there is no application-specific error.
	 * 
	 * @return the name of the application-specific error condition, if it
	 *         exists.
	 */
	public String getApplicationConditionName() {
		for (final Element el : getChildElements(element)) {
			if (!ERROR_NAMESPACE.equals(el.getNamespaceURI()))
				return el.getTagName();
		}

		return null;
	}

	/**
	 * Returns the namespace of the application-specific error condition, or
	 * <tt>null</tt> if there is no application-specific error.
	 * 
	 * @return the namespace of the application-specific error condition, if it
	 *         exists.
	 */
	public String getApplicationConditionNamespaceURI() {
		for (final Element el : getChildElements(element)) {
			if (!ERROR_NAMESPACE.equals(el.getNamespaceURI()))
				return el.getNamespaceURI();
		}
		return null;
	}

	/**
	 * Type-safe enumeration for the error condition.
	 * <p>
	 * 
	 * Implementation note: XMPP error conditions use "-" characters in their
	 * names such as "bad-request". Because "-" characters are not valid
	 * identifier parts in Java, they have been converted to "_" characters in
	 * the enumeration names, such as <tt>bad_request</tt>. The
	 * {@link #toXMPP()} and {@link #fromXMPP(String)} methods can be used to
	 * convert between the enumertation values and XMPP error code strings.
	 */
	public enum Condition {

		/**
		 * The sender has sent XML that is malformed or that cannot be processed
		 * (e.g., an IQ stanza that includes an unrecognized value of the 'type'
		 * attribute); the associated error type SHOULD be "modify".
		 */
		bad_request(Type.modify, 400),

		/**
		 * Access cannot be granted because an existing resource or session
		 * exists with the same name or address; the associated error type
		 * SHOULD be "cancel".
		 */
		conflict(Type.cancel, 409),

		/**
		 * The feature requested is not implemented by the recipient or server
		 * and therefore cannot be processed; the associated error type SHOULD
		 * be "cancel".
		 */
		feature_not_implemented(Type.cancel, 501),

		/**
		 * The requesting entity does not possess the required permissions to
		 * perform the action; the associated error type SHOULD be "auth".
		 */
		forbidden(Type.auth, 403),

		/**
		 * The recipient or server can no longer be contacted at this address
		 * (the error stanza MAY contain a new address in the XML character data
		 * of the <gone/> element); the associated error type SHOULD be
		 * "modify".
		 */
		gone(Type.modify, 302),

		/**
		 * The server could not process the stanza because of a misconfiguration
		 * or an otherwise-undefined internal server error; the associated error
		 * type SHOULD be "wait".
		 */
		internal_server_error(Type.wait, 500),

		/**
		 * The addressed JID or item requested cannot be found; the associated
		 * error type SHOULD be "cancel".
		 */
		item_not_found(Type.cancel, 404),

		/**
		 * The sending entity has provided or communicated an XMPP address
		 * (e.g., a value of the 'to' attribute) or aspect thereof (e.g., a
		 * resource identifier) that does not adhere to the syntax defined in
		 * Addressing Scheme (Section 3); the associated error type SHOULD be
		 * "modify".
		 */
		jid_malformed(Type.modify, 400),

		/**
		 * The recipient or server understands the request but is refusing to
		 * process it because it does not meet criteria defined by the recipient
		 * or server (e.g., a local policy regarding acceptable words in
		 * messages); the associated error type SHOULD be "modify".
		 */
		not_acceptable(Type.modify, 406),

		/**
		 * The recipient or server does not allow any entity to perform the
		 * action; the associated error type SHOULD be "cancel".
		 */
		not_allowed(Type.cancel, 405),

		/**
		 * The sender must provide proper credentials before being allowed to
		 * perform the action, or has provided improper credentials; the
		 * associated error type SHOULD be "auth".
		 */
		not_authorized(Type.auth, 401),

		/**
		 * The requesting entity is not authorized to access the requested
		 * service because payment is required; the associated error type SHOULD
		 * be "auth".
		 */
		payment_required(Type.auth, 402),

		/**
		 * The intended recipient is temporarily unavailable; the associated
		 * error type SHOULD be "wait" (note: an application MUST NOT return
		 * this error if doing so would provide information about the intended
		 * recipient's network availability to an entity that is not authorized
		 * to know such information).
		 */
		recipient_unavailable(Type.wait, 404),

		/**
		 * The recipient or server is redirecting requests for this information
		 * to another entity, usually temporarily (the error stanza SHOULD
		 * contain the alternate address, which MUST be a valid JID, in the XML
		 * character data of the &lt;redirect/&gt; element); the associated
		 * error type SHOULD be "modify".
		 */
		redirect(Type.modify, 302),

		/**
		 * The requesting entity is not authorized to access the requested
		 * service because registration is required; the associated error type
		 * SHOULD be "auth".
		 */
		registration_required(Type.auth, 407),

		/**
		 * A remote server or service specified as part or all of the JID of the
		 * intended recipient does not exist; the associated error type SHOULD
		 * be "cancel".
		 */
		remote_server_not_found(Type.cancel, 404),

		/**
		 * A remote server or service specified as part or all of the JID of the
		 * intended recipient (or required to fulfill a request) could not be
		 * contacted within a reasonable amount of time; the associated error
		 * type SHOULD be "wait".
		 */
		remote_server_timeout(Type.wait, 504),

		/**
		 * The server or recipient lacks the system resources necessary to
		 * service the request; the associated error type SHOULD be "wait".
		 */
		resource_constraint(Type.wait, 500),

		/**
		 * The server or recipient does not currently provide the requested
		 * service; the associated error type SHOULD be "cancel".
		 */
		service_unavailable(Type.cancel, 503),

		/**
		 * The requesting entity is not authorized to access the requested
		 * service because a subscription is required; the associated error type
		 * SHOULD be "auth".
		 */
		subscription_required(Type.auth, 407),

		/**
		 * The error condition is not one of those defined by the other
		 * conditions in this list; any error type may be associated with this
		 * condition, and it SHOULD be used only in conjunction with an
		 * application-specific condition.
		 * <p>
		 * 
		 * Implementation note: the default type for this condition is
		 * {@link Type#wait}, which is not specified in the XMPP protocol.
		 */
		undefined_condition(Type.wait, 500),

		/**
		 * The recipient or server understood the request but was not expecting
		 * it at this time (e.g., the request was out of order); the associated
		 * error type SHOULD be "wait".
		 */
		unexpected_request(Type.wait, 400);

		/**
		 * Converts a String value into its Condition representation.
		 * 
		 * @param condition
		 *            the String value.
		 * @return the condition corresponding to the String.
		 */
		public final static Condition fromXMPP(String condition) {
			return valueOf(condition.replace("-", "_"));
		}

		public final static Condition fromLegacyCode(final int code) {
			for (Condition condition : values()) {
				if (condition.getLegacyCode() == code)
					return condition;
			}
			
			throw new IllegalArgumentException("Invalid code: " + code);
		}

		private final Type defaultType;
		private final int legacyCode;

		private Condition(final Type defaultType, final int legacyCode) {
			this.defaultType = defaultType;
			this.legacyCode = legacyCode;
		}

		/**
		 * Returns the default {@link Type} associated with this condition. Each
		 * error condition has an error type that it is usually associated with.
		 * 
		 * @return the default error type.
		 */
		public final Type getDefaultType() {
			return defaultType;
		}

		/**
		 * Returns the legacy error code associated with the error. Error code
		 * mappings are based on XEP-0086 'Error Condition Mappings'. Support
		 * for legacy error codes is necessary since many "Jabber" clients do
		 * not understand XMPP error codes. The {@link #fromLegacyCode(int)}
		 * method will convert numeric error codes into Conditions.
		 * 
		 * @return the legacy error code.
		 * @see <a href="http://xmpp.org/extensions/xep-0086.html">XEP-0086:
		 *      Error Condition Mappings</a>
		 */
		public final int getLegacyCode() {
			return legacyCode;
		}

		/**
		 * Returns the error code as a valid XMPP error code string.
		 * 
		 * @return the XMPP error code value.
		 */
		@Override
		public final String toString() {
			return super.toString().replace("_", "-");
		}
	}

	/**
	 * Error type. Valid types are:
	 * <ul>
	 * 
	 * <li>{@link #cancel Error.Type.cancel} -- do not retry (the error is
	 * unrecoverable).
	 * <li>{@link #continue_processing Error.Type.continue_processing} --
	 * proceed (the condition was only a warning). Equivalent to the XMPP error
	 * type "continue".
	 * <li>{@link #modify Error.Type.modify} -- retry after changing the data
	 * sent.
	 * <li>{@link #auth Eror.Type.auth} -- retry after providing credentials.
	 * <li>{@link #wait Error.Type.wait} -- retry after waiting (the error is
	 * temporary).
	 * </ul>
	 * 
	 * Implementation note: one of the XMPP error types is "continue". Because
	 * "continue" is a reserved Java keyword, the enum name is
	 * <tt>continue_processing</tt>. The {@link #toXMPP()} and
	 * {@link #fromXMPP(String)} methods can be used to convert between the
	 * enumertation values and XMPP error type strings.
	 */
	public enum Type {

		/**
		 * Do not retry (the error is unrecoverable).
		 */
		cancel,

		/**
		 * Proceed (the condition was only a warning). This represents the
		 * "continue" error code in XMPP; because "continue" is a reserved
		 * keyword in Java the enum name has been changed.
		 */
		continue_processing,

		/**
		 * Retry after changing the data sent.
		 */
		modify,

		/**
		 * Retry after providing credentials.
		 */
		auth,

		/**
		 * Retry after waiting (the error is temporary).
		 */
		wait;

		/**
		 * Converts a String value into its Type representation.
		 * 
		 * @param type
		 *            the String value.
		 * @return the condition corresponding to the String.
		 */
		public final static Type fromXMPP(String type) {
			if (type != null && type.equals("continue"))
				return continue_processing;
			
			return valueOf(type);
		}

		/**
		 * Returns the error code as a valid XMPP error code string.
		 * 
		 * @return the XMPP error code value.
		 */
		@Override
		public final String toString() {
			if (this == continue_processing)
				return "continue";
			
			return super.toString();
		}
	}
}