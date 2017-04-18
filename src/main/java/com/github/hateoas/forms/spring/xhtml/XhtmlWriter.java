package com.github.hateoas.forms.spring.xhtml;

import static com.github.hateoas.forms.spring.xhtml.XhtmlWriter.OptionalAttributes.attr;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.hateoas.forms.action.Type;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.ActionInputParameterVisitor;
import com.github.hateoas.forms.affordance.Affordance;
import com.github.hateoas.forms.affordance.Suggest;
import com.github.hateoas.forms.affordance.SuggestType;
import com.github.hateoas.forms.spring.DefaultDocumentationProvider;
import com.github.hateoas.forms.spring.DocumentationProvider;

/**
 * Created by Dietrich on 09.02.2015.
 */
public class XhtmlWriter extends Writer {

	private final Writer writer;

	private List<String> stylesheets = Collections.emptyList();

	public static final String HTML_HEAD_START = "" + //
	// "<?xml version='1.0' encoding='UTF-8' ?>" + // formatter
			"<!DOCTYPE html>" + //
			"<html xmlns='http://www.w3.org/1999/xhtml'>" + //
			"  <head>" + //
			"    <meta charset=\"utf-8\"/>" + //
			"    <title>%s</title>";

	public static final String HTML_STYLESHEET = "" + //
			"    <link rel=\"stylesheet\" href=\"%s\"  />";

	public static final String HTML_HEAD_END = "" + //
			"  </head>" + //
			"  <body>" + //
			"    <div class=\"container\">\n" + //
			"      <div class=\"row\">";

	public static final String HTML_END = "" + //
			"      </div>" + //
			"    </div>" + //
			"  </body>" + //
			"</html>";

	private String methodParam = "_method";

	private DocumentationProvider documentationProvider = new DefaultDocumentationProvider();

	private final String formControlClass = "form-control";

	private final String formGroupClass = "form-group";

	private final String controlLabelClass = "control-label";

	public XhtmlWriter(final Writer writer) {
		this.writer = writer;
	}

	public void setMethodParam(final String methodParam) {
		this.methodParam = methodParam;
	}

	public void beginHtml(final String title) throws IOException {
		write(String.format(HTML_HEAD_START, title));
		for (String stylesheet : stylesheets) {
			write(String.format(HTML_STYLESHEET, stylesheet));
		}
		write(String.format(HTML_HEAD_END, title));
	}

	public void endHtml() throws IOException {
		write(HTML_END);
	}

	public void beginDiv() throws IOException {
		writer.write("<div>");
	}

	public void beginDiv(final OptionalAttributes attributes) throws IOException {
		writer.write("<div ");
		writeAttributes(attributes);
		endTag();
	}

	public void endDiv() throws IOException {
		writer.write("</div>");
	}

	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		writer.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

	public void beginUnorderedList() throws IOException {
		writer.write("<ul class=\"list-group\">");
	}

	public void endUnorderedList() throws IOException {
		writer.write("</ul>");
	}

	public void beginListItem() throws IOException {
		writer.write("<li class=\"list-group-item\">");
	}

	public void endListItem() throws IOException {
		writer.write("</li>");
	}

	public void beginSpan() throws IOException {
		writer.write("<span>");
	}

	public void endSpan() throws IOException {
		writer.write("</span>");
	}

	public void beginDl() throws IOException {
		// TODO: make this configurable?
		writer.write("<dl >");
	}

	public void endDl() throws IOException {
		writer.write("</dl>");
	}

	public void beginDt() throws IOException {
		writer.write("<dt>");
	}

	public void endDt() throws IOException {
		writer.write("</dt>");
	}

	public void beginDd() throws IOException {
		writer.write("<dd>");
	}

	public void endDd() throws IOException {
		writer.write("</dd>");
	}

	public void writeSpan(final Object value) throws IOException {
		beginSpan();
		writer.write(value.toString());
		endSpan();
	}

	public void writeDefinitionTerm(final Object value) throws IOException {
		beginDt();
		writer.write(value.toString());
		endDt();
	}

	public void setStylesheets(final List<String> stylesheets) {
		Assert.notNull(stylesheets);
		this.stylesheets = stylesheets;
	}

	public void setDocumentationProvider(final DocumentationProvider documentationProvider) {
		this.documentationProvider = documentationProvider;
	}

	public static class OptionalAttributes {

		private final Map<String, String> attributes = new LinkedHashMap<String, String>();

		@Override
		public String toString() {
			return attributes.toString();
		}

