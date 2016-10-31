package com.github.hateoas.forms.spring.xhtml;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.xml.sax.SAXException;

import com.github.hateoas.forms.spring.xhtml.beans.DummyController;
import com.github.hateoas.forms.spring.xhtml.beans.Item;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class XHTMLResponseTest {

	public static final Logger LOG = LoggerFactory.getLogger(XHTMLResponseTest.class);

	private static DummyController dm;

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

		webAppContextSetup(wac).build();
		dm.setUp();
	}

	@Test
	public void testRequestWithStatusFound() throws Exception {

		int index = 0;
		for (Item item : dm.items) {
			String htmlStr = writeXml(index, DummyController.MODIFY, HttpMethod.PUT.toString());

			assertInputProperty(htmlStr, "number", "id", false, false, Integer.toString(item.getId()), false);
			assertInputProperty(htmlStr, "text", "name", DummyController.NAME_READONLY.contains(index),
					DummyController.NAME_REQUIRED.contains(index), item.getName(), false);
			assertInputProperty(htmlStr, "select", "type", DummyController.TYPE_READONLY.contains(index),
					DummyController.TYPE_REQUIRED.contains(index), item.getType().toString(), false);
			assertInputProperty(htmlStr, "select", "multiple", false, false, item.getMultiple().toArray(), false);
			assertInputProperty(htmlStr, "select", "singleSub", DummyController.SUBITEM_READONLY.contains(index),
					DummyController.SUBITEM_REQUIRED.contains(index), item.getSingleSub(), false);
			assertInputProperty(htmlStr, "select", "subItemId", DummyController.SUBITEM_ID_READONLY.contains(index),
					DummyController.SUBITEM_ID_REQUIRED.contains(index), Integer.toString(item.getSubItemId()), false);
			// REMOTE
			assertInputProperty(htmlStr, "select", "searchedSubItem", DummyController.SEARCHED_SUBITEM_READONLY.contains(index),
					DummyController.SEARCHED_SUBITEM_REQUIRED.contains(index), item.getSearchedSubItem(), false);
			XMLAssert.assertXpathExists("//select[@name='searchedSubItem'][@data-remote='http://localhost/test/subitem/filter{?filter}']",
					htmlStr);
			// REMOTE
			assertInputProperty(htmlStr, "select", "another", DummyController.ANOTHER_SUBITEM_READONLY.contains(index),
					DummyController.ANOTHER_SUBITEM_REQUIRED.contains(index), item.getAnother(), false);
			XMLAssert.assertXpathExists("//select[@name='another'][@data-remote='http://localhost/test/subitem/anotherFilter/{filter}/']",
					htmlStr);
			assertInputProperty(htmlStr, "text", "subEntity.name", DummyController.SUBENTITY_NAME_READONLY.contains(index),
					DummyController.SUBENTITY_NAME_REQUIRED.contains(index), item.getSubEntity().getName(), false);
			assertInputProperty(htmlStr, "select", "subEntity.multiple", DummyController.SUBENTITY_MULTIPLE_READONLY.contains(index),
					DummyController.SUBENTITY_MULTIPLE_REQUIRED.contains(index), item.getSubEntity().getMultiple().toArray(), false);
			assertInputProperty(htmlStr, "number", "listSubEntity[0].lkey", DummyController.LIST_SUBENTITY_KEY_READONLY.contains(index),
					DummyController.LIST_SUBENTITY_KEY_REQUIRED.contains(index), Integer.toString(item.getListSubEntity().get(0).getLkey()),
					false);

			assertInputProperty(htmlStr, "select", "listSubEntity[0].multiple",
					DummyController.LIST_SUBENTITY_MULTIPLE_READONLY.contains(index),
					DummyController.LIST_SUBENTITY_MULTIPLE_REQUIRED.contains(index),
					item.getListSubEntity().get(0).getMultiple().toArray(), false);

			assertInputProperty(htmlStr, "number", "amount", DummyController.AMOUNT_READONLY.contains(index),
					DummyController.AMOUNT_REQUIRED.contains(index), item.getAmount(), false);
			assertInputProperty(htmlStr, "select", "flag", DummyController.FLAG_READONLY.contains(index),
					DummyController.FLAG_REQUIRED.contains(index), item.isFlag(), false);
			assertInputProperty(htmlStr, "select", "integerList", DummyController.INTEGER_LIST_READONLY.contains(index),
					DummyController.INTEGER_LIST_REQUIRED.contains(index), item.getIntegerList().toArray(), false);
			assertInputProperty(htmlStr, "number", "doubleLevelWildCardEntityList[*].lkey",
					DummyController.LIST_WC_SUBENTITY_KEY_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITY_KEY_REQUIRED.contains(index),
					item.getDoubleLevelWildCardEntityList().get(0).getLkey(), false);
			assertInputProperty(htmlStr, "text", "doubleLevelWildCardEntityList[*].lname",
					DummyController.LIST_WC_SUBENTITY_NAME_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITY_NAME_REQUIRED.contains(index),
					item.getDoubleLevelWildCardEntityList().get(0).getLname(), false);
			assertInputProperty(htmlStr, "number", "doubleLevelWildCardEntityList[*].subItemList[*].id",
					DummyController.LIST_WC_SUBENTITYLIST_ID_READONLY.contains(index),
					DummyController.LIST_WC_SUBENTITYLIST_ID_REQUIRED.contains(index),
					item.getDoubleLevelWildCardEntityList().get(0).getSubItemList().get(0).getId(), false);
			assertInputProperty(htmlStr, "text", "stringArray", DummyController.ARRAY_READONLY.contains(index),
					DummyController.ARRAY_REQUIRED.contains(index), item.getStringArray(), false);

			index++;
		}
	}

	private void assertInputProperty(final String htmlStr, final String htmlType, final String name, final boolean readOnly,
			final boolean required, final Object valueObj, final boolean isHidden) throws XpathException, IOException, SAXException {

		String value;
		Object[] values;
		if (valueObj instanceof Object[]) {
			values = (Object[]) valueObj;
		}
		else {
			values = new Object[] { valueObj };
		}

		for (int i = 0; i < values.length; i++) {
			value = values[i].toString();
			if (isHidden) {
				XMLAssert.assertXpathExists("//input[@type='hidden'][@name='" + name + "'][@value='" + value + "']", htmlStr);
			}
			else {
				XMLAssert.assertXpathExists("//label[@for='" + name + "']", htmlStr);
				if (!"select".equals(htmlType)) {
					XMLAssert.assertXpathExists("//input[@type='" + htmlType + "'][@name='" + name + "'][@value='" + value + "']", htmlStr);
					if (required) {
						XMLAssert.assertXpathExists("//input[@type='" + htmlType + "'][@name='" + name + "'][@value='" + value
								+ "'][@required='" + String.valueOf(required) + "']", htmlStr);
					}
					else {
						XMLAssert.assertXpathNotExists("//input[@type='" + htmlType + "'][@name='" + name + "'][@value='" + value
								+ "'][@required='" + String.valueOf(!required) + "']", htmlStr);
					}
					if (readOnly) {
						XMLAssert.assertXpathExists("//input[@type='" + htmlType + "'][@name='" + name + "'][@value='" + value
								+ "'][@editable='" + String.valueOf(!readOnly) + "']", htmlStr);
					}
					else {
						XMLAssert.assertXpathNotExists("//input[@type='" + htmlType + "'][@name='" + name + "'][@value='" + value
								+ "'][@editable='" + String.valueOf(readOnly) + "']", htmlStr);
					}
				}
				else {
					if (value != null && !"".equals(value)) {
						XMLAssert.assertXpathExists("//select[@name='" + name + "']/option[@selected][@value='" + value + "']", htmlStr);
						if (readOnly) {
							XMLAssert.assertXpathExists("//input[@type='hidden'][@name='" + name + "'][@value='" + value + "']", htmlStr);
						}
					}
				}
			}
		}
	}

	private String writeXml(final int item, final String rel, final String method) throws IOException {
		String html = "";
		Writer writer = null;
		XhtmlWriter xhtml = null;
		try {
			writer = new StringWriter();
			xhtml = new XhtmlWriter(writer);
			xhtml.writeLinks(Arrays.asList(dm.get(item, rel).getLink(rel)));
			html = writer.toString();
		}
		catch (IOException e) {
			LOG.error("Error writing xhtml.", e);
		}
		finally {
			if (xhtml != null) {
				xhtml.flush();
				xhtml.close();
			}
			if (writer != null) {
				writer.flush();
				writer.close();
			}

		}
		return html;
	}
}
