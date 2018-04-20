package org.springframework.data.solr.repository.autowiring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.domain.Brand;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BrandRepository}.
 */
@ContextConfiguration(locations = "classpath:springTestContext.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BrandRepositoryTest
{
  @Autowired
  private BrandRepository repository;

  private Brand entry;

  /**
   * Clears the index after each test.
   */
  @After
  public void cleanup()
  {
    repository.deleteAll();
  }

  /**
   * Adds an index entry for each test to run.
   */
  @Before
  public void setup()
  {
    repository.save(entry = new Brand(UUID.randomUUID().toString(), "Brand", "Lorem ipsum dolor sit amet."));
  }

  /**
   * Tests that repositories can be autowired correctly.
   */
  @Test
  public void testAutowiring()
  {
    assertNotNull(repository);
  }

  /**
   * Tests that objects indexed previously can be loaded using the repository
   * interfaces.
   */
  @Test
  public void testFind()
  {
    final Brand brand = repository.findById(entry.getId()).get();

    assertNotNull(brand);
    assertNotNull(brand.getDescription());
    assertEquals(entry.getDescription(), brand.getDescription());
    assertNotNull(brand.getId());
    assertEquals(entry.getId(), brand.getId());
    assertNotNull(brand.getName());
    assertEquals(entry.getName(), brand.getName());
  }

  /**
   * Tests that all indexed records can be retrieved.
   */
  @Test
  public void testFindAll()
  {
    final Iterable<Brand> brands = repository.findAll();

    assertNotNull(brands);
    assertNotNull(brands.iterator());
    assertTrue(brands.iterator().hasNext());
  }

  /**
   * Tests that indexed records can be searched.
   */
  @Test
  public void testFindAllByDescription()
  {
    final List<Brand> brands = repository.findAllByDescription(entry.getDescription());

    assertNotNull(brands);
    assertFalse(brands.isEmpty());
    brands.forEach(brand ->
                   {
                     assertNotNull(brand);
                     assertNotNull(brand.getDescription());
                     assertNotNull(brand.getId());
                     assertNotNull(brand.getName());
                   });
  }
}
