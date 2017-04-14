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
package org.springframework.data.solr.server.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.data.solr.server.support.SolrClientFactoryBean;
import org.w3c.dom.Element;

/**
 * Parses
 * {@code <solr:client core="[core-name]"
 *                     path="[path-to-Solr-home-directory]"
 *                     timeout="[timeout-value]"
 *                     max-connections="[maximum-concurrent-connections]" />}
 * XML configuration elements.
 */
public class SolrClientBeanDefinitionParser extends AbstractBeanDefinitionParser
{
  /*
   * (non-Javadoc)
   * Parses
   * {@code <solr:client core="[core-name]"
   *                     path="[path-to-Solr-home-directory]"
   *                     timeout="[timeout-value]"
   *                     max-connections="[maximum-concurrent-connections]" />}
   * to create a {@code SolrClient}.
   */
  @Override
  protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext)
  {
    final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(SolrClientFactoryBean.class);
    setProperties(element, builder);

    return getSourcedBeanDefinition(builder, element, parserContext);
  }

  /*
   * (non-Javadoc)
   * Extracts configuration properties required to build a {@code SolrClient}.
   */
  private void setProperties(final Element element, final BeanDefinitionBuilder builder)
  {
    builder.addPropertyValue("core", element.getAttribute("core"));
    builder.addPropertyValue("path", element.getAttribute("path"));
    builder.addPropertyValue("maxConnections", element.getAttribute("max-connections"));
    builder.addPropertyValue("timeout", element.getAttribute("timeout"));
  }

  /*
   * (non-Javadoc)
   * Creates a {@code SolrClient} from the configuration information.
   */
  private AbstractBeanDefinition getSourcedBeanDefinition(final BeanDefinitionBuilder builder
      , final Element source
      , final ParserContext context)
  {
    final AbstractBeanDefinition definition = builder.getBeanDefinition();
    definition.setSource(context.extractSource(source));

    return definition;
  }
}
