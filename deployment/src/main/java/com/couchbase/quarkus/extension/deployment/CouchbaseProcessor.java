/*
 * Copyright (c) 2021 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.quarkus.extension.deployment;

import jakarta.enterprise.context.ApplicationScoped;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.quarkus.extension.runtime.CouchbaseConfig;
import com.couchbase.quarkus.extension.runtime.CouchbaseRecorder;
import com.couchbase.quarkus.extension.runtime.jacksonhandling.JacksonSupportRecorder;

import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;

public class CouchbaseProcessor {

    //TODO: Figure out arguments for this, currently doesn't seem to be working.
    //    @BuildStep
    //    RemovedResourceBuildItem overrideTwo() {
    //        return new RemovedResourceBuildItem(ArtifactKey.fromString(
    //                "com.couchbase.client.core.deps.org.xbill.DNS.config:java-client"),
    //                Collections.singleton("com/couchbase/client/core/deps/org/xbill/DNS/config/WindowsResolverConfigProvider.class"));
    //    }

    //    @BuildStep
    //    void setIsWindows(BuildProducer<SystemPropertyBuildItem> systemProperty) {
    //        systemProperty.produce(new SystemPropertyBuildItem("dnsjava.configprovider.skipinit", "true"));
    //    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void produceCouchbaseClient(CouchbaseRecorder recorder, JacksonSupportRecorder jacksonRecorder,
            CouchbaseConfig config,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        //TODO: WIP
        ClusterEnvironment.Builder builder = ClusterEnvironment.builder();

        syntheticBeans.produce(SyntheticBeanBuildItem
                .configure(Cluster.class)
                .scope(ApplicationScoped.class)
                .unremovable()
                .supplier(recorder.getCluster(config))
                .setRuntimeInit()
                .done());

    }

}
