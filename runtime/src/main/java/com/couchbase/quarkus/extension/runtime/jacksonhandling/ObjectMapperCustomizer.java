package com.couchbase.quarkus.extension.runtime.jacksonhandling;

import com.couchbase.client.core.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.quarkus.extension.runtime.jacksonhandling.runtime.ObjectMapperProducer;

/**
 * Meant to be implemented by a CDI bean that provides arbitrary customization for the default {@link ObjectMapper}.
 * <p>
 * All implementations (that are registered as CDI beans) are taken into account when producing the default
 * {@link ObjectMapper}.
 * <p>
 * See also {@link ObjectMapperProducer#objectMapper}.
 */
public interface ObjectMapperCustomizer extends Comparable<ObjectMapperCustomizer> {

    int MINIMUM_PRIORITY = Integer.MIN_VALUE;
    int MAXIMUM_PRIORITY = Integer.MAX_VALUE;
    // we use this priority to give a chance to other customizers to override serializers / deserializers
    // that might have been added by the modules that Quarkus registers automatically
    // (Jackson will keep the last registered serializer / deserializer for a given type
    // if multiple are registered)
    int QUARKUS_CUSTOMIZER_PRIORITY = MINIMUM_PRIORITY + 100;
    int DEFAULT_PRIORITY = 0;

    void customize(ObjectMapper objectMapper);

    /**
     * Defines the priority that the customizers are applied.
     * A lower integer value means that the customizer will be applied after a customizer with a higher priority
     */
    default int priority() {
        return DEFAULT_PRIORITY;
    }

    default int compareTo(ObjectMapperCustomizer o) {
        return Integer.compare(o.priority(), priority());
    }
}