		/**
		 * Creates OptionalAttributes with one optional attribute having name if value is not null.
		 *
		 * @param name of first attribute
		 * @param value may be null
		 * @return builder with one attribute, attr builder if value is null
		 */
		public static OptionalAttributes attr(final String name, final String value) {
			Assert.isTrue(name != null && value != null || value == null);
			OptionalAttributes attributeBuilder = new OptionalAttributes();
			addAttributeIfValueNotNull(name, value, attributeBuilder);
			return attributeBuilder;
		}

		private static void addAttributeIfValueNotNull(final String name, final String value, final OptionalAttributes attributeBuilder) {
			if (value != null) {
				attributeBuilder.attributes.put(name, value);
			}
		}

		public OptionalAttributes and(final String name, final String value) {
			addAttributeIfValueNotNull(name, value, this);
			return this;
		}

		public Map<String, String> build() {
			return attributes;
		}

		/**
		 * Creates OptionalAttributes builder.
		 *
		 * @return builder
		 */
		public static OptionalAttributes attr() {
			return attr(null, null);
		}
	}

	public void writeLinks(final List<Link> links) throws IOException {
		for (Link link : links) {

			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;
				List<ActionDescriptor> actionDescriptors = affordance.getActionDescriptors();
				if (actionDescriptors.isEmpty()) {
					// treat like simple link
					appendLinkWithoutActionDescriptor(affordance);
				}
				else {
					if (affordance.isTemplated()) {
						// TODO ensure that template expansion always takes place for base uri
						if (!affordance.isBaseUriTemplated()) {
							for (ActionDescriptor actionDescriptor : actionDescriptors) {
								RequestMethod httpMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());
								// html does not allow templated action attr for forms, only render GET form
								if (RequestMethod.GET == httpMethod) {
									// TODO: partial uritemplate query must become hidden field
									appendForm(affordance, actionDescriptor);
								}
								// TODO write human-readable description of additional methods?
							}
						}
					}
					else {
						for (ActionDescriptor actionDescriptor : actionDescriptors) {
							// TODO write documentation about the supported action and maybe fields?
							if ("GET".equals(actionDescriptor.getHttpMethod()) && actionDescriptor.getRequestParamNames().isEmpty()) {
								beginDiv();
								// GET without params is simple <a href>
								writeAnchor(OptionalAttributes.attr("href", affordance.expand().getHref()).and("rel", affordance.getRel()),
										affordance.getRel());
								endDiv();
							}
							else {
								appendForm(affordance, actionDescriptor);
							}
						}
					}
				}
			}
			else { // simple link, may be templated
				appendLinkWithoutActionDescriptor(link);
			}
		}
	}

	/**
	 * Appends form and squashes non-GET or POST to POST. If required, adds _method field for handling by an appropriate filter such as
	 * Spring's HiddenHttpMethodFilter.
	 *
	 * @param affordance to make into a form
	 * @param actionDescriptor describing the form action
	 * @throws IOException
	 * @see <a href= "http://docs.spring.io/spring/docs/3.0 .x/javadoc-api/org/springframework/web/filter/HiddenHttpMethodFilter.html"
	 * >Spring MVC HiddenHttpMethodFilter</a>
	 */
	private void appendForm(final Affordance affordance, final ActionDescriptor actionDescriptor) throws IOException {
		String formName = actionDescriptor.getActionName();
		RequestMethod httpMethod = RequestMethod.valueOf(actionDescriptor.getHttpMethod());

		// Link's expand method removes non-required variables from URL
		String actionUrl = affordance.expand().getHref();
		beginForm(
				OptionalAttributes.attr("action", actionUrl).and("method", getHtmlConformingHttpMethod(httpMethod)).and("name", formName));
		write("<h4>");
		write("Form " + formName);
		write("</h4>");

		writeHiddenHttpMethodField(httpMethod);
		actionDescriptor.accept(new ActionInputParameterVisitor() {

			@Override
			public void visit(final ActionInputParameter actionInputParameter) {
				try {
					List<Suggest<Object>> possibleValues = actionInputParameter.getPossibleValues(actionDescriptor);
					if (!Type.HIDDEN.equals(actionInputParameter.getHtmlInputFieldType()) && !possibleValues.isEmpty()) {
						appendSelect(possibleValues, actionInputParameter);
					}
					else {
						if (actionInputParameter.isArrayOrCollection()) {
							// have as many inputs as there are call values, list of 5 nulls gives you five input fields
							// TODO support for free list input instead, code on demand?
							Object[] callValues = actionInputParameter.getValues();
							int items = callValues.length;
							for (int i = 0; i < items; i++) {
								Object value;
								if (i < callValues.length) {
									value = callValues[i];
								}
								else {
									value = null;
								}
								appendInput(actionInputParameter, value); // not readonly
							}
						}
						else {
							String callValueFormatted = actionInputParameter.getValueFormatted();
							appendInput(actionInputParameter, callValueFormatted); // not readonly
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		inputButton(Type.SUBMIT, capitalize(httpMethod.name().toLowerCase()));
		endForm();
	}

	private void appendLinkWithoutActionDescriptor(final Link link) throws IOException {
		if (link.isTemplated()) {
			// TODO ensure that template expansion takes place for base uri
			Link expanded = link.expand(); // remove query variables
			beginForm(OptionalAttributes.attr("action", expanded.getHref()).and("method", "GET"));
			List<TemplateVariable> variables = link.getVariables();
			for (TemplateVariable variable : variables) {
				String variableName = variable.getName();
				String label = variable.hasDescription() ? variable.getDescription() : variableName;
				writeLabelWithDoc(label, variableName, null); // no documentation url
				input(variableName, Type.TEXT);
			}
		}
		else {
			String rel = link.getRel();
			String title = rel != null ? rel : link.getHref();
			// TODO: write html <link> instead of anchor <a> here?
			writeAnchor(OptionalAttributes.attr("href", link.getHref()).and("rel", link.getRel()), title);
		}
	}

	/**
	 * Classic submit or reset button.
	 *
	 * @param type submit or reset
	 * @param value caption on the button
	 * @throws IOException
	 */
	private void inputButton(final Type type, final String value) throws IOException {
		write("<input type=\"");
		write(type.toString());
		write("\" ");
		write("value");
		write("=");
		quote();
		write(value);
		quote();
		write("/>");
	}

	private void hidden(final String fieldName, final String value) throws IOException {
		input(fieldName, Type.HIDDEN, attr("value", value));
	}

	private void input(final String fieldName, final Type type, final OptionalAttributes attributes) throws IOException {
		write("<input name=\"");
		write(fieldName);
		write("\" type=\"");
		write(type.toString());
		write("\" class=\"");
		write(formControlClass);
		write("\" ");
		writeAttributes(attributes);
		write("/>");
	}

	private void input(final String fieldName, final Type type) throws IOException {
		input(fieldName, type, attr());
	}

	private void beginLabel(final OptionalAttributes attributes) throws IOException {
		write("<label");
		writeAttributes(attributes);
		endTag();
	}

	private void endLabel() throws IOException {
		write("</label>");
	}

	private void beginForm(final OptionalAttributes attrs) throws IOException {
		write("<form class=\"well\" ");
		writeAttributes(attrs);
		write(">");
	}

	private void writeAttributes(final OptionalAttributes attrs) throws IOException {
		Map<String, String> attributes = attrs.build();
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			write(" ");
			write(entry.getKey());
			write("=");
			quote();
			write(entry.getValue());
			quote();
		}
	}

	private void quote() throws IOException {
		write("\"");
	}

	private void endForm() throws IOException {
		write("</form>");
	}

	public void beginAnchor(final OptionalAttributes attrs) throws IOException {
		write("<a ");
		writeAttributes(attrs);
		endTag();
	}

	public void endAnchor() throws IOException {
		write("</a>");
	}

	private void writeAnchor(final OptionalAttributes attrs, final String value) throws IOException {
		beginAnchor(attrs);
		write(value);
		endAnchor();
	}

	private String capitalize(final String name) {
		if (name != null && name.length() != 0) {
			char[] chars = name.toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			return new String(chars);
		}
		else {
			return name;
		}
	}

	private void writeHiddenHttpMethodField(final RequestMethod httpMethod) throws IOException {
		switch (httpMethod) {
		case GET:
		case POST:
			break;
		default:
			hidden(methodParam, httpMethod.name());
		}
	}

	private String getHtmlConformingHttpMethod(final RequestMethod requestMethod) {
		String ret;
		switch (requestMethod) {
		case GET:
		case POST:
			ret = requestMethod.name();
			break;
		default:
			ret = RequestMethod.POST.name();
		}
		return ret;
	}

	private void appendInput(final ActionInputParameter actionInputParameter, final Object value) throws IOException {
		if (actionInputParameter.isRequestBody()) { // recurseBeanProperties does that
			throw new IllegalArgumentException("cannot append input field for requestBody");
		}
		Type htmlInputFieldType = actionInputParameter.getHtmlInputFieldType();
		String requestParamName = actionInputParameter.getName();
		Assert.notNull(htmlInputFieldType, requestParamName);
		String val = value == null ? "" : value.toString();
		if (Type.HIDDEN.equals(htmlInputFieldType)) {
			hidden(requestParamName, val);
		}
		else {
			beginDiv(OptionalAttributes.attr("class", formGroupClass));
			String documentationUrl = documentationProvider.getDocumentationUrl(actionInputParameter, value);
			// TODO consider @Input-include/exclude/hidden here
			OptionalAttributes attrs = attr("value", val);
			if (actionInputParameter.isReadOnly()) {
				attrs.and(ActionInputParameter.READONLY, ActionInputParameter.READONLY);
			}
			if (actionInputParameter.isRequired()) {
				attrs.and("required", "required");
			}
			writeLabelWithDoc(requestParamName, documentationUrl);
			if (actionInputParameter.hasInputConstraints()) {
				for (Map.Entry<String, Object> inputConstraint : actionInputParameter.getInputConstraints().entrySet()) {
					attrs.and(inputConstraint.getKey(), inputConstraint.getValue().toString());
				}
			}
			input(requestParamName, htmlInputFieldType, attrs);
			endDiv();
		}
	}

	private void writeLabelWithDoc(final String fieldName, final String documentationUrl) throws IOException {
		writeLabelWithDoc(fieldName, fieldName, documentationUrl);
	}

	private void writeLabelWithDoc(final String label, final String fieldName, final String documentationUrl) throws IOException {
		beginLabel(attr("for", fieldName).and("class", controlLabelClass));
		if (documentationUrl == null) {
			write(label);
		}
		else {
			beginAnchor(attr("href", documentationUrl).and("title", documentationUrl));
			write(label);
			endAnchor();
		}
		endLabel();
	}

	private void appendSelect(final List<Suggest<Object>> possibleValues, final ActionInputParameter actionInputParameter)
			throws IOException {
		beginDiv(attr("class", formGroupClass));
		String requestParamName = actionInputParameter.getName();
		boolean isMultiple = actionInputParameter.isArrayOrCollection();
		OptionalAttributes attributes = attr("class", formControlClass);
		Object callValue;
		String[] actualValues;
		if (isMultiple) {
			Object[] actionParamValues = actionInputParameter.getValues();
			if (actionParamValues.length > 0) {
				callValue = actionParamValues[0];
			}
			else {
				callValue = null;
			}
			actualValues = new String[actionParamValues.length];
			for (int i = 0; i < actionParamValues.length; i++) {
				actualValues[i] = String.valueOf(actionParamValues[i]);
			}

			attributes = attributes.and("multiple", "multiple");
		}
		else {
			callValue = actionInputParameter.getValue();
			actualValues = new String[] { callValue != null ? callValue.toString() : "" };
		}
		String documentationUrl = documentationProvider.getDocumentationUrl(actionInputParameter, callValue);
		writeLabelWithDoc(requestParamName, documentationUrl);

		if (actionInputParameter.isReadOnly()) {
			attributes.and("disabled", "disabled");

			int items = actualValues.length;
			for (int i = 0; i < items; i++) {
				Object value;
				if (i < actualValues.length) {
					value = actualValues[i];
				}
				else {
					value = "";
				}
				hidden(requestParamName, value.toString());
			}
		}
		if (actionInputParameter.isRequired()) {
			attributes.and("required", "required");
		}

		// Check if is a remote type select
		if (SuggestType.REMOTE == actionInputParameter.getSuggestType()) {
			attributes.and("data-remote", possibleValues.get(0).getValue().toString());
			beginSelect(requestParamName, possibleValues.size(), attributes);
			for (Object possibleValue : actualValues) {
				option(String.valueOf(possibleValue), attr("selected", "selected").and("value", String.valueOf(possibleValue)));
			}
		}
		else {
			beginSelect(requestParamName, possibleValues.size(), attributes);
			for (Suggest<?> possibleValue : possibleValues) {
				if (ObjectUtils.containsElement(actualValues, possibleValue.getValueAsString())) {
					option(possibleValue.getText(), attr("selected", "selected").and("value", possibleValue.getValueAsString()));
				}
				else {
					option(possibleValue.getText(), attr("value", possibleValue.getValueAsString()));
				}
			}
		}
		endSelect();
		endDiv();
	}

	private void option(final String option, final OptionalAttributes attr) throws IOException {
		beginTag("option");
		writeAttributes(attr);
		endTag();
		write(option);
		write("</option>");
	}

	private void beginTag(final String tag) throws IOException {
		write("<");
		write(tag);
	}

	private void endTag() throws IOException {
		write(">");
	}

	private void beginSelect(final String name, final int size, final OptionalAttributes attrs) throws IOException {
		beginTag("select");
		write(" name=");
		quote(name);
		write(" id=");
		quote(name);
		writeAttributes(attrs);
		endTag();
	}

	private void endSelect() throws IOException {
		write("</select>");
	}

	private void quote(final String s) throws IOException {
		quote();
		write(s);
		quote();
	}
}
