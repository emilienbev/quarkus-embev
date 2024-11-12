/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.couchbase.quarkus.extension.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;

@Path("/couchbase-quarkus-extension")
@ApplicationScoped
public class CouchbaseQuarkusExtensionResource {
    // add some rest methods here

    @Inject
    Cluster cluster;

    @GET
    public String hello() {
        return "Hello couchbase-quarkus-extension";
    }

    @GET
    @Path("/clusterCheck")
    public String clusterCheck() {
        var query = cluster.query("select 1 as test");
        return "hello";
    }

    @GET
    @Path("/kvCheck")
    public String kvCheck() {
        var col = cluster.bucket("default").scope("_default").collection("_default");
        col.upsert("idTest", JsonObject.create().put("content", "value"));
        return "hello";
    }

    @GET
    @Path("/asyncKvCheck")
    public String asyncKvCheck() {
        var col = cluster.async().bucket("default").scope("_default").collection("_default");
        col.upsert("idTest", JsonObject.create().put("content", "value"));
        return "hello";
    }

    @GET
    @Path("/reactiveKvCheck")
    public String reactiveKvCheck() {
        var col = cluster.reactive().bucket("default").scope("_default").collection("_default");
        col.upsert("idTest", JsonObject.create().put("content", "value")).block();
        return "hello";
    }

    @GET
    @Path("/check")
    public String check() {
        var test = new com.couchbase.client.java.manager.user.GetGroupOptions();
        return "hello";
    }

    @GET
    @Path("/newCheck")
    public String NewCheck() {
        var test = Cluster.connect("couchbase://localhost", "Administrator", "password");
        var query = cluster.query("select 1 as test");
        return "hello";
    }

    @GET
    @Path("/isClusterNull")
    public String isNull() {
        if (cluster == null) {
            return "Yes cluster is null";
        } else {
            return "no cluster is not null";
        }
    }
}
