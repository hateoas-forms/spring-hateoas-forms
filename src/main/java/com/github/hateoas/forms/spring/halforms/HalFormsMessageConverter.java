package com.github.hateoas.forms.spring.halforms;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hateoas.forms.spring.halforms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;

public class HalFormsMessageConverter extends AbstractHttpMessageConverter<Object> {

	private final ObjectMapper objectMapper;

	public static final String HAL_FORMS_MEDIA_TYPE = "application/prs.hal-forms+json";

	public HalFormsMessageConverter(final ObjectMapper objectMapper, final RelProvider relProvider, final CurieProvider curieProvider,
			final MessageSourceAccessor messageSourceAccessor) {
		this.objectMapper = objectMapper;

		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new Jackson2HalFormsModule());
		objectMapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, messageSourceAccessor, true));
		setSupportedMediaTypes(Arrays.asList(MediaType.parseMediaType(HAL_FORMS_MEDIA_TYPE)));
	}

	@Override
	protected boolean supports(final Class<?> clazz) {
		return true;
	}

	@Override
	protected boolean canWrite(final MediaType mediaType) {
		if (mediaType == null) {
			return false;
		}
		return super.canWrite(mediaType);
	}

	@Override
	protected Object readInternal(final Class<? extends Object> clazz, final HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		return null;
	}

	@Override
	protected void writeInternal(final Object t, final HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		Object entity = HalFormsUtils.toHalFormsDocument(t, objectMapper);

		JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputMessage.getBody(), JsonEncoding.UTF8);

		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}

		try {
			objectMapper.writeValue(jsonGenerator, entity);
		}
		catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}

}
