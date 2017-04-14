package org.springframework.data.solr.repository.autowiring;

import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.domain.Brand;

import java.util.List;

/**
 * Contract for search operations on {@link Brand}s.
 */
public interface BrandRepository extends SolrCrudRepository<Brand, String>
{
  /**
   * Finds all brands with description containing specified keywords.
   *
   * @param description The keywords to search in the description.
   * @return A {@link List} of {@link Brand}s.
   */
  List<Brand> findAllByDescription(String description);
}
