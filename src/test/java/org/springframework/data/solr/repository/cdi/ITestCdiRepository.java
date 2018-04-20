/*
 * Copyright 2012-2017 the original author or authors.
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
package org.springframework.data.solr.repository.cdi;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.solr.repository.ProductBean;

/**
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public class ITestCdiRepository {

	private static SeContainer cdiContainer;
	private CdiProductRepository repository;
	private SamplePersonRepository samplePersonRepository;

	@BeforeClass
	public static void init() {

		cdiContainer = SeContainerInitializer.newInstance() //
				.disableDiscovery() //
				.addPackages(CdiRepositoryClient.class) //
				.initialize();
	}

	@AfterClass
	public static void shutdown() {
		cdiContainer.close();
	}

	@Before
	public void setUp() {
		CdiRepositoryClient client = cdiContainer.select(CdiRepositoryClient.class).get();
		repository = client.getRepository();
		samplePersonRepository = client.getSamplePersonRepository();
	}

	@Test
	public void testCdiRepository() {
		Assert.assertNotNull(repository);

		ProductBean bean = new ProductBean();
		bean.setId("id-1");
		bean.setName("cidContainerTest-1");

		repository.save(bean);

		Assert.assertTrue(repository.existsById(bean.getId()));

		ProductBean retrieved = repository.findOne(bean.getId());
		Assert.assertNotNull(retrieved);
		Assert.assertEquals(bean.getId(), retrieved.getId());
		Assert.assertEquals(bean.getName(), retrieved.getName());

		Assert.assertEquals(1, repository.count());

		Assert.assertTrue(repository.existsById(bean.getId()));

		repository.delete(bean);

		Assert.assertEquals(0, repository.count());
		retrieved = repository.findOne(bean.getId());
		Assert.assertNull(retrieved);
	}

	@Test // DATASOLR-187
	public void returnOneFromCustomImpl() {

		assertThat(samplePersonRepository.returnOne(), is(1));
	}

}
