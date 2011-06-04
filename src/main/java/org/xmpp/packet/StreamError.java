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
 * A stream error. Stream errors have a condition and they can optionally
 * include explanation text.
 * 
 * @author Matt Tucker
 */
@NotThreadSafe
public class StreamError extends BaseXML {

	private static final String ERROR_NAMESPACE = "urn:ietf:params:xml:ns:xmpp-streams";

	/**
	 * Construcs a new StreamError with the specified condition.
	 * 
	 * @param condition
	 *            the error condition.
	 */
	public StreamError(final Condition condition) {
		super("error:stream", "http://etherx.jabber.org/streams");
		setCondition(condition);
	}

	/**
	 * Constructs a new StreamError with the specified condition and error text.
	 * 
	 * @param condition
	 *            the error condition.
	 * @param text
	 *            the text description of the error.
	 */
	public StreamError(final Condition condition, final String text) {
		this(condition);
		setText(text, null);
	}

	/**
	 * Constructs a new StreamError with the specified condition and error text.
	 * 
	 * @param condition
	 *            the error condition.
	 * @param text
	 *            the text description of the error.
	 * @param language
	 *            the language code of the error description (e.g. "en").
	 */
	public StreamError(final Condition condition, final String text, final String language) {
		this(condition);
		setText(text, language);
	}

	/**
	 * Constructs a new StreamError using an existing Element. This is useful
	 * for parsing incoming error Elements into StreamError objects.
	 * 
	 * @param element
	 *            the stream error Element.
	 */
	public StreamError(final Element element) {
		super(element);
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

	@Override
	public StreamError clone() {
		return new StreamError(element);
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
		 * The entity has sent XML that cannot be processed; this error MAY be
		 * used instead of the more specific XML-related errors, such as
		 * &lt;bad-namespace-prefix/&gt;, &lt;invalid-xml/&gt;,
		 * &lt;restricted-xml/&gt;, &lt;unsupported-encoding/&gt;, and
		 * &lt;xml-not-well-formed/&gt;, although the more specific errors are
		 * preferred.
		 */
		bad_format,

		/**
		 * The entity has sent a namespace prefix that is unsupported, or has
		 * sent no namespace prefix on an element that requires such a prefix.
		 */
		bad_namespace_prefix,

		/**
		 * The server is closing the active stream for this entity because a new
		 * stream has been initiated that conflicts with the existing stream.
		 */
		conflict,

		/**
		 * The entity has not generated any traffic over the stream for some
		 * period of time (configurable according to a local service policy).
		 */
		connection_timeout,

		/**
		 * The value of the 'to' attribute provided by the initiating entity in
		 * the stream header corresponds to a hostname that is no longer hosted
		 * by the server.
		 */
		host_gone,

		/**
		 * The value of the 'to' attribute provided by the initiating entity in
		 * the stream header does not correspond to a hostname that is hosted by
		 * the server.
		 */
		host_unknown,

		/**
		 * A stanza sent between two servers lacks a 'to' or 'from' attribute
		 * (or the attribute has no value).
		 */
		improper_addressing,

		/**
		 * The server has experienced a misconfiguration or an
		 * otherwise-undefined internal error that prevents it from servicing
		 * the stream.
		 */
		internal_server_error,

		/**
		 * The JID or hostname provided in a 'from' address does not match an
		 * authorized JID or validated domain negotiated between servers via
		 * SASL or dialback, or between a client and a server via authentication
		 * and resource binding.
		 */
		invalid_from,

		/**
		 * The stream ID or dialback ID is invalid or does not match an ID
		 * previously provided.
		 */
		invalid_id,

		/**
		 * the streams namespace name is something other than
		 * "http://etherx.jabber.org/streams" or the dialback namespace name is
		 * something other than "jabber:server:dialback".
		 */
		invalid_namespace,

		/**
		 * The entity has sent invalid XML over the stream to a server that
		 * performs validation.
		 */
		invalid_xml,

		/**
		 * The entity has attempted to send data before the stream has been
		 * authenticated, or otherwise is not authorized to perform an action
		 * related to stream negotiation; the receiving entity MUST NOT process
		 * the offending stanza before sending the stream error.
		 */
		not_authorized,

		/**
		 * The entity has violated some local service policy; the server MAY
		 * choose to specify the policy in the <text/> element or an
		 * application-specific condition element.
		 */
		policy_violation,

		/**
		 * The server is unable to properly connect to a remote entity that is
		 * required for authentication or authorization.
		 */
		remote_connection_failed,

		/**
		 * The server lacks the system resources necessary to service the
		 * stream.
		 */
		resource_constraint,

		/**
		 * The entity has attempted to send restricted XML features such as a
		 * comment, processing instruction, DTD, entity reference, or unescaped
		 * character.
		 */
		restricted_xml,

		/**
		 * The server will not provide service to the initiating entity but is
		 * redirecting traffic to another host; the server SHOULD specify the
		 * alternate hostname or IP address (which MUST be a valid domain
		 * identifier) as the XML character data of the &lt;see-other-host/&gt;
		 * element.
		 */
		see_other_host,

		/**
		 * The server is being shut down and all active streams are being
		 * closed.
		 */
		system_shutdown,

		/**
		 * The error condition is not one of those defined by the other
		 * conditions in this list; this error condition SHOULD be used only in
		 * conjunction with an application-specific condition.
		 */
		undefined_condition,

		/**
		 * The initiating entity has encoded the stream in an encoding that is
		 * not supported by the server.
		 */
		unsupported_encoding,

		/**
		 * The initiating entity has sent a first-level child of the stream that
		 * is not supported by the server.
		 */
		unsupported_stanza_type,

		/**
		 * the value of the 'version' attribute provided by the initiating
		 * entity in the stream header specifies a version of XMPP that is not
		 * supported by the server; the server MAY specify the version(s) it
		 * supports in the &lt;text/&gt; element.
		 */
		unsupported_version,

		/**
		 * The initiating entity has sent XML that is not well-formed.
		 */
		xml_not_well_formed;

		/**
		 * Converts a String value into its Condition representation.
		 * 
		 * @param condition
		 *            the String value.
		 * @return the condition corresponding to the String.
		 */
		public final static Condition fromXMPP(final String condition) {
			return valueOf(condition.replace("-", "_"));
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
}
