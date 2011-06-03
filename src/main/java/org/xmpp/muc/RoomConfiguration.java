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

package org.xmpp.muc;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.Element;
import org.xmpp.packet.IQ;

/**
 * RoomConfiguration is a packet that helps to set the configuration of MUC
 * rooms. RoomConfiguration is a speacial IQ packet whose child element contains
 * a data form. The data form holds the fields to set together with a list of
 * values.
 * <p>
 * 
 * Code example:
 * 
 * <pre>
 * // Set the fields and the values.
 * Map&lt;String, Collection&lt;String&gt;&gt; fields = new HashMap&lt;String, Collection&lt;String&gt;&gt;();
 * // Make a non-public room
 * List&lt;String&gt; values = new ArrayList&lt;String&gt;();
 * values.add(&quot;0&quot;);
 * fields.put(&quot;muc#roomconfig_publicroom&quot;, values);
 * 
 * // Create a RoomConfiguration with the fields and values
 * RoomConfiguration conf = new RoomConfiguration(fields);
 * conf.setTo(&quot;room@conference.jabber.org&quot;);
 * conf.setFrom(&quot;john@jabber.org/notebook&quot;);
 * 
 * component.sendPacket(conf);
 * </pre>
 * 
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class RoomConfiguration extends IQ {

	/**
	 * Creates a new IQ packet that contains the field and values to send for
	 * setting the room configuration.
	 * 
	 * @param fieldValues
	 *            the list of fields associated with the list of values.
	 */
	public RoomConfiguration(final Map<String, Collection<String>> fieldValues) {
		super(Type.set);

		final Element query = setIQChildElement("query", "http://jabber.org/protocol/muc#owner");
		final Element form = addChildElement(query, "x", "jabber:x:data");
		form.setAttribute("type", "submit");

		// TODO: use XMPP Forms

		// Add static field
		Element field = addChildElement(form, "field");
		field.setAttribute("var", "FORM_TYPE");
		addChildElement(field, "value").setTextContent("http://jabber.org/protocol/muc#roomconfig");

		// Add the specified fields and their corresponding values
		for (final Entry<String, Collection<String>> entry : fieldValues.entrySet()) {
			field = addChildElement(form, "field");
			field.setAttribute("var", entry.getKey());
			for (final String value : entry.getValue()) {
				addChildElement(field, "value").setTextContent(value);
			}
		}
	}
}
