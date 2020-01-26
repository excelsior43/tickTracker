package com.tick.tracker.reactive_stream;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import com.tick.tracker.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the subscription component that acts as a mediator between publisher and subscriber.
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 * @param <T>
 */
public class TickSubscription<T> implements Subscription{

    private static final Logger log=LoggerFactory.getLogger(TickSubscription.class); 

	private final AtomicLong demand;
    private final AtomicBoolean cancelled;
	private Supplier<T> tickSupplier;
	private Subscriber<? super T> subscriber;
	private ScheduledExecutorService execService;
    
    public TickSubscription( ScheduledExecutorService execService, Supplier<T> tickSupplier ) {
    	demand=new AtomicLong(Constants.SUBSCRIBER_BUFFER_SIZE);
    	cancelled=new AtomicBoolean(false);
    	this.tickSupplier=tickSupplier;
    	this.execService=execService;
    }
	
	@Override
	public void request(long n) {
		 log.debug("\nThread %s : Downstream demand is %d\n", Thread.currentThread().getName(), this.demand.get());
		 this.demand.set(this.cancelled.get() ? 0 : n);
	}

	@Override
	public void cancel() {
		cancelled.compareAndExchange(false, true);
	}
	
	void publish() {
         // As long as we have demand poll queue and send items
         while (this.demand.getAndDecrement() > 0) {
             final T receivedTick = tickSupplier.get();
             this.subscriber.onNext(receivedTick);
         }
     }

    public void init() {
    	this.execService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
            	publish();
            }
        }, Constants.Threading.SCHEDULE_DELAY, Constants.Threading.SCHEDULE_DELAY, TimeUnit.MILLISECONDS);
    }
    
    public void setSubscriber(Subscriber<? super T> subscriber) {
		this.subscriber=subscriber;
		
	}

}
