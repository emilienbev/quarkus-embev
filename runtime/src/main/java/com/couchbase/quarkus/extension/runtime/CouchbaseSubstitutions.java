package com.couchbase.quarkus.extension.runtime;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import com.couchbase.client.core.deps.org.xbill.DNS.Name;
import com.couchbase.client.core.deps.org.xbill.DNS.ResolverConfig;
import com.couchbase.client.core.deps.org.xbill.DNS.SimpleResolver;
import com.couchbase.client.core.deps.org.xbill.DNS.config.*;
import com.couchbase.client.core.encryption.CryptoManager;
import com.couchbase.client.java.codec.*;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.oracle.svm.core.annotate.Alias;
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

@TargetClass(value = ResolverConfig.class)
final class Target_ResolverConfig {
    @Alias
    private List<InetSocketAddress> servers = new ArrayList<>(2);
    @Alias
    private List<Name> searchlist = new ArrayList<>(0);
    @Alias
    private int ndots = 1;
    @Alias
    private static ResolverConfig currentConfig;
    @Alias
    private static List<ResolverConfigProvider> configProviders;

    @Substitute
    public Target_ResolverConfig() {
        synchronized (ResolverConfig.class) {
            if (configProviders == null) {
                configProviders = new ArrayList<>(5);
                configProviders.add(new PropertyResolverConfigProvider());
                configProviders.add(new ResolvConfResolverConfigProvider());
                configProviders.add(new JndiContextResolverConfigProvider());
                configProviders.add(new SunJvmResolverConfigProvider());
                configProviders.add(new FallbackPropertyResolverConfigProvider());

            }
        }

        for (ResolverConfigProvider provider : configProviders) {
            if (provider.isEnabled()) {
                try {
                    provider.initialize();
                    if (servers.isEmpty()) {
                        servers.addAll(provider.servers());
                    }

                    if (searchlist.isEmpty()) {
                        List<Name> lsearchPaths = provider.searchPaths();
                        if (!lsearchPaths.isEmpty()) {
                            searchlist.addAll(lsearchPaths);
                            ndots = provider.ndots();
                        }
                    }

                    if (!servers.isEmpty() && !searchlist.isEmpty()) {
                        // found both servers and search path, we're done
                        return;
                    }
                } catch (InitializationException e) {
                    //Ignore
                }
            }
        }

        if (servers.isEmpty()) {
            servers.add(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), SimpleResolver.DEFAULT_PORT));
        }
    }
}

//@TargetClass(value = WindowsResolverConfigProvider.class, onlyWith = TargetWindowsResolver.IsOSWindows.class)
//final class TargetWindowsResolver {
//
//    @Substitute
//    public TargetWindowsResolver() {
//
//    }
//
//    @Substitute
//    private static final class InnerWindowsResolverConfigProvider extends BaseResolverConfigProvider {
//        @Override
//        public void initialize() {
//
//        }
//    }
//
//    public static class IsOSWindows implements BooleanSupplier {
//        @Override
//        public boolean getAsBoolean() {
//            return true;
//        }
//    }
//}
