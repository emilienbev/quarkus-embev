package com.couchbase.quarkus.extension.runtime;

import java.time.Duration;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jctools.queues.atomic.unpadded.MpscAtomicUnpaddedArrayQueue;

import com.couchbase.client.core.cnc.EventBus;
import com.couchbase.client.core.cnc.OrphanReporter;
import com.couchbase.client.core.env.OrphanReporterConfig;
import com.couchbase.client.core.msg.Request;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

public class OrphanReporterSubstitutions {
}

@TargetClass(OrphanReporter.class)
final class Target_OrphanReporter {

    @Alias
    public static String ORPHAN_TREAD_PREFIX;
    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    private static AtomicInteger ORPHAN_REPORTER_ID = new AtomicInteger();;
    @Alias
    private static String KEY_TOTAL_MICROS;
    @Alias
    private static String KEY_DISPATCH_MICROS;
    @Alias
    private static String KEY_TOTAL_DISPATCH_MICROS;
    @Alias
    private static String KEY_ENCODE_MICROS;
    @Alias
    private static String KEY_SERVER_MICROS;
    @Alias
    private static String KEY_TOTAL_SERVER_MICROS;
    @Alias
    private static String KEY_OPERATION_ID;
    @Alias
    private static String KEY_OPERATION_NAME;
    @Alias
    private static String KEY_LAST_LOCAL_SOCKET;
    @Alias
    private static String KEY_LAST_REMOTE_SOCKET;
    @Alias
    private static String KEY_LAST_LOCAL_ID;
    @Alias
    private static String KEY_TIMEOUT;
    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    private AtomicBoolean running = new AtomicBoolean(false);
    @Alias
    volatile Thread worker; // visible for testing
    @Alias
    private Queue<Request<?>> orphanQueue;
    @Alias
    private Duration emitInterval;
    @Alias
    private int sampleSize;
    @Alias
    private EventBus eventBus;
    @Alias
    private boolean enabled;
    @Alias
    private OrphanReporterConfig config;

    @Substitute
    public Target_OrphanReporter(final EventBus eventBus, final OrphanReporterConfig config) {
        this.eventBus = eventBus;
        this.orphanQueue = new MpscAtomicUnpaddedArrayQueue<>(config.queueLength());
        this.emitInterval = config.emitInterval();
        this.sampleSize = config.sampleSize();
        this.enabled = config.enabled();
        this.config = config;

        // Spawn a thread only if the reporter is enabled.
        if (enabled) {
            worker = new Thread(new Target_OrphanReporter.Target_Worker());
            worker.setDaemon(true);
            worker.setName(ORPHAN_TREAD_PREFIX + ORPHAN_REPORTER_ID.incrementAndGet());
        }
    }

    @Alias
    public native OrphanReporterConfig config();

    @TargetClass(value = OrphanReporter.class, innerClass = "Worker")
    private static final class Target_Worker implements Runnable {
        @Override
        @Alias
        public native void run();

        @Alias
        private Comparator<Request<?>> THRESHOLD_COMPARATOR;
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private Queue<Request<?>> kvOrphans = new PriorityQueue<>(THRESHOLD_COMPARATOR);
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private Queue<Request<?>> queryOrphans = new PriorityQueue<>(THRESHOLD_COMPARATOR);
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private Queue<Request<?>> viewOrphans = new PriorityQueue<>(THRESHOLD_COMPARATOR);
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private Queue<Request<?>> searchOrphans = new PriorityQueue<>(THRESHOLD_COMPARATOR);
        @Alias
        @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
        private Queue<Request<?>> analyticsOrphans = new PriorityQueue<>(THRESHOLD_COMPARATOR);
    }
}
