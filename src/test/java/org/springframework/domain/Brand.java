package org.springframework.domain;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * Represents a brand of products.
 */
@SolrDocument(solrCoreName = "collection1")
public class Brand
{
  @Field("description")
  private String description;
  @Field("id")
  @Id
  private String id;
  @Field("name")
  private String name;

  /**
   * Default constructor.
   */
  public Brand()
  {
  }

  /**
   * Initializes all the fields.
   *
   * @param id          The unique brand identifier.
   * @param name        The brand name.
   * @param description The brand description.
   */
  public Brand(final String id, final String name, final String description)
  {
    this.description = description;
    this.id = id;
    this.name = name;
  }

  /**
   * Gets the brand description.
   *
   * @return The brand description.
   */
  public final String getDescription()
  {
    return this.description;
  }

  /**
   * Gets the unique brand identifier.
   *
   * @return The unique brand identifier.
   */
  public final String getId()
  {
    return this.id;
  }

  /**
   * Gets the brand name.
   *
   * @return The brand name.
   */
  public final String getName()
  {
    return this.name;
  }

  /**
   * Sets the brand description.
   *
   * @param description The brand description.
   */
  public final void setDescription(final String description)
  {
    this.description = description;
  }

  /**
   * Sets the unique brand identifier.
   *
   * @param id The unique brand identifier.
   */
  public final void setId(final String id)
  {
    this.id = id;
  }

  /**
   * Sets the brand name.
   *
   * @param name The brand name.
   */
  public final void setName(final String name)
  {
    this.name = name;
  }
}
