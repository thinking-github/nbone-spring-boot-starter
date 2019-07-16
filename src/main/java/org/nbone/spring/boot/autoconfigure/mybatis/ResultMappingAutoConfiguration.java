package org.nbone.spring.boot.autoconfigure.mybatis;

import org.nbone.framework.spring.annotation.ClassPathInterfaceScanner;
import org.nbone.persistence.annotation.ResultMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 *  ImportBeanDefinitionRegistrar 实现类需要配合 @Import使用 方能生效
 * @author thinking
 * @version 1.0
 * @since 2019/7/5
 */
//@Import(ResultMappingAutoConfiguration.ResultMappingScannerRegistrar.class)
@Configuration
public class ResultMappingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ResultMappingAutoConfiguration.class);


    public static class ResultMappingScannerRegistrar
            implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.");
                return;
            }

            logger.debug("Searching for mappers annotated with @Mapper");

            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            if (logger.isDebugEnabled()) {
                for (String pkg : packages) {
                    logger.debug("Using auto-configuration base package '{}'", pkg);
                }
            }

            ClassPathInterfaceScanner scanner = new ClassPathInterfaceScanner(registry);
            if (this.resourceLoader != null) {
                scanner.setResourceLoader(this.resourceLoader);
            }
            scanner.setAnnotationClass(ResultMapping.class);
            scanner.registerFilters();
            scanner.doScan(StringUtils.toStringArray(packages));

        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

}
