/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.producer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.internal.EEJPAContextInitializer;
import org.picketlink.internal.EESecurityContextFactory;
import org.picketlink.internal.IdentityStoreAutoConfiguration;
import org.picketlink.internal.SecuredIdentityManager;


/**
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {

    @Inject Instance<IdentityConfiguration> identityConfigInstance;

    @Inject Event<IdentityConfigurationEvent> identityConfigEvent;

    @Inject EESecurityContextFactory icf;

    @Inject EEJPAContextInitializer jpaContextInitializer;

    @Inject IdentityStoreAutoConfiguration autoConfig;

    private IdentityConfiguration identityConfig;

    private IdentityManagerFactory factory;

    @Inject
    public void init() {
        if (identityConfigInstance.isUnsatisfied()) {
            this.identityConfig = new IdentityConfiguration();
        } else if (identityConfigInstance.isAmbiguous()) {
            throw new SecurityConfigurationException("Multiple IdentityConfiguration beans found, can not " +
                    "configure IdentityManagerFactory");
        } else {
            this.identityConfig = identityConfigInstance.get();
        }

        this.identityConfigEvent.fire(new IdentityConfigurationEvent(this.identityConfig));

        if (this.identityConfig.getConfiguredStores().isEmpty()) {
            loadAutoConfig();
        }

        List<IdentityStoreConfiguration> configuredStores = this.identityConfig.getConfiguredStores();

        for (IdentityStoreConfiguration identityStoreConfiguration : configuredStores) {
            if (JPAIdentityStoreConfiguration.class.isInstance(identityStoreConfiguration)) {
                JPAIdentityStoreConfiguration jpaConfig = (JPAIdentityStoreConfiguration) identityStoreConfiguration;
                jpaConfig.addContextInitializer(this.jpaContextInitializer);
            }
        }

        this.identityConfig.contextFactory(this.icf);

        this.factory = this.identityConfig.buildIdentityManagerFactory();
    }

    @SuppressWarnings("unchecked")
    private void loadAutoConfig() {
        JPAIdentityStoreConfiguration jpaConfig = autoConfig.getJPAConfiguration();
        if (jpaConfig.isConfigured()) {
            FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet());
            FeatureSet.addRelationshipSupport(jpaConfig.getFeatureSet());
            jpaConfig.getFeatureSet().setSupportsCustomRelationships(true);
            jpaConfig.getFeatureSet().setSupportsMultiRealm(true);
            this.identityConfig.addConfig(jpaConfig);
        } else {
            FileIdentityStoreConfiguration config = new FileIdentityStoreConfiguration();
            FeatureSet.addFeatureSupport(config.getFeatureSet());
            FeatureSet.addRelationshipSupport(config.getFeatureSet());
            config.getFeatureSet().setSupportsCustomRelationships(true);
            config.getFeatureSet().setSupportsMultiRealm(true);
            this.identityConfig.addConfig(config);
        }
    }

    @Produces @Dependent
    public IdentityManager createIdentityManager() {
        return new SecuredIdentityManager(this.factory.createIdentityManager());
    }

}
