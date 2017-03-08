package com.github.hateoas.forms.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.hateoas.forms.action.Input;
import com.github.hateoas.forms.action.Select;

public class ActionParameterTypeTest {

	@Input
	public List<String> values;

	@Select
	public RequestMethod method;

	public String data;

	public String[] array;

	public ActionParameterTypeTest() {
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test() throws SecurityException, NoSuchMethodException {

		Field[] fields = ActionParameterTypeTest.class.getFields();
		List<FieldParameterType> fieldsTypes = new ArrayList<FieldParameterType>();
		for (Field field : fields) {
			fieldsTypes.add(new FieldParameterType(field));
		}
		Method m = ActionParameterTypeTest.class.getMethod("method", List.class, RequestMethod.class, String.class, String[].class);
		List<MethodParameterType> mpTypes = new ArrayList<MethodParameterType>();
		for (int i = 0; i < 4; i++) {
			mpTypes.add(new MethodParameterType(new MethodParameter(m, i)));
		}

		for (int i = 0; i < fieldsTypes.size(); i++) {
			checkEquality(fieldsTypes.get(i), mpTypes.get(i));
		}

	}

	private void checkEquality(final ActionParameterType type1, final ActionParameterType type2) {
		Assert.assertEquals(type1.toString(), type2.toString());
	}

	public void method(@Input final List<String> values, @Select final RequestMethod method, final String data, final String[] array) {

	}

}
