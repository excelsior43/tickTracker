package com.tick.tracker.reactive_stream;

import java.util.Objects; 
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

/**
 * The TickPublisher program implements a Publisher 
 * that would take tick that is sent over by Supplier.
 * 
 * it implements backpressure and only serves ticks when threads are ready to process them.
 * - This is a generic component
 * 
 * @author sonu.yasir@gmail.com
 * @version 1.0
 * @since 2020-01-23
 * 
 */
public class TickPublisher<T> implements Publisher<T> {
	private TickSubscription<T> theSubscription;

	public TickPublisher(final ScheduledExecutorService execService, Supplier<T> supplier) {
		Objects.requireNonNull(execService);
		Objects.requireNonNull(supplier);

		this.theSubscription = new TickSubscription<T>(execService, supplier);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		this.theSubscription.setSubscriber(subscriber);
		subscriber.onSubscribe(theSubscription);

	}

	public void init() {
		this.theSubscription.init();
	}

	public void stop() {
		this.theSubscription.cancel();

	}

}
