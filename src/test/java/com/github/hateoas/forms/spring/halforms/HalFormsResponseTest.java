package com.github.hateoas.forms.spring.halforms;

import static com.github.hateoas.forms.spring.Path.on;
import static com.github.hateoas.forms.spring.Path.path;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.hateoas.forms.spring.halforms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import com.github.hateoas.forms.spring.halforms.beans.DummyController;
import com.github.hateoas.forms.spring.halforms.beans.Item;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HalFormsResponseTest {

	public static final Logger LOG = LoggerFactory.getLogger(HalFormsResponseTest.class);

	// private MockMvc mockMvc;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static DummyController dm;

	private static final RelProvider relProvider = new DefaultRelProvider();

	private static final CurieProvider curieProvider = new DefaultCurieProvider("test",
			new UriTemplate("http://localhost:8080/profile/{rel}"));

	private enum SuggestType {
		REMOTE, DIRECT, EMBEDDED;
	}

	private final Map<Integer, SuggestType> suggestProperties = new HashMap<Integer, SuggestType>();

	private final Map<Integer, List<Map<String, Object>>> suggestsValuesList = new HashMap<Integer, List<Map<String, Object>>>();

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class WebConfig extends WebMvcConfigurerAdapter {

		@Bean
		public DummyController dummyController() {
			dm = new DummyController();
			return dm;
		}

	}

	@Autowired
	private WebApplicationContext wac;

	@Before
	public void setUp() {

		loadControlValues();

		webAppContextSetup(wac).build();
		dm.setUp();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new Jackson2HalFormsModule());
		objectMapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, null, true));
	}

	private void loadControlValues() {
		suggestProperties.put(2, SuggestType.DIRECT);
		suggestProperties.put(3, SuggestType.EMBEDDED);
		suggestProperties.put(4, SuggestType.EMBEDDED);
		suggestProperties.put(5, SuggestType.EMBEDDED);
		suggestProperties.put(6, SuggestType.REMOTE);
		suggestProperties.put(7, SuggestType.REMOTE);
		suggestProperties.put(10, SuggestType.EMBEDDED);
		suggestProperties.put(11, SuggestType.DIRECT);
		suggestProperties.put(14, SuggestType.EMBEDDED);
		suggestProperties.put(15, SuggestType.DIRECT);
		suggestProperties.put(18, SuggestType.DIRECT);
		suggestProperties.put(19, SuggestType.DIRECT);
		suggestProperties.put(22, SuggestType.DIRECT);
		suggestProperties.put(23, SuggestType.DIRECT);
		suggestProperties.put(25, SuggestType.DIRECT);
		suggestProperties.put(26, SuggestType.DIRECT);
		suggestProperties.put(30, SuggestType.DIRECT);
		suggestProperties.put(31, SuggestType.DIRECT);
		suggestProperties.put(34, SuggestType.DIRECT);
		suggestProperties.put(35, SuggestType.DIRECT);
		suggestProperties.put(41, SuggestType.DIRECT);
		suggestProperties.put(42, SuggestType.DIRECT);
		suggestProperties.put(45, SuggestType.DIRECT);
		suggestProperties.put(46, SuggestType.DIRECT);
		suggestProperties.put(49, SuggestType.DIRECT);
		suggestProperties.put(50, SuggestType.DIRECT);

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
	public void testRequestWithStatusFound() throws Exception {

		int index = 0;
		for (Item item : dm.items) {
			assertCommonForItem(index, DummyController.DELETE, HttpMethod.DELETE.toString());
			JsonNode jsonNode = assertCommonForItem(index, DummyController.MODIFY, HttpMethod.PUT.toString());
			String jsonEdit = jsonNode.toString();

			assertProperty(jsonEdit, 0, path(on(Item.class).getId()), false, false, Integer.toString(item.getId()));
			assertProperty(jsonEdit, 1, path(on(Item.class).getName()), DummyController.NAME_READONLY.contains(index),
					DummyController.NAME_REQUIRED.contains(index), item.getName());
			assertProperty(jsonEdit, 2, path(on(Item.class).getType()), DummyController.TYPE_READONLY.contains(index),
					DummyController.TYPE_REQUIRED.contains(index), item.getType().toString());
			assertProperty(jsonEdit, 3, path(on(Item.class).getMultiple()), false, false,
					objectMapper.writeValueAsString(item.getMultiple()));
			assertProperty(jsonEdit, 4, path(on(Item.class).getSingleSub()), DummyController.SUBITEM_READONLY.contains(index),
					DummyController.SUBITEM_REQUIRED.contains(index), objectMapper.writeValueAsString(item.getSingleSub()));
			assertProperty(jsonEdit, 5, path(on(Item.class).getSubItemId()), DummyController.SUBITEM_ID_READONLY.contains(index),
					DummyController.SUBITEM_ID_REQUIRED.contains(index), Integer.toString(item.getSubItemId()));
			assertProperty(jsonEdit, 6, path(on(Item.class).getSearchedSubItem()),
					DummyController.SEARCHED_SUBITEM_READONLY.contains(index), DummyController.SEARCHED_SUBITEM_REQUIRED.contains(index),
					String.valueOf(item.getSearchedSubItem()));
			assertProperty(jsonEdit, 7, path(on(Item.class).getAnother()), DummyController.ANOTHER_SUBITEM_READONLY.contains(index),
					DummyController.ANOTHER_SUBITEM_REQUIRED.contains(index), objectMapper.writeValueAsString(item.getAnother()));
			assertProperty(jsonEdit, 9, path(on(Item.class).getSubEntity().getName()),
					DummyController.SUBENTITY_NAME_READONLY.contains(index), DummyController.SUBENTITY_NAME_REQUIRED.contains(index),
					item.getSubEntity().getName());
			assertProperty(jsonEdit, 10, path(on(Item.class).getSubEntity().getMultiple()),
					DummyController.SUBENTITY_MULTIPLE_READONLY.contains(index),
					DummyController.SUBENTITY_MULTIPLE_REQUIRED.contains(index),
					objectMapper.writeValueAsString(item.getSubEntity().getMultiple()));
			assertProperty(jsonEdit, 16, path(on(Item.class).getListSubEntity()) + "[0].lkey",
					DummyController.LIST_SUBENTITY_KEY_READONLY.contains(index),
					DummyController.LIST_SUBENTITY_KEY_REQUIRED.contains(index),
					Integer.toString(item.getListSubEntity().get(0).getLkey()));
			assertProperty(jsonEdit, 24, path(on(Item.class).getAmount()), DummyController.AMOUNT_READONLY.contains(index),
					DummyController.AMOUNT_REQUIRED.contains(index), Double.toString(item.getAmount()));
			assertProperty(jsonEdit, 25, path(on(Item.class).isFlag()), DummyController.FLAG_READONLY.contains(index),
					DummyController.FLAG_REQUIRED.contains(index), String.valueOf(item.isFlag()));
			assertProperty(jsonEdit, 26, path(on(Item.class).getIntegerList()), DummyController.INTEGER_LIST_READONLY.contains(index),
					DummyController.INTEGER_LIST_REQUIRED.contains(index), objectMapper.writeValueAsString(item.getIntegerList()));
			assertProperty(jsonEdit, 32, path(on(Item.class).getDoubleLevelWildCardEntityList()) + "[*].lkey",
					DummyController.LIST_WC_SUBENTITY_KEY_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITY_KEY_REQUIRED.contains(index),
					Integer.toString(item.getDoubleLevelWildCardEntityList().get(0).getLkey()));
			assertProperty(jsonEdit, 33, path(on(Item.class).getDoubleLevelWildCardEntityList()) + "[*].lname",
					DummyController.LIST_WC_SUBENTITY_NAME_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITY_NAME_REQUIRED.contains(index),
					item.getDoubleLevelWildCardEntityList().get(0).getLname());
			assertProperty(jsonEdit, 36, path(on(Item.class).getDoubleLevelWildCardEntityList()) + "[*].subItemList[*].id",
					DummyController.LIST_WC_SUBENTITYLIST_ID_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITYLIST_ID_REQUIRED.contains(index),
					Integer.toString(item.getDoubleLevelWildCardEntityList().get(0).getSubItemList().get(0).getId()));
			assertProperty(jsonEdit, 38, path(on(Item.class).getStringArray()), DummyController.ARRAY_READONLY.contains(index),
					DummyController.ARRAY_REQUIRED.contains(index), objectMapper.writeValueAsString(item.getStringArray()));

			assertSuggest(jsonNode);

			index++;
		}
	}

	private void assertProperty(final String jsonEdit, final int i, final String name, final boolean readOnly, final boolean required,
			final String value) {
		assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].name", equalTo(name)));
		assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].readOnly", equalTo(readOnly)));

		String requiredProperty = "$._templates.default.properties[" + i + "].required";
		if (required) {
			assertThat(jsonEdit, hasJsonPath(requiredProperty, equalTo(required)));
		}
		else {
			try {
				assertThat(jsonEdit, hasJsonPath(requiredProperty, equalTo(required)));

			}
			catch (AssertionError e) {
				assertThat(jsonEdit, hasNoJsonPath(requiredProperty));

			}
		}
		if (value != null) {
			try {
				assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].value", equalTo(value)));
			}
			catch (AssertionError e) {
				e.printStackTrace();
			}
		}
	}

	private void assertSuggest(final JsonNode jsonNode) {
		String jsonEdit = jsonNode.toString();
		JsonNode properties = jsonNode.get("_templates").get("default").get("properties");
		for (int i = 0; i < properties.size(); i++) {
			String suggestPropPath = "$._templates.default.properties[" + i + "].suggest";
			if (suggestProperties.containsKey(i)) {
				assertThat(jsonEdit, hasJsonPath(suggestPropPath));
				String promptField = "name";
				String embeddedName = "test:subItemList";

				if (suggestProperties.get(i).equals(SuggestType.EMBEDDED)) {
					assertThat(jsonEdit, hasJsonPath(suggestPropPath + ".embedded", equalTo(embeddedName)));
					assertThat(jsonEdit, hasJsonPath(suggestPropPath + ".prompt-field", equalTo(promptField)));
					checkSuggestValues(jsonEdit, "$._embedded.test:subItemList", suggestsValuesList.get(i),
							jsonNode.get("_embedded").get(embeddedName).size());
				}
				else if (suggestProperties.get(i).equals(SuggestType.DIRECT)) {
					checkSuggestValues(jsonEdit, suggestPropPath, suggestsValuesList.get(i), properties.get(i).get("suggest").size());
				}
				else {
					assertThat(jsonEdit, hasJsonPath(suggestPropPath + ".href"));
					assertThat(jsonEdit, hasJsonPath(suggestPropPath + ".prompt-field"));
				}
			}
			else {
				assertThat(jsonEdit, hasNoJsonPath(suggestPropPath));
			}
		}
	}

	private void checkSuggestValues(final String jsonEdit, final String halFormSuggestPath, final List<Map<String, Object>> suggestValues,
			final int halFormSuggestSize) {

		assertEquals(halFormSuggestSize, suggestValues.size());
		for (int j = 0; j < suggestValues.size(); j++) {
			for (Entry<String, Object> val : suggestValues.get(j).entrySet()) {
				assertThat(jsonEdit, hasJsonPath(halFormSuggestPath + "[" + j + "]." + val.getKey(), equalTo(val.getValue())));
			}
		}
	}

	private JsonNode assertCommonForItem(final int item, final String rel, final String method) {
		// If @RequestParam annotation is not present the request is correct
		Object entity = HalFormsUtils.toHalFormsDocument(dm.get(item, rel), objectMapper);
		JsonNode jsonNode = objectMapper.valueToTree(entity);
		String json = jsonNode.toString();

		assertThat(json, hasJsonPath("$._links.self"));
		assertThat(json, hasJsonPath("$._templates"));
		/**
		 * By default we are sending three links, get (even that it has no parameters), put and delete, the first one is default as it is
		 * mandatory
		 */
		assertThat(json, hasJsonPath("$._templates.default"));
		assertThat(json, hasJsonPath("$._templates.default.method", equalTo(method)));
		if (method.equals("GET") || method.equals("DELETE")) {
			assertThat(json, hasNoJsonPath("$._templates.delete.properties"));
		}
		return jsonNode;
	}
}
