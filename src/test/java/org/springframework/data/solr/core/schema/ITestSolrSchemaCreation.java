/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.solr.core.schema;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest.AddField;
import org.apache.solr.client.solrj.response.schema.SchemaRepresentation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.mapping.Indexed;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.schema.SolrPersistentEntitySchemaCreator.Feature;
import org.springframework.data.solr.test.util.EmbeddedSolrServer;
import org.springframework.data.solr.test.util.EmbeddedSolrServer.ClientCache;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Christoph Strobl
 */
public class ITestSolrSchemaCreation {

	public @Rule EmbeddedSolrServer resource = EmbeddedSolrServer.configure(new ClassPathResource("managed-schema"),
			ClientCache.ENABLED);

	static final String COLLECTION_NAME = "collection1";

	private SolrTemplate template;

	@Before
	public void setUp() {

		this.template = new SolrTemplate(resource);
		this.template.setSolrCore(COLLECTION_NAME);
		template.setSchemaCreationFeatures(Collections.singletonList(Feature.CREATE_MISSING_FIELDS));
		template.afterPropertiesSet();

		template.delete(new SimpleQuery("*:*"));
		template.commit();
	}

	@Test // DATASOLR-72, DATASOLR-313
	public void beanShouldBeSavedCorrectly() throws SolrServerException, IOException {

		Foo foo = new Foo();
		foo.id = "1";
		template.saveBean(foo);

		Map<String, Map<String, Object>> fields = requestSchemaFields();

		// @Indexed String indexedStringWithoutType;
		assertThat((String) fields.get("indexedStringWithoutType").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("indexed"), is(true));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("stored"), is(true));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("required"), is(false));

		// @Indexed(name = "namedField", type = "string", searchable = false)
		assertThat((String) fields.get("namedField").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("namedField").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("namedField").get("indexed"), is(false));
		assertThat((Boolean) fields.get("namedField").get("stored"), is(true));
		assertThat((Boolean) fields.get("namedField").get("required"), is(false));

		// @Indexed List<String> listField;
		assertThat((String) fields.get("listField").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("listField").get("multiValued"), is(true));
		assertThat((Boolean) fields.get("listField").get("indexed"), is(true));
		assertThat((Boolean) fields.get("listField").get("stored"), is(true));
		assertThat((Boolean) fields.get("listField").get("required"), is(false));

		// @Indexed(type = "tdouble") Double someDoubleValue;
		assertThat((String) fields.get("someDoubleValue").get("type"), is(equalTo("tdouble")));
		assertThat((Boolean) fields.get("someDoubleValue").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("someDoubleValue").get("indexed"), is(true));
		assertThat((Boolean) fields.get("someDoubleValue").get("stored"), is(true));
		assertThat((Boolean) fields.get("someDoubleValue").get("required"), is(false));

		// @Indexed(name = "copySource", type = "string", copyTo = { "_text_" }) String aCopyFiled;
		assertThat((String) fields.get("copySource").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("copySource").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("copySource").get("indexed"), is(true));
		assertThat((Boolean) fields.get("copySource").get("stored"), is(true));
		assertThat((Boolean) fields.get("copySource").get("required"), is(false));

		Map<String, Object> hm = new HashMap<String, Object>();
		hm.put("source", "copySource");
		hm.put("dest", "_text_");

		assertThat(requestCopyFields(), hasItem(hm));
	}

	@Test // DATASOLR-313
	public void existingFieldsShouldNotBeTouched() throws SolrServerException, IOException, InterruptedException {

		Map<String, Object> field = new LinkedHashMap<String, Object>();
		field.put("name", "indexedStringWithoutType");
		field.put("type", "string");
		field.put("multiValued", true);
		field.put("indexed", false);
		field.put("stored", true);

		new AddField(field).process(resource.getSolrClient(COLLECTION_NAME), COLLECTION_NAME);

		Foo foo = new Foo();
		foo.id = "1";
		template.saveBean(foo);

		Map<String, Map<String, Object>> fields = requestSchemaFields();

		// @Indexed String indexedStringWithoutType;
		assertThat((String) fields.get("indexedStringWithoutType").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("multiValued"), is(true));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("indexed"), is(false));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("stored"), is(true));
		assertThat((Boolean) fields.get("indexedStringWithoutType").get("required"), is(nullValue()));

		// @Indexed(name = "namedField", type = "string", searchable = false)
		assertThat((String) fields.get("namedField").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("namedField").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("namedField").get("indexed"), is(false));
		assertThat((Boolean) fields.get("namedField").get("stored"), is(true));
		assertThat((Boolean) fields.get("namedField").get("required"), is(false));

		// @Indexed List<String> listField;
		assertThat((String) fields.get("listField").get("type"), is(equalTo("string")));
		assertThat((Boolean) fields.get("listField").get("multiValued"), is(true));
		assertThat((Boolean) fields.get("listField").get("indexed"), is(true));
		assertThat((Boolean) fields.get("listField").get("stored"), is(true));
		assertThat((Boolean) fields.get("listField").get("required"), is(false));

		// @Indexed(type = "tdouble") Double someDoubleValue;
		assertThat((String) fields.get("someDoubleValue").get("type"), is(equalTo("tdouble")));
		assertThat((Boolean) fields.get("someDoubleValue").get("multiValued"), is(false));
		assertThat((Boolean) fields.get("someDoubleValue").get("indexed"), is(true));
		assertThat((Boolean) fields.get("someDoubleValue").get("stored"), is(true));
		assertThat((Boolean) fields.get("someDoubleValue").get("required"), is(false));
	}

	private Map<String, Map<String, Object>> requestSchemaFields() throws SolrServerException, IOException {

		SchemaRepresentation schema = new SchemaRequest().process(resource.getSolrClient(COLLECTION_NAME), COLLECTION_NAME)
				.getSchemaRepresentation();

		Map<String, Map<String, Object>> fields = new LinkedHashMap<String, Map<String, Object>>();

		for (Map<String, Object> field : schema.getFields()) {
			fields.put(field.get("name").toString(), field);
		}

		return fields;
	}

	private List<Map<String, Object>> requestCopyFields() throws SolrServerException, IOException {

		SchemaRepresentation schema = new SchemaRequest().process(resource.getSolrClient(COLLECTION_NAME), COLLECTION_NAME)
				.getSchemaRepresentation();

		return schema.getCopyFields();
	}

	@SolrDocument(solrCoreName = COLLECTION_NAME)
	public static class Foo {

		@Indexed @Id String id;

		@Indexed String indexedStringWithoutType;

		@Indexed(name = "namedField", type = "string", searchable = false) String justAStoredField;

		@Indexed List<String> listField;

		@Indexed(type = "tdouble") Double someDoubleValue;

		@Indexed(name = "copySource", type = "string", copyTo = { "_text_" }) String aCopyFiled;
	}

}
