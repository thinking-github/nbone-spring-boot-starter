package org.nbone.spring.boot.apollo;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * ApolloOpenApiClient
 * /apps/camera-material/envs/DEV/clusters/application/namespaces/application/item
 * /apps/camera-material/envs/DEV/clusters/application/namespaces/application/releases
 *
 * @author thinking
 * @version 1.0
 * @since 6/24/21
 */
public class OpenApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiClient.class);

    ApolloOpenApiClient client;

    @Autowired(required = false)
    private Environment environment;

    //System.getProperty("app.id")
    @Value("${app.id:${spring.application.name:}}")
    private String appId;

    //System.getProperty("env") SPRING_PROFILES_ACTIVE
    @Value("${env:${spring.profiles.active:}}")
    private String env;

    //System.getProperty("apollo.cluster")
    @Value("${apollo.open-api.cluster:${apollo.cluster:default}}")
    private String cluster;

    //apollo.portal.address
    @Value("${apollo.portal.address:${apollo.portal:}}")
    private String portalUrl;

    //apollo.accesskey.secret
    @Value("${apollo.open-api.token:${apollo.token:}}")
    private String token;

    @Value("${apollo.open-api.author:${apollo.author:}}")
    private String author;

    @Value("${apollo.open-api.releasedBy:${apollo.releasedBy:}}")
    private String releasedBy;


    private boolean initialize = false;


    public OpenApiClient(String portal, String token) {
        this.client = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portal)
                .withToken(token)
                .build();
        initialize = true;
    }

    public OpenApiClient() {
        //PostConstruct --> initialize();
    }

    @PostConstruct
    public void initialize() {
        if (!initialize) {
            this.client = ApolloOpenApiClient.newBuilder()
                    .withPortalUrl(portalUrl)
                    .withToken(token)
                    .build();
        }

        logger.info("----> " + appId + "+" + env + "+" + cluster + ": " + portalUrl);
    }

    public ApolloOpenApiClient getClient() {
        return client;
    }

    public boolean pushValue(String key, String value, String comment) {
        return pushValue(appId, env, cluster, ConfigConsts.NAMESPACE_APPLICATION, key, value, comment, author);
    }

    public boolean pushValue(String namespace, String key, String value, String comment) {
        return pushValue(appId, env, cluster, namespace, key, value, comment, author);
    }

    public boolean pushValue(String namespace, String key, String value, String comment, String author) {
        return pushValue(appId, env, cluster, namespace, key, value, comment, author);
    }

    public boolean pushValue(String appId, String env, String cluster, String namespace,
                             String key, String value, String comment, String author) {
        boolean flag = false;
        try {
            OpenItemDTO openItemDTO = new OpenItemDTO();
            openItemDTO.setKey(key);
            openItemDTO.setValue(value);
            openItemDTO.setComment(comment);
            openItemDTO.setDataChangeCreatedBy(author);
            client.createOrUpdateItem(appId, env, cluster, namespace, openItemDTO);
            NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
            if(StringUtils.isEmpty(releasedBy)){
                releasedBy = author;
            }
            namespaceReleaseDTO.setReleasedBy(releasedBy);
            namespaceReleaseDTO.setReleaseTitle("release:" + comment);
            client.publishNamespace(appId, env, cluster, namespace, namespaceReleaseDTO);
            flag = true;
        } catch (Exception e) {
            logger.error("publish single item Exception", e);
        }
        return flag;
    }


    public boolean updateValue(String namespace, String key, String value, String comment) {
        return updateValue(appId, env, cluster, namespace, key, value, comment, author);
    }

    public boolean updateValue(String appId, String env, String cluster,
                               String namespace, String key, String value, String comment, String author) {
        boolean flag = false;
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(key);
        openItemDTO.setValue(value);
        openItemDTO.setComment(comment);
        openItemDTO.setDataChangeCreatedBy(author);
        try {
            client.createOrUpdateItem(appId, env, cluster, namespace, openItemDTO);
            flag = true;
        } catch (Exception e) {
            logger.error("updateValue Exception;", e);
        }
        return flag;
    }

    public OpenItemDTO getValue(String namespace, String key) {
        return getValue(appId, env, cluster, namespace, key);
    }

    public OpenItemDTO getValue(String appId, String env, String cluster, String namespace, String key) {
        OpenItemDTO itemDTO = null;
        try {
            itemDTO = client.getItem(appId, env, cluster, namespace, key);
        } catch (Exception e) {
            logger.error("getValue is Exception ;", e);
        }
        return itemDTO;
    }


    public boolean deleteValue(String namespace, String key, String token, String author) {
        return deleteValue(appId, env, cluster, namespace, key, author);
    }

    public boolean deleteValue(String appId, String env, String cluster,
                               String namespace, String key, String author) {
        boolean flag = false;
        try {
            client.removeItem(appId, env, cluster, namespace, key, author);
            flag = true;
        } catch (Exception e) {
            logger.error("deleteValue is Exception ;", e);
        }
        return flag;
    }


    public boolean releaseValue(String namespace, String comment, String author) {
        return releaseValue(appId, env, cluster, namespace, comment, author);
    }

    public boolean releaseValue(String appId, String env, String cluster,
                                String namespace, String comment, String author) {
        boolean flag = false;
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setReleasedBy(author);
        namespaceReleaseDTO.setReleaseTitle(comment);
        namespaceReleaseDTO.setReleaseComment(comment);
        try {
            client.publishNamespace(appId, env, cluster, namespace, namespaceReleaseDTO);
            flag = true;
        } catch (Exception e) {
            logger.error("releaseValue is Exception;", e);
        }
        return flag;
    }

}
