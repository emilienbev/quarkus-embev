package com.couchbase.quarkus.extension.runtime;

import java.util.function.BooleanSupplier;

import com.couchbase.client.core.deps.org.xbill.DNS.config.BaseResolverConfigProvider;
import com.couchbase.client.core.deps.org.xbill.DNS.config.InitializationException;
import com.couchbase.client.core.deps.org.xbill.DNS.config.WindowsResolverConfigProvider;
import com.couchbase.client.core.encryption.CryptoManager;
import com.couchbase.client.java.codec.*;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class CouchbaseSubstitutions {
}

//@TargetClass(className = "com.couchbase.client.java.env.ClusterEnvironment")
//final class Target_ClusterEnvironment {

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

@TargetClass(value = ClusterEnvironment.class, onlyWith = TargetClusterEnvironment.IsJacksonAbsent.class)
final class TargetClusterEnvironment {
    @Substitute
    private JsonSerializer newDefaultSerializer(CryptoManager cryptoManager) {
        return DefaultJsonSerializer.create(cryptoManager);
    }

    public static class IsJacksonAbsent implements BooleanSupplier {

        @Override
        public boolean getAsBoolean() {
            try {
                Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
                return false;
            } catch (ClassNotFoundException ignored) {
                return true;
            }
        }
    }
}

@TargetClass(value = WindowsResolverConfigProvider.class)
final class TargetWindowsResolver {

    //    @Inject
    //    private static final boolean IsOSWindows = false;

    @Substitute
    public TargetWindowsResolver() {
    }

    @Substitute
    public void initialize() throws InitializationException {
    }

    @Substitute
    private static final class InnerWindowsResolverConfigProvider extends BaseResolverConfigProvider {
        @Override
        public void initialize() {

        }
    }
}

//@TargetClass(value = AndroidResolverConfigProvider.class)
//final class Target_AndroidResolverConfigProvider {
//
//    @Substitute
//    public void initialize() throws InitializationException {
//        return;
//    }
//}
