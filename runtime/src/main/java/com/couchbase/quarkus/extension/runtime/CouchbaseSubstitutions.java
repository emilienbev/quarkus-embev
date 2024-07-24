package com.couchbase.quarkus.extension.runtime;

import com.couchbase.client.java.codec.*;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class CouchbaseSubstitutions {
}

@TargetClass(className = "com.couchbase.client.java.env.ClusterEnvironment")
final class Target_ClusterEnvironment {

    //    @Alias
    //    private com.couchbase.client.java.codec.JsonSerializer jsonSerializer;
    //
    //    @Alias
    //    private Transcoder transcoder;
    //
    //    @Alias
    //    private Optional<CryptoManager> cryptoManager;

    //    @Substitute
    //    private Target_ClusterEnvironment(ClusterEnvironment.Builder builder) {
    //        super(builder);
    //        this.jsonSerializer = builder.jsonSerializer != null
    //                ? new JsonValueSerializerWrapper(builder.jsonSerializer)
    //                : DefaultJsonSerializer.create(builder.cryptoManager);
    //        this.transcoder = defaultIfNull(builder.transcoder, () -> JsonTranscoder.create(jsonSerializer));
    //        this.cryptoManager = Optional.ofNullable(builder.cryptoManager);
    //    }

    @Substitute
    private boolean nonShadowedJacksonPresent() {
        return false;
    }
}
