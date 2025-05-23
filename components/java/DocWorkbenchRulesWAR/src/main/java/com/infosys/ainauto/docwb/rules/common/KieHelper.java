
package com.infosys.ainauto.docwb.rules.common;

import java.util.Collection;

/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/


import org.drools.compiler.builder.impl.KnowledgeBuilderConfigurationImpl;
import org.drools.compiler.lang.descr.PackageDescr;
import org.drools.core.common.InternalAgenda;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.reteoo.builder.NodeFactory;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.definition.KiePackage;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.KieSessionOption;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieHelper{

    private static Logger logger = LoggerFactory.getLogger(KieHelper.class);

    public static KieSession createKieSession(KieBase kbase) {
        return kbase.newKieSession();
    }

    public static KieSession createKieSession(KieBase kbase, KieSessionOption option) {
        KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption(option);
        return kbase.newKieSession(ksconf, null);
    }

    public static KieSession createKieSession(KieBase kbase, KieSessionConfiguration sessionConfiguration, Environment env) {
        return kbase.newKieSession(sessionConfiguration, env);
    }
    
    public static KieSession createKnowledgeSession(KieBase kbase) {
        return kbase.newKieSession();
    }

    public static KieSession createKnowledgeSession(KieBase kbase, KieSessionOption option) {
        KieSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption(option);
        return kbase.newKieSession(ksconf, null);
    }

    public static KieSession createKnowledgeSession(KieBase kbase, KieSessionConfiguration ksconf) {
        return kbase.newKieSession(ksconf, null);
    }

    public static KieSession createKnowledgeSession(KieBase kbase, KieSessionConfiguration ksconf, Environment env) {
        return kbase.newKieSession(ksconf, env);
    }

    public static StatelessKieSession createStatelessKnowledgeSession(KieBase kbase) {
    	logger.debug("Creating stateless session");
    	return kbase.newStatelessKieSession();
    }

    public static KieBase loadKnowledgeBaseFromString(NodeFactory nodeFactory, String... drlContentStrings) throws Exception {
        return loadKnowledgeBaseFromString(null, null, nodeFactory, drlContentStrings);
    }

    public static KieBase loadKnowledgeBaseFromString(String... drlContentStrings) throws Exception {
        return loadKnowledgeBaseFromString(null, null, drlContentStrings);
    }

    public static KieBase loadKnowledgeBaseFromString(KnowledgeBuilderConfiguration config, String... drlContentStrings) throws Exception {
        return loadKnowledgeBaseFromString(config, null, drlContentStrings);
    }

    public static KieBase loadKnowledgeBaseFromString(
            KieBaseConfiguration kBaseConfig, String... drlContentStrings) throws Exception {
        return loadKnowledgeBaseFromString(null, kBaseConfig, drlContentStrings);
    }

    public static KieBase loadKnowledgeBaseFromString( KnowledgeBuilderConfiguration config, KieBaseConfiguration kBaseConfig, String... drlContentStrings) throws Exception {
        return loadKnowledgeBaseFromString( config, kBaseConfig, (NodeFactory)null, drlContentStrings);
    }

    public static KieBase loadKnowledgeBaseFromString( KnowledgeBuilderConfiguration config, KieBaseConfiguration kBaseConfig, NodeFactory nodeFactory, String... drlContentStrings) throws Exception {
        KnowledgeBuilder kbuilder = config == null ? KnowledgeBuilderFactory.newKnowledgeBuilder() : KnowledgeBuilderFactory.newKnowledgeBuilder(config);
        for (String drlContentString : drlContentStrings) {
            kbuilder.add(ResourceFactory.newByteArrayResource(drlContentString
                    .getBytes()), ResourceType.DRL);
        }

        if (kbuilder.hasErrors()) {
        	throw new Exception (kbuilder.getErrors().toString());
            //fail();
        }
        if (kBaseConfig == null) {
            kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        }
        InternalKnowledgeBase kbase = kBaseConfig == null ? KnowledgeBaseFactory.newKnowledgeBase() : KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
        if (nodeFactory != null) {
            ((KnowledgeBaseImpl) kbase).getConfiguration().getComponentFactory().setNodeFactoryProvider(nodeFactory);
        }
        kbase.addPackages( kbuilder.getKnowledgePackages());
        return kbase;
    }

    public static KieBase loadKnowledgeBase(KnowledgeBuilderConfiguration kbuilderConf, KieBaseConfiguration kbaseConf, String... classPathResources) throws Exception {
        Collection<KiePackage> knowledgePackages = loadKnowledgePackages(kbuilderConf, classPathResources);

        if (kbaseConf == null) {
            kbaseConf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(kbaseConf);
        kbase.addPackages(knowledgePackages);
        try {
            kbase = SerializationHelper.serializeObject(kbase);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kbase;
    }

    public static KieBase loadKnowledgeBase(PackageDescr descr) throws Exception {
        return loadKnowledgeBase(null, null, descr);
    }

    public static KieBase loadKnowledgeBase(KnowledgeBuilderConfiguration kbuilderConf,KieBaseConfiguration kbaseConf, PackageDescr descr) throws Exception {
        Collection<KiePackage> knowledgePackages = loadKnowledgePackages(kbuilderConf, descr);

        if (kbaseConf == null) {
            kbaseConf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase(kbaseConf);
        kbase.addPackages(knowledgePackages);
        try {
            kbase = SerializationHelper.serializeObject(kbase);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kbase;
    }

    public static Collection<KiePackage> loadKnowledgePackages(String... classPathResources) throws Exception {
        return loadKnowledgePackages(null, classPathResources);
    }

    public static Collection<KiePackage> loadKnowledgePackages(PackageDescr descr) throws Exception {
        return loadKnowledgePackages(null, descr);
    }

    public static Collection<KiePackage> loadKnowledgePackages(KnowledgeBuilderConfiguration kbuilderConf, PackageDescr descr) throws Exception {
        if (kbuilderConf == null) {
            kbuilderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        }
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbuilderConf);
        kbuilder.add(ResourceFactory.newDescrResource(descr), ResourceType.DESCR);
        if (kbuilder.hasErrors()) {
        	throw new Exception (kbuilder.getErrors().toString());
            //fail(kbuilder.getErrors().toString());
        }
        Collection<KiePackage> knowledgePackages = kbuilder.getKnowledgePackages();
        return knowledgePackages;
    }

    public static Collection<KiePackage> loadKnowledgePackages( KnowledgeBuilderConfiguration kbuilderConf, String... classPathResources) throws Exception {
        return loadKnowledgePackages(kbuilderConf, true, classPathResources);
    }

    public static Collection<KiePackage> loadKnowledgePackages( KnowledgeBuilderConfiguration kbuilderConf, boolean serialize, String... classPathResources) throws Exception {
        if (kbuilderConf == null) {
            kbuilderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbuilderConf);
        for (String classPathResource : classPathResources) {
            kbuilder.add(ResourceFactory.newClassPathResource(classPathResource, KieHelper.class), ResourceType.DRL);
        }

        if (kbuilder.hasErrors()) {
            //fail(kbuilder.getErrors().toString());
            throw new Exception (kbuilder.getErrors().toString());
        }

        Collection<KiePackage> knowledgePackages = null;
        if ( serialize ) {
            try {
                knowledgePackages = SerializationHelper.serializeObject(kbuilder.getKnowledgePackages(),  ((KnowledgeBuilderConfigurationImpl)kbuilderConf).getClassLoader() );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            knowledgePackages = kbuilder.getKnowledgePackages();
        }
        return knowledgePackages;
    }

    public static Collection<KiePackage> loadKnowledgePackagesFromString(String... content) throws Exception {
        return loadKnowledgePackagesFromString(null, content);
    }

    public static Collection<KiePackage> loadKnowledgePackagesFromString(KnowledgeBuilderConfiguration kbuilderConf, String... content) throws Exception {
        if (kbuilderConf == null) {
            kbuilderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        }
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbuilderConf);
        for (String r : content) {
            kbuilder.add(ResourceFactory.newByteArrayResource(r.getBytes()),ResourceType.DRL);
        }
        if (kbuilder.hasErrors()) {
            //fail(kbuilder.getErrors().toString());
        	throw new Exception (kbuilder.getErrors().toString());
        }
        Collection<KiePackage> knowledgePackages = kbuilder.getKnowledgePackages();
        return knowledgePackages;
    }

    public static KieBase loadKnowledgeBase(KnowledgeBuilderConfiguration kbuilderConf,String... classPathResources) throws Exception {
        return loadKnowledgeBase(kbuilderConf, null, classPathResources);
    }

    public static KieBase loadKnowledgeBase(KieBaseConfiguration kbaseConf, String... classPathResources) throws Exception {
        return loadKnowledgeBase(null, kbaseConf, classPathResources);
    }


    public static KieBase getKnowledgeBase() {
        KieBaseConfiguration kBaseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        return getKnowledgeBase(kBaseConfig);
    }

    public static KieBase getKnowledgeBase(KieBaseConfiguration kBaseConfig) {
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
        try {
            kbase = SerializationHelper.serializeObject(kbase, ((InternalKnowledgeBase) kbase).getRootClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kbase;
    }

    public static KieBase loadKnowledgeBase(String... classPathResources) throws Exception {
        return loadKnowledgeBase(null, null, classPathResources);
    }

    public static InternalAgenda getInternalAgenda(StatefulKnowledgeSession session) {
        return (InternalAgenda) session.getAgenda();
    }
    
}

