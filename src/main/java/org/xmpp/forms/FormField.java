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

package org.xmpp.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.w3c.dom.Element;
import org.xmpp.util.BaseXML;

/**
 * Represents a field of a form. The field could be used to represent a question
 * to complete, a completed question or a data returned from a search. The exact
 * interpretation of the field depends on the context where the field is used.
 * 
 * @author Gaston Dombiak
 */
@NotThreadSafe
public class FormField extends BaseXML {

	protected FormField() {
		super("field");
	}

	protected FormField(final Element element) {
		super(element);
	}

	/**
	 * Returns an indicative of the format for the data to answer. Valid formats
	 * are:
	 * <p/>
	 * <ul>
	 * <li>text-single -> single line or word of text
	 * <li>text-private -> instead of showing the user what they typed, you show
	 * ***** to protect it
	 * <li>text-multi -> multiple lines of text entry
	 * <li>list-single -> given a list of choices, pick one
	 * <li>list-multi -> given a list of choices, pick one or more
	 * <li>boolean -> 0 or 1, true or false, yes or no. Default value is 0
	 * <li>fixed -> fixed for putting in text to show sections, or just
	 * advertise your web site in the middle of the form
	 * <li>hidden -> is not given to the user at all, but returned with the
	 * questionnaire
	 * <li>jid-single -> Jabber ID - choosing a JID from your roster, and
	 * entering one based on the rules for a JID.
	 * <li>jid-multi -> multiple entries for JIDs
	 * </ul>
	 * 
	 * @return format for the data to answer.
	 */
	public Type getType() {
		final String type = getAttribute(element, "type");

		return type != null ? Type.fromXMPP(type) : null;
	}

	/**
	 * Sets an indicative of the format for the data to answer. Valid formats
	 * are:
	 * <p/>
	 * <ul>
	 * <li>text-single -> single line or word of text
	 * <li>text-private -> instead of showing the user what they typed, you show
	 * ***** to protect it
	 * <li>text-multi -> multiple lines of text entry
	 * <li>list-single -> given a list of choices, pick one
	 * <li>list-multi -> given a list of choices, pick one or more
	 * <li>boolean -> 0 or 1, true or false, yes or no. Default value is 0
	 * <li>fixed -> fixed for putting in text to show sections, or just
	 * advertise your web site in the middle of the form
	 * <li>hidden -> is not given to the user at all, but returned with the
	 * questionnaire
	 * <li>jid-single -> Jabber ID - choosing a JID from your roster, and
	 * entering one based on the rules for a JID.
	 * <li>jid-multi -> multiple entries for JIDs
	 * </ul>
	 * 
	 * @param type
	 *            an indicative of the format for the data to answer.
	 */
	public void setType(final Type type) {
		setAttribute(element, "type", type != null ? type.toString() : null);
	}

	/**
	 * Returns the variable name that the question is filling out.
	 * 
	 * @return the variable name of the question.
	 */
	public String getVariable() {
		return getAttribute(element, "var");
	}

	/**
	 * Sets the attribute that uniquely identifies the field in the context of
	 * the form. If the field is of type "fixed" then the variable is optional.
	 * 
	 * @param var
	 *            the unique identifier of the field in the context of the form.
	 */
	public void setVariable(final String var) {
		setAttribute(element, "var", var);
	}

	/**
	 * Returns the label of the question which should give enough information to
	 * the user to fill out the form.
	 * 
	 * @return label of the question.
	 */
	public String getLabel() {
		return getAttribute(element, "label");
	}

	/**
	 * Sets the label of the question which should give enough information to
	 * the user to fill out the form.
	 * 
	 * @param label
	 *            the label of the question.
	 */
	public void setLabel(final String label) {
		setAttribute(element, "label", label);
	}

	/**
	 * Returns a description that provides extra clarification about the
	 * question. This information could be presented to the user either in
	 * tool-tip, help button, or as a section of text before the question.
	 * <p>
	 * <p/>
	 * If the question is of type FIXED then the description should remain
	 * empty.
	 * 
	 * @return description that provides extra clarification about the question.
	 */
	public String getDescription() {
		return getChildElementText(element, "desc");
	}

	/**
	 * Sets a description that provides extra clarification about the question.
	 * This information could be presented to the user either in tool-tip, help
	 * button, or as a section of text before the question.
	 * <p>
	 * <p/>
	 * If the question is of type FIXED then the description should remain
	 * empty.
	 * <p>
	 * No new description will be set, if the provided argument is <tt>null</tt>
	 * or an empty String (although an existing description will be removed).
	 * 
	 * @param description
	 *            provides extra clarification about the question.
	 */
	public void setDescription(final String description) {
		setChildElementText(element, "desc", description);
	}

	/**
	 * Returns true if the question must be answered in order to complete the
	 * questionnaire.
	 * 
	 * @return true if the question must be answered in order to complete the
	 *         questionnaire.
	 */
	public boolean isRequired() {
		return getChildElement(element, "required") != null;
	}

	/**
	 * Sets if the question must be answered in order to complete the
	 * questionnaire.
	 * 
	 * @param required
	 *            if the question must be answered in order to complete the
	 *            questionnaire.
	 */
	public void setRequired(final boolean required) {
		// Remove an existing desc element.
		if (!required && isRequired()) {
			element.removeChild(getChildElement(element, "required"));
		} else if (required && !isRequired()) {
			addChildElement(element, "required");
		}
	}

