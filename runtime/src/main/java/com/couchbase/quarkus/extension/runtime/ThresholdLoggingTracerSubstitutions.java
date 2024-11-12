package com.couchbase.quarkus.extension.runtime;

import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.jctools.queues.atomic.unpadded.MpscAtomicUnpaddedArrayQueue;

import com.couchbase.client.core.cnc.EventBus;
import com.couchbase.client.core.cnc.RequestSpan;
import com.couchbase.client.core.cnc.RequestTracer;
import com.couchbase.client.core.cnc.tracing.ThresholdLoggingTracer;
import com.couchbase.client.core.env.ThresholdLoggingTracerConfig;
import com.couchbase.client.core.msg.Request;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import reactor.core.publisher.Mono;

public class ThresholdLoggingTracerSubstitutions {
}

@TargetClass(value = ThresholdLoggingTracer.class)
final class Target_ThresholdLoggingTracer implements RequestTracer {

    @Alias
    @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    private static AtomicInteger REQUEST_TRACER_ID = new AtomicInteger();
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
    private Queue<Request<?>> overThresholdQueue;
    @Alias
    private EventBus eventBus;
    @Alias
    private Thread worker;
    @Alias
    private ThresholdLoggingTracerConfig config;
    @Alias
    private long kvThreshold;
    @Alias
    private long queryThreshold;
    @Alias
    private long viewThreshold;
    @Alias
    private long searchThreshold;
    @Alias
    private long analyticsThreshold;
    @Alias
    private long transactionsThreshold;
    @Alias
    private Duration emitInterval;
    @Alias
    private int sampleSize;

    @Substitute
    private Target_ThresholdLoggingTracer(final EventBus eventBus, ThresholdLoggingTracerConfig config) {
        this.eventBus = eventBus;
        this.overThresholdQueue = new MpscAtomicUnpaddedArrayQueue<>(config.queueLength());
        kvThreshold = config.kvThreshold().toNanos();
        analyticsThreshold = config.analyticsThreshold().toNanos();
        searchThreshold = config.searchThreshold().toNanos();
        viewThreshold = config.viewThreshold().toNanos();
        queryThreshold = config.queryThreshold().toNanos();
        transactionsThreshold = config.transactionsThreshold().toNanos();
        sampleSize = config.sampleSize();
        emitInterval = config.emitInterval();
        this.config = config;

        worker = new Thread(new Target_ThresholdLoggingTracer.Target_Worker());
        worker.setDaemon(true);
    }

    @Alias
    public native RequestSpan requestSpan(String s, RequestSpan requestSpan);

    @Alias
    public native Mono<Void> start();

    @Alias
    public native Mono<Void> stop(Duration duration);

    @TargetClass(value = ThresholdLoggingTracer.class, innerClass = "Worker")
    private static final class Target_Worker implements Runnable {
        @Alias
        public native void run();
    }
}
