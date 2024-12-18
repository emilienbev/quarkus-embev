package com.couchbase.quarkus.extension.deployment;

import com.couchbase.quarkus.extension.runtime.CouchbaseConfig;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class CouchbaseDevUiProcessor {
    @BuildStep(onlyIf = IsDevelopment.class)
    public CardPageBuildItem pages(CouchbaseConfig config) {
        CardPageBuildItem cardPageBuildItem = new CardPageBuildItem();

        var hostname = extractHostnameFromConnectionString(config.connectionString());
        var clusterUiUrl = "http://" + hostname + ":8091/ui/index.html";

        String JAVA_SDK_DOCS = "https://docs.couchbase.com/java-sdk/current/hello-world/overview.html";

        cardPageBuildItem.addPage(Page.externalPageBuilder("Cluster Dashboard")
                .url(clusterUiUrl, clusterUiUrl)
                .doNotEmbed()
                .icon("font-awesome-solid:database"));

        cardPageBuildItem.addPage(Page.externalPageBuilder("Java SDK Docs")
                .url(JAVA_SDK_DOCS, JAVA_SDK_DOCS)
                .doNotEmbed()
                .icon("font-awesome-solid:couch"));

        cardPageBuildItem.addPage(Page.externalPageBuilder("Extension Guide")
                .url("https://docs.quarkiverse.io/quarkus-couchbase/dev/index.html")
                .isHtmlContent()
                .icon("font-awesome-solid:book"));

        return cardPageBuildItem;
    }

    /**
     * Extracts the first hostname from the connection string to redirect to the Cluster UI Dashboard.
     *
     * @param connectionString The connection string specified in application.properties.
     * @return The first hostname, or "localhost" by default.
     */
    private String extractHostnameFromConnectionString(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            return "localhost";
        }

        var hosts = connectionString;
        if (hosts.startsWith("couchbase://")) {
            hosts = hosts.substring("couchbase://".length());
        }

        var hostPorts = hosts.split(",");
        if (hostPorts.length == 0) {
            return "localhost";
        }

        var firstHostPort = hostPorts[0].trim();
        int colonIndex = firstHostPort.indexOf(':');
        if (colonIndex == -1) {
            return firstHostPort;
        }

        return firstHostPort.substring(0, colonIndex);
    }
}