	/**
	 * Returns an Iterator for the default values of the question if the
	 * question is part of a form to fill out. Otherwise, returns an Iterator
	 * for the answered values of the question.
	 * 
	 * @return an Iterator for the default values or answered values of the
	 *         question.
	 */
	public List<String> getValues() {
		final List<String> answer = new ArrayList<String>();

		for (final Element el : getChildElements(element, "value")) {
			answer.add(el.getTextContent().trim());
		}

		return answer;
	}

	/**
	 * Returns the first value from the FormField, or 'null' if no value has
	 * been set.
	 * 
	 * @param formField
	 *            The field from which to return the first value.
	 * @return String based value, or 'null' if the FormField has no values.
	 */
	public String getFirstValue() {
		return getChildElement(element, "value").getTextContent();
	}

	/**
	 * Adds a default value to the question if the question is part of a form to
	 * fill out. Otherwise, adds an answered value to the question.
	 * <p>
	 * Nothing will be added if the provided argument is <tt>null</tt>.
	 * 
	 * @param value
	 *            a default value or an answered value of the question.
	 */
	public void addValue(final Object value) {
		if (value == null)
			return;

		addChildElement(element, "value").setTextContent(DataForm.encode(value));
	}

	/**
	 * Removes all the values of the field.
	 */
	public void clearValues() {
		for (final Element el : getChildElements(element, "value")) {
			element.removeChild(el);
		}
	}

	/**
	 * Returns the available options to answer for this question. The returned
	 * options cannot be modified but they will be updated if the underlying DOM
	 * object gets updated.
	 * 
	 * @return the available options to answer for this question.
	 */
	public List<Option> getOptions() {
		final List<Option> answer = new ArrayList<Option>();

		for (final Element el : getChildElements(element, "option")) {
			answer.add(new Option(getAttribute(el, "label"), getChildElementText(el, "value")));
		}

		return Collections.unmodifiableList(answer);
	}

	/**
	 * Adds an available option to the question that the user has in order to
	 * answer the question.
	 * <p>
	 * If argument 'value' is <tt>null</tt> or an empty String, no option
	 * element will be added.
	 * 
	 * @param label
	 *            a label that represents the option. Optional argument.
	 * @param value
	 *            the value of the option.
	 */
	public void addOption(final String label, final String value) {
		if (value == null || value.trim().length() == 0)
			return;

		final Element option = addChildElement(element, "option");
		setAttribute(option, "label", label);
		setChildElementText(option, "value", value);
	}

	@Override
	public FormField clone() {
		return new FormField(element);
	}

	/**
	 * Represents the available option of a given FormField.
	 * 
	 * @author Gaston Dombiak
	 */
	public static final class Option {

		private final String label;
		private final String value;

		private Option(final String label, final String value) {
			this.label = label;
			this.value = value;
		}

		/**
		 * Returns the label that represents the option.
		 * 
		 * @return the label that represents the option.
		 */
		public final String getLabel() {
			return label;
		}

		/**
		 * Returns the value of the option.
		 * 
		 * @return the value of the option.
		 */
		public final String getValue() {
			return value;
		}
	}

	/**
	 * Type-safe enumeration to represent the field type of the Data forms.
	 * <p>
	 * 
	 * Implementation note: XMPP error conditions use "-" characters in their
	 * names such as "jid-multi". Because "-" characters are not valid
	 * identifier parts in Java, they have been converted to "_" characters in
	 * the enumeration names, such as <tt>jid_multi</tt>. The {@link #toXMPP()}
	 * and {@link #fromXMPP(String)} methods can be used to convert between the
	 * enumertation values and Type code strings.
	 */
	public enum Type {
		/**
		 * The field enables an entity to gather or provide an either-or choice
		 * between two options. The allowable values are 1 for yes/true/assent
		 * and 0 for no/false/decline. The default value is 0.
		 */
		boolean_type,

		/**
		 * The field is intended for data description (e.g., human-readable text
		 * such as "section" headers) rather than data gathering or provision.
		 * The <value/> child SHOULD NOT contain newlines (the \n and \r
		 * characters); instead an application SHOULD generate multiple fixed
		 * fields, each with one <value/> child.
		 */
		fixed,

		/**
		 * The field is not shown to the entity providing information, but
		 * instead is returned with the form.
		 */
		hidden,

		/**
		 * The field enables an entity to gather or provide multiple Jabber IDs.
		 */
		jid_multi,

		/**
		 * The field enables an entity to gather or provide multiple Jabber IDs.
		 */
		jid_single,

		/**
		 * The field enables an entity to gather or provide one or more options
		 * from among many.
		 */
		list_multi,

		/**
		 * The field enables an entity to gather or provide one option from
		 * among many.
		 */
		list_single,

		/**
		 * The field enables an entity to gather or provide multiple lines of
		 * text.
		 */
		text_multi,

		/**
		 * The field enables an entity to gather or provide a single line or
		 * word of text, which shall be obscured in an interface (e.g., *****).
		 */
		text_private,

		/**
		 * The field enables an entity to gather or provide a single line or
		 * word of text, which may be shown in an interface. This field type is
		 * the default and MUST be assumed if an entity receives a field type it
		 * does not understand.
		 */
		text_single;

		/**
		 * Converts a String value into its Type representation.
		 * 
		 * @param type
		 *            the String value.
		 * @return the type corresponding to the String.
		 */
		public final static Type fromXMPP(final String type) {
			if (type != null && type.equals("boolean"))
				return boolean_type;

			return valueOf(type);
		}

		/**
		 * Returns the Field Type as a valid Field Type code string.
		 * 
		 * @return the Field Type value.
		 */
		@Override
		public final String toString() {
			if (this == boolean_type)
				return "boolean";

			return super.toString();
		}
	}
}
