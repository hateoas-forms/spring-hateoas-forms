package com.github.hateoas.forms.spring.siren;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.hateoas.forms.affordance.SuggestType;
import com.github.hateoas.forms.spring.xhtml.beans.DummyController;
import com.github.hateoas.forms.spring.xhtml.beans.Item;

public class SirenItemsTest {

	ObjectMapper objectMapper = new ObjectMapper();

	SirenUtils sirenUtils = new SirenUtils();

	private DummyController dm;

	private final Map<Integer, SuggestType> suggestProperties = new HashMap<Integer, SuggestType>();

	private final Map<Integer, List<Map<String, Object>>> suggestsValuesList = new HashMap<Integer, List<Map<String, Object>>>();

	@Before
	public void setUp() {
		dm = new DummyController();
	}

	private void loadControlValues() {
		suggestProperties.put(2, SuggestType.INTERNAL);
		suggestProperties.put(3, SuggestType.EXTERNAL);
		suggestProperties.put(4, SuggestType.EXTERNAL);
		suggestProperties.put(5, SuggestType.EXTERNAL);
		suggestProperties.put(6, SuggestType.REMOTE);
		suggestProperties.put(7, SuggestType.REMOTE);
		suggestProperties.put(10, SuggestType.EXTERNAL);
		suggestProperties.put(11, SuggestType.INTERNAL);
		suggestProperties.put(14, SuggestType.EXTERNAL);
		suggestProperties.put(15, SuggestType.INTERNAL);
		suggestProperties.put(18, SuggestType.INTERNAL);
		suggestProperties.put(19, SuggestType.INTERNAL);
		suggestProperties.put(22, SuggestType.INTERNAL);
		suggestProperties.put(23, SuggestType.INTERNAL);
		suggestProperties.put(25, SuggestType.INTERNAL);
		suggestProperties.put(26, SuggestType.INTERNAL);
		suggestProperties.put(30, SuggestType.INTERNAL);
		suggestProperties.put(31, SuggestType.INTERNAL);
		suggestProperties.put(34, SuggestType.INTERNAL);
		suggestProperties.put(35, SuggestType.INTERNAL);
		suggestProperties.put(41, SuggestType.INTERNAL);
		suggestProperties.put(42, SuggestType.INTERNAL);
		suggestProperties.put(45, SuggestType.INTERNAL);
		suggestProperties.put(46, SuggestType.INTERNAL);
		suggestProperties.put(49, SuggestType.INTERNAL);
		suggestProperties.put(50, SuggestType.INTERNAL);

		List<Map<String, Object>> valuesList = new ArrayList<Map<String, Object>>();
		Map<String, Object> suggestValue = new HashMap<String, Object>();
		suggestValue.put("id", 1);
		suggestValue.put("name", "S1");
		valuesList.add(suggestValue);

		suggestValue = new HashMap<String, Object>();
		suggestValue.put("id", 2);
		suggestValue.put("name", "S2");
		valuesList.add(suggestValue);

		suggestValue = new HashMap<String, Object>();
		suggestValue.put("id", 3);
		suggestValue.put("name", "S3");
		valuesList.add(suggestValue);

		suggestValue = new HashMap<String, Object>();
		suggestValue.put("id", 4);
		suggestValue.put("name", "S4");
		valuesList.add(suggestValue);
		suggestsValuesList.put(3, valuesList);
		suggestsValuesList.put(4, valuesList);
		suggestsValuesList.put(5, valuesList);
		suggestsValuesList.put(10, valuesList);
		suggestsValuesList.put(14, valuesList);

		valuesList = new ArrayList<Map<String, Object>>();
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "ONE");
		suggestValue.put("prompt", "ONE");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "TWO");
		suggestValue.put("prompt", "TWO");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "THREE");
		suggestValue.put("prompt", "THREE");
		valuesList.add(suggestValue);
		suggestsValuesList.put(2, valuesList);
		suggestsValuesList.put(11, valuesList);
		suggestsValuesList.put(15, valuesList);

		valuesList = new ArrayList<Map<String, Object>>();
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "FOUR");
		suggestValue.put("prompt", "FOUR");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "FIVE");
		suggestValue.put("prompt", "FIVE");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "SIX");
		suggestValue.put("prompt", "SIX");
		valuesList.add(suggestValue);
		suggestsValuesList.put(18, valuesList);
		suggestsValuesList.put(19, valuesList);
		suggestsValuesList.put(22, valuesList);
		suggestsValuesList.put(23, valuesList);
		suggestsValuesList.put(30, valuesList);
		suggestsValuesList.put(31, valuesList);
		suggestsValuesList.put(34, valuesList);
		suggestsValuesList.put(35, valuesList);
		suggestsValuesList.put(41, valuesList);
		suggestsValuesList.put(42, valuesList);
		suggestsValuesList.put(45, valuesList);
		suggestsValuesList.put(46, valuesList);
		suggestsValuesList.put(49, valuesList);
		suggestsValuesList.put(50, valuesList);

		valuesList = new ArrayList<Map<String, Object>>();
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "true");
		suggestValue.put("prompt", "true");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "false");
		suggestValue.put("prompt", "false");
		valuesList.add(suggestValue);
		suggestsValuesList.put(25, valuesList);

		valuesList = new ArrayList<Map<String, Object>>();
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "0");
		suggestValue.put("prompt", "0");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "1");
		suggestValue.put("prompt", "1");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "2");
		suggestValue.put("prompt", "2");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "3");
		suggestValue.put("prompt", "3");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "4");
		suggestValue.put("prompt", "4");
		valuesList.add(suggestValue);
		suggestValue = new HashMap<String, Object>();
		suggestValue.put("value", "5");
		suggestValue.put("prompt", "5");
		valuesList.add(suggestValue);
		suggestsValuesList.put(26, valuesList);

	}

	@Test
	public void testNestedBeansToSirenEntityProperties() throws Exception {

		for (Item item : dm.items) {

			SirenEntity entity = new SirenEntity();
			sirenUtils.toSirenEntity(entity, item);
			String json = objectMapper.valueToTree(entity).toString();
			FileUtils.writeStringToFile(new File("siren.html"), json);

		}
	}

}
