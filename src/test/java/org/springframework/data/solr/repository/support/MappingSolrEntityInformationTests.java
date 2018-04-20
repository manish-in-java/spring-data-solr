/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.solr.repository.support;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.solr.core.mapping.SimpleSolrPersistentProperty;
import org.springframework.data.solr.core.mapping.SolrPersistentEntity;
import org.springframework.data.solr.repository.ProductBean;
import org.springframework.data.solr.repository.query.SolrEntityInformation;
import org.springframework.data.util.ClassTypeInformation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Strobl
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class MappingSolrEntityInformationTests {

	private static final String PRODUCT_BEAN_SIMPLE_NAME = "productbean";

	@Mock
	private SolrPersistentEntity<ProductBean> persistentEntity;

	@Mock
	private SolrPersistentEntity<ProductBeanWithAlternateFieldNameForId> persistentEntityWithAlternateFieldNameForId;

	@Mock
	private SolrPersistentEntity<ProductBeanWithLongIdFieldType> persistentEntityWithLongIdFieldType;

	@Before
	public void setUp() {
		when(persistentEntity.getType()).thenReturn(ProductBean.class);
		when(persistentEntity.getSolrCoreName()).thenReturn(PRODUCT_BEAN_SIMPLE_NAME);

		when(persistentEntityWithAlternateFieldNameForId.getType()).thenReturn(
				ProductBeanWithAlternateFieldNameForId.class);
		when(persistentEntityWithAlternateFieldNameForId.getSolrCoreName()).thenReturn(PRODUCT_BEAN_SIMPLE_NAME);

		when(persistentEntityWithLongIdFieldType.getType()).thenReturn(ProductBeanWithLongIdFieldType.class);
		when(persistentEntityWithLongIdFieldType.getSolrCoreName()).thenReturn(PRODUCT_BEAN_SIMPLE_NAME);
	}

	@Test
	public void testSolrCoreRetrievalWhenNotExplicitlySet() {
		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity);
		assertEquals(PRODUCT_BEAN_SIMPLE_NAME, entityInformation.getCollectionName());
	}

	@Test
	public void testSolrCoreRetrievalWhenSet() {
		final String coreName = "core1";
		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity, coreName);
		assertEquals(coreName, entityInformation.getCollectionName());
	}

	@Test
	public void testIdType() throws NoSuchFieldException, SecurityException {
		when(persistentEntity.getTypeInformation()).thenReturn(ClassTypeInformation.from(ProductBean.class));

		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(
				Property.of(ClassTypeInformation.from(ProductBean.class), ProductBean.class.getDeclaredField("id")),
				persistentEntity, SimpleTypeHolder.DEFAULT);
		when(persistentEntity.getRequiredIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<>(persistentEntity);
		assertEquals(String.class, entityInformation.getIdType());
	}

	@Test
	public void testIdTypeWithLongIdFieldType() throws NoSuchFieldException, SecurityException {
		when(persistentEntityWithLongIdFieldType.getTypeInformation())
				.thenReturn(ClassTypeInformation.from(ProductBeanWithLongIdFieldType.class));

		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(
				Property.of(ClassTypeInformation.from(ProductBeanWithLongIdFieldType.class),
						ProductBeanWithLongIdFieldType.class.getDeclaredField("id")),
				persistentEntityWithLongIdFieldType, SimpleTypeHolder.DEFAULT);
		when(persistentEntityWithLongIdFieldType.getRequiredIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBeanWithLongIdFieldType, Long> entityInformation = new MappingSolrEntityInformation<>(
				persistentEntityWithLongIdFieldType);
		assertEquals(Long.class, entityInformation.getIdType());
	}

	@Test
	public void testGetIdAttribute() throws NoSuchFieldException, SecurityException {
		when(persistentEntity.getTypeInformation()).thenReturn(ClassTypeInformation.from(ProductBean.class));
		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(
				Property.of(ClassTypeInformation.from(ProductBean.class), ProductBean.class.getDeclaredField("id")),
				persistentEntity, SimpleTypeHolder.DEFAULT);
		when(persistentEntity.getIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBean, String> entityInformation = new MappingSolrEntityInformation<ProductBean, String>(
				persistentEntity);
		assertEquals("id", entityInformation.getIdAttribute());
	}

	@Test
	public void testGetIdAttributeForAlternateFieldName() throws NoSuchFieldException, SecurityException {
		when(persistentEntityWithAlternateFieldNameForId.getTypeInformation()).thenReturn(
				ClassTypeInformation.from(ProductBeanWithAlternateFieldNameForId.class));

		SimpleSolrPersistentProperty property = new SimpleSolrPersistentProperty(
				Property.of(ClassTypeInformation.from(ProductBeanWithAlternateFieldNameForId.class),
						ProductBeanWithAlternateFieldNameForId.class.getDeclaredField("productId")),
				persistentEntityWithAlternateFieldNameForId, SimpleTypeHolder.DEFAULT);
		when(persistentEntityWithAlternateFieldNameForId.getIdProperty()).thenReturn(property);

		SolrEntityInformation<ProductBeanWithAlternateFieldNameForId, String> entityInformation = new MappingSolrEntityInformation<ProductBeanWithAlternateFieldNameForId, String>(
				persistentEntityWithAlternateFieldNameForId);
		assertEquals("product_id", entityInformation.getIdAttribute());
	}

	class ProductBeanWithAlternateFieldNameForId {

		@Id
		@Field("product_id")
		private String productId;

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

	}

	class ProductBeanWithLongIdFieldType {

		@Id
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

	}
}
