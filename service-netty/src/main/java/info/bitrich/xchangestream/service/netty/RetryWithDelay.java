package info.bitrich.xchangestream.service.netty;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RetryWithDelay implements Function<Flowable<? extends Throwable>, Publisher<?>> {
    private final long retryDelayMillis;
    private final AtomicBoolean ignoreRetry;

    public RetryWithDelay(final long retryDelayMillis, AtomicBoolean ignoreRetry) {
        this.retryDelayMillis = retryDelayMillis;
        this.ignoreRetry = ignoreRetry;
    }

    @Override
    public Publisher<?> apply(Flowable<? extends Throwable> flowable) {
        return flowable
                .takeWhile(e -> !ignoreRetry.get())
                .flatMap((Function<Throwable, Publisher<?>>) throwable -> {
                    return Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                });
    }
}
