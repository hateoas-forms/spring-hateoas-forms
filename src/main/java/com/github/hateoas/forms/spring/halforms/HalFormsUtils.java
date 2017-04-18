package com.github.hateoas.forms.spring.halforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hateoas.forms.affordance.ActionDescriptor;
import com.github.hateoas.forms.affordance.ActionInputParameter;
import com.github.hateoas.forms.affordance.ActionInputParameterVisitor;
import com.github.hateoas.forms.affordance.Affordance;
import com.github.hateoas.forms.affordance.SuggestType;

public class HalFormsUtils {

	public static Object toHalFormsDocument(final Object object, final ObjectMapper objectMapper) {
		if (object == null) {
			return null;
		}

		if (object instanceof ResourceSupport) {
			ResourceSupport rs = (ResourceSupport) object;
			List<Template> templates = new ArrayList<Template>();
			List<Link> links = new ArrayList<Link>();
			process(rs, links, templates, objectMapper);
			return new HalFormsDocument(links, templates);

		}
		else { // bean
			return object;
		}
	}

	private static void process(final ResourceSupport resource, final List<Link> links, final List<Template> templates,
			final ObjectMapper objectMapper) {
		for (Link link : resource.getLinks()) {
			if (link instanceof Affordance) {
				Affordance affordance = (Affordance) link;

				for (int i = 0; i < affordance.getActionDescriptors().size(); i++) {
					ActionDescriptor actionDescriptor = affordance.getActionDescriptors().get(i);
					if (i == 0) {
						links.add(affordance);
					}
					else {
						String key = actionDescriptor.getSemanticActionType();
						if (true || actionDescriptor.hasRequestBody() || !actionDescriptor.getRequestParamNames().isEmpty()) {
							Template template = templates.isEmpty() ? new Template()
									: new Template(key != null ? key : actionDescriptor.getHttpMethod().toLowerCase());
							template.setContentType(actionDescriptor.getConsumes());

							template.setMethod(actionDescriptor.getHttpMethod());
							TemplateActionInputParameterVisitor visitor = new TemplateActionInputParameterVisitor(template,
									actionDescriptor, objectMapper);

							actionDescriptor.accept(visitor);

							templates.add(template);
						}
					}
				}
			}
			else {
				links.add(link);
			}
		}

	}

	public static Property getProperty(final ActionInputParameter actionInputParameter, final ActionDescriptor actionDescriptor,
			final Object propertyValue, final String name, final ObjectMapper objectMapper) {
		Map<String, Object> inputConstraints = actionInputParameter.getInputConstraints();

		boolean readOnly = actionInputParameter.isReadOnly();
		String regex = (String) inputConstraints.get(ActionInputParameter.PATTERN);
		boolean required = actionInputParameter.isRequired();

		String value = null;

		final List<com.github.hateoas.forms.affordance.Suggest<Object>> possibleValues = actionInputParameter
				.getPossibleValues(actionDescriptor);
		ValueSuggest<?> suggest = null;
		final SuggestType suggestType = actionInputParameter.getSuggestType();
		boolean multi = false;
		if (!possibleValues.isEmpty()) {
			try {
				if (propertyValue != null) {
					if (propertyValue.getClass().isEnum()) {
						value = propertyValue.toString();
					}
					else {
						value = objectMapper.writeValueAsString(propertyValue);
					}
				}
			}
			catch (JsonProcessingException e) {
			}

			if (actionInputParameter.isArrayOrCollection()) {
				multi = true;
			}
			String textField = null;
			String valueField = null;
			List<Object> values = new ArrayList<Object>();
			for (com.github.hateoas.forms.affordance.Suggest<Object> possibleValue : possibleValues) {
				values.add(possibleValue.getValue());
				textField = possibleValue.getTextField();
				valueField = possibleValue.getValueField();
			}
			suggest = new ValueSuggest<Object>(values, textField, valueField, suggestType);
		}
		else {
			if (propertyValue != null) {
				try {
					if (propertyValue instanceof List || propertyValue.getClass().isArray()) {
						value = objectMapper.writeValueAsString(propertyValue);
					}
					else {
						value = propertyValue.toString();
					}
				}
				catch (JsonProcessingException e) {
				}
			}
		}

		return new Property(name, readOnly, false, value, null, regex, required, multi, suggest);
	}

	static class TemplateActionInputParameterVisitor implements ActionInputParameterVisitor {

		private final Template template;

		private final ActionDescriptor actionDescriptor;

		private final ObjectMapper objectMapper;

		public TemplateActionInputParameterVisitor(final Template template, final ActionDescriptor actionDescriptor,
				final ObjectMapper objectMapper) {
			this.template = template;
			this.actionDescriptor = actionDescriptor;
			this.objectMapper = objectMapper;
		}

		@Override
		public void visit(final ActionInputParameter inputParameter) {
			Property property = getProperty(inputParameter, actionDescriptor, inputParameter.getValue(), inputParameter.getName(),
					objectMapper);

			template.getProperties().add(property);
		}

	}

}
