package com.tick.tracker.reactive_stream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;

import com.tick.tracker.Constants;
import com.tick.tracker.MessageDTO.BasicTick;
import com.tick.tracker.MessageDTO.StatisticsCalculatorInterface;
import com.tick.tracker.MessageDTO.TickConsumerInterface;
import com.tick.tracker.MessageDTO.TickModel;

/**
 * The TickSubscriber program implements a Subscriber and TickConsumerInterface 
 * that would take tick that is sent by Supplier.
 * - This is a generic component
 * - works on backpressure
 * 
 * @author sonu.yasir@gmail.com
 * @version 1.0
 * @since 2020-01-23
 * 
 */

public class TickSubscriber<T extends TickModel,R extends TickModel> implements Subscriber<T>, TickConsumerInterface{
	
	private Subscription subscription;
	private AtomicInteger  demand;
	private BlockingQueue<R> queue=null;
	private Class<R> clazz;
	private StatisticsCalculatorInterface statisticsCalculator;
	
	public TickSubscriber(StatisticsCalculatorInterface statisticsCalculator, 
			BlockingQueue<R> queue, Class<R> clazz) {
		this(statisticsCalculator, clazz);
		this.queue=queue;
	}
	public TickSubscriber(StatisticsCalculatorInterface statisticsCalculator, Class<R> clazz) {
		demand=new AtomicInteger(Constants.SUBSCRIBER_BUFFER_SIZE);
		this.clazz=clazz;
		this.statisticsCalculator=statisticsCalculator;
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription=subscription;
	}

	@Override
	public void onError(Throwable throwable) {
		this.subscription.cancel();
		
	}

	@Override
	public void onComplete() {
		this.subscription.cancel();
		
	}
	@Override
	public void onNext(T item) {
		this.consumeTick(item.getBasicTick()); 
		 /**
		  * if required, move the tick to next stage
		  */
		 Optional 	
		 	.ofNullable(queue)  	
		 	.ifPresent(action->queue.offer(createTickForNextStage(item)));
		
		 
		if(demand.decrementAndGet()<=0) {
			cleanupFunctionToCheckConsistancy();
			initiateRequest();
		}
	}
	
	private void initiateRequest() { 
		int buffer = demand.addAndGet(Constants.SUBSCRIBER_BUFFER_SIZE);
		subscription.request(buffer);
		
	}
	
	private boolean cleanupFunctionToCheckConsistancy() {
		boolean health=this.checkHealth();
		return health;
	}
	private boolean checkHealth() {
		
		return statisticsCalculator.checkHealth();
	}
	/**
	 * Creates Immutable Tick to be consumed by the next stage
	 * for next stage for ReceivedTick ->  ActiveTick
	 * for next stage for ActiveTick ->  ExpiredTick
	 * Used Reflection to create next stage tick 
	 * @param tick
	 * @return
	 */
	private R createTickForNextStage(T tick)  {
		R nextStep=null;
			try {
				Constructor<R> constructorR = this.clazz.getConstructor(tick.getInstrument().getClass(), Double.class, Long.class);
				nextStep=constructorR.newInstance(tick.getInstrument(), tick.getPrice(), tick.getTimestamp());
				
			
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e ) {
				e.printStackTrace();
			}
			return nextStep;
	}
	@Override
	public void consumeTick(BasicTick tick) {
		this.statisticsCalculator.consumeTick(tick);
		
	}
	
}
