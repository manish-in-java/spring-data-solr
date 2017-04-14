/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.data.solr.server.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Creates a {@link SolrClient} based on the composition of
 * {@code <solr:client />} XML element.
 */
public class SolrClientFactoryBean
    extends SolrClientFactoryBase
    implements FactoryBean<SolrClient>
    , InitializingBean
    , DisposableBean
{
  private static final String HTTP_PATH_PREFIX  = "http://";
  private static final String HTTPS_PATH_PREFIX = "https://";
  private static final String PATH_SEPARATOR    = ",";

  private String core;
  private AtomicReference<CoreContainer>                embeddedContainer = new AtomicReference<>(null);
  private ConcurrentHashMap<String, EmbeddedSolrServer> embeddedServers   = new ConcurrentHashMap<>();
  private String  path;
  private Integer timeout;
  private Integer maxConnections;

  /**
   * {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.hasText(core, "Solr core name must not be null or empty!");
    Assert.hasText(path, "Solr path must not be null or empty!");

    createSolrClient();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy()
  {
    super.destroy();

    if (isEmbedded())
    {
      embeddedContainer.get().shutdown();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getCores()
  {
    return this.core != null ? Arrays.asList(this.core) : Collections.emptyList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SolrClient getObject() throws Exception
  {
    return getSolrClient();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?> getObjectType()
  {
    if (getSolrClient() == null)
    {
      return SolrClient.class;
    }

    return getSolrClient().getClass();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SolrClient getSolrClient()
  {
    return isEmbedded()
           ? getSolrClient(core)
           : super.getSolrClient();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SolrClient getSolrClient(final String core)
  {
    if (isEmbedded())
    {
      if (embeddedServers.containsKey(core != null ? core : ""))
      {
        return embeddedServers.get(core);
      }

      final EmbeddedSolrServer server = new EmbeddedSolrServer(embeddedContainer.get(), core != null ? core : "");
      embeddedServers.put(core != null ? core : "", server);

      return server;
    }

    return getSolrClient();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSingleton()
  {
    return true;
  }

  /**
   * Sets the name of the Solr core to connect to.
   *
   * @param core The name of the Solr core to connect to.
   */
  public void setCore(final String core)
  {
    this.core = core;
  }

  /**
   * Sets the maximum number of concurrent connections that the Solr server
   * can handle.
   *
   * @param maxConnections The maximum number of concurrent connections that
   *                       the Solr server can handle.
   */
  public void setMaxConnections(final Integer maxConnections)
  {
    this.maxConnections = maxConnections;
  }

  /**
   * Sets the path(s) to the Solr server(s) to connect to.
   *
   * @param path The path(s) to the Solr server(s) to connect to. These can
   *             be multiple paths separated by commas.
   */
  public void setPath(final String path)
  {
    this.path = path;
  }

  /**
   * Sets the operation timeout.
   *
   * @param timeout The operation timeout.
   */
  public void setTimeout(final Integer timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Creates a Solr {@link CoreContainer} that can be used to run an embedded
   * Solr server.
   *
   * @param path The path to a local Solr installation to use for running the
   *             embedded server.
   * @return A {@link CoreContainer}.
   * @throws FileNotFoundException if the specified installation path
   *                               does not exist.
   */
  private CoreContainer createEmbeddedContainer(final String path) throws FileNotFoundException
  {
    final String solrDirectory = ResourceUtils.getFile(path).getPath();

    final File solrConfigurationFile = new File(solrDirectory + "/solr.xml");

    return CoreContainer.createAndLoad(ResourceUtils.getFile(path).toPath(), solrConfigurationFile.toPath());
  }

  /**
   * Creates an {@link EmbeddedSolrServer}.
   */
  private void createEmbeddedSolrServer()
  {
    try
    {
      embeddedContainer.compareAndSet(null, createEmbeddedContainer(path));

      setSolrClient(new EmbeddedSolrServer(embeddedContainer.get(), core));
    }
    catch (final Exception e)
    {
      throw new IllegalArgumentException("Unable to create embedded Solr server with path [" + path + "].", e);
    }
  }

  /**
   * Creates an {@link HttpSolrClient}.
   */
  private void createHttpSolrClient()
  {
    final HttpSolrClient solrClient = new HttpSolrClient(path);

    if (timeout != null)
    {
      solrClient.setConnectionTimeout(timeout);
    }
    if (maxConnections != null)
    {
      solrClient.setMaxTotalConnections(maxConnections);
    }

    setSolrClient(solrClient);
  }

  /**
   * Creates a {@link LBHttpSolrClient}.
   */
  private void createLoadBalancedHttpSolrClient()
  {
    try
    {
      final LBHttpSolrClient solrClient = new LBHttpSolrClient(StringUtils.split(path, PATH_SEPARATOR));

      if (timeout != null)
      {
        solrClient.setConnectionTimeout(timeout);
      }

      setSolrClient(solrClient);
    }
    catch (final MalformedURLException e)
    {
      throw new IllegalArgumentException("Unable to create load-balanced Solr server with paths [" + path + "].", e);
    }
  }

  /**
   * Creates a {@link SolrClient} based on the Solr server path specified.
   * If the server path contains multiple values separated by commas, a
   * {@link LBHttpSolrClient} is created. If not and the server path starts
   * with {@code http://} or {@code https://}, an {@link HttpSolrClient} is
   * created. Otherwise, an {@link EmbeddedSolrServer} is created.
   */
  private void createSolrClient()
  {
    if (path.contains(PATH_SEPARATOR))
    {
      createLoadBalancedHttpSolrClient();
    }
    else if (path.toLowerCase().startsWith(HTTP_PATH_PREFIX)
        || path.toLowerCase().startsWith(HTTPS_PATH_PREFIX))
    {
      createHttpSolrClient();
    }
    else
    {
      createEmbeddedSolrServer();
    }
  }

  /**
   * Gets whether the configuration implies an embedded Solr server.
   *
   * @return {@code true} if the configuration implies an embedded Solr server,
   * {@code false} otherwise.
   */
  private boolean isEmbedded()
  {
    return embeddedContainer.get() != null;
  }
}
