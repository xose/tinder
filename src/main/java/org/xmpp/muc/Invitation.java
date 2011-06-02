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

import net.jcip.annotations.NotThreadSafe;

import org.w3c.dom.Element;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

/**
 * Represents an invitation to a Multi-User Chat room from a room occupant to a
 * user that is not an occupant of the room. The invitation must be <b>sent to
 * the room</b> and it's the room responsibility to forward the invitation to
 * the invitee. The <b>sender of the invitation must be the real full JID of the
 * inviter</b>.
 * <p>
 * 
 * Code example:
 * 
 * <pre>
 * // Invite the someone to the room.
 * Invitation invitation = new Invitation(&quot;invitee@jabber.org&quot;, &quot;Join this excellent room&quot;);
 * invitation.setTo(&quot;room@conference.jabber.org&quot;);
 * invitation.setFrom(&quot;inviter@jabber.org/notebook&quot;);
 * 
 * component.sendPacket(invitation);
 * </pre>
 * 
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class Invitation extends Message {

	/**
	 * Creates a new invitation.
	 * 
	 * @param invitee
	 *            the XMPP address of the invitee. The room will forward the
	 *            invitation to this address.
	 * @param reason
	 *            the reason why the invitation is being sent.
	 */
	public Invitation(final JID invitee, final String reason) {
		super();
		final Element child = addChildElement(element, "x", "http://jabber.org/protocol/muc#user");
		final Element invite = addChildElement(child, "invite");

		invite.setAttribute("to", invitee.toString());
		if (reason != null && !reason.isEmpty()) {
			addChildElement(invite, "reason").setTextContent(reason);
		}
	}
}
