/**
 * 
 */
package com.tick.tracker.web;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tick.tracker.Constants;
import com.tick.tracker.MessageDTO.ActivatedTick;
import com.tick.tracker.MessageDTO.ExitedTick;
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.reactive_stream.TickPublisher;
import com.tick.tracker.reactive_stream.TickSubscriber;
import com.tick.tracker.reactive_stream.TickSupplier;
import com.tick.tracker.statistics.TickStatisticsCalculator;
import com.tick.tracker.statistics.TickStatisticsDataStructure;


/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 * all the required spring beans are defined here.
 */
@Configuration
public class TickConfiguration {
	
	 @Bean(name="tickStatisticsMap")
	 public TickStatisticsDataStructure tickStatisticsMap() {
		 return new TickStatisticsDataStructure();
	 }
	
	 @Bean(name="instrumentMap")
	 public  ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap() {
		 return new ConcurrentHashMap<String, TickStatisticsDataStructure>();
	 }
	 
	 @Bean(name="tickStatisticsCalculator")
	 public TickStatisticsCalculator tickStatisticsCalculator(
			 @Autowired @Qualifier("tickStatisticsMap") TickStatisticsDataStructure tickStatisticsMap,
			 @Autowired @Qualifier("instrumentMap") ConcurrentHashMap<String, TickStatisticsDataStructure>  instrumentMap
			 ){
		 return new TickStatisticsCalculator(tickStatisticsMap, instrumentMap);
	 }
	 /**
	  * T
	  * his is the actual supplier that take input from web component. 
	  * @TickService injects ticks into this component using supplier.insertValue(T) method
	  * @return
	  * 
	  */
	 @Bean(name="supplier")
	 public TickSupplier<ReceivedTick> getSupplier() { 
			return new TickSupplier<ReceivedTick>();
		}
	 
	 /**
	  * 
	  * This queue is used to trigger tick expiration.
	  * 
	  * @return
	  */
	 @Bean(name="activeQueue")
	 BlockingQueue<ActivatedTick> activeQueue(){
		  BlockingQueue<ActivatedTick> activeQueue=new DelayQueue<ActivatedTick>();
		  return activeQueue;
	 }
	 /**
	  * This is the tick TickPublisher that takes Supplier and publishes 
	  * ticks on demand for statistics calculation.
	  * This component publishes active ticks that should be consumed (inserted or checkin()) by TicksStatisticsCalculator
	  * 
	  * @param activeQueue
	  * @param tickStatisticsCalculator
	  * @param supplier
	  * @return
	  */
	 @Bean("tickPublisher")
	 public TickPublisher<ReceivedTick>  reactivePublisher(
			 @Autowired @Qualifier("activeQueue") BlockingQueue<ActivatedTick> activeQueue,  
			 @Autowired @Qualifier("tickStatisticsCalculator")  TickStatisticsCalculator tickStatisticsCalculator,
			 @Autowired @Qualifier("supplier") TickSupplier<ReceivedTick>  supplier){
	        final TickPublisher<ReceivedTick> thePublisher = new TickPublisher<ReceivedTick>( Constants.Threading.SCHEDULER, supplier);
	        
	        final TickSubscriber<ReceivedTick, ActivatedTick> theSubscriber = new TickSubscriber<ReceivedTick, ActivatedTick>(tickStatisticsCalculator,
	        		activeQueue, ActivatedTick.class); 
	        thePublisher.subscribe(theSubscriber);
	        return thePublisher;
	 }
	 /**
	  * This component publishes expired ticks that should be consumed (removed or checkout()) by TicksStatisticsCalculator
	  * 
	  * @param activeQueue
	  * @param tickStatisticsCalculator
	  * @param supplier
	  * @return
	  */
	 @Bean("tickExpirer")
	 public TickPublisher<ActivatedTick>  reactiveConsumer(
			 @Autowired @Qualifier("activeQueue") BlockingQueue<ActivatedTick> activeQueue,  
			 @Autowired @Qualifier("tickStatisticsCalculator")  TickStatisticsCalculator tickStatisticsCalculator,
			 @Autowired @Qualifier("supplier") TickSupplier<ReceivedTick>  supplier){
		
		 // Expired Ticks
	     Supplier<ActivatedTick> expiredSupplier=getSupplierFromQueue(activeQueue);
	     final TickPublisher<ActivatedTick> expiredPublisher = new TickPublisher<ActivatedTick>( Constants.Threading.EXPIRED_TICKS_SCHEDULER, expiredSupplier);
	     final TickSubscriber<ActivatedTick, ExitedTick> expiredSubscriber = new TickSubscriber<ActivatedTick, ExitedTick>(tickStatisticsCalculator, ExitedTick.class); //Constants.Threading.IO_BOUND
	     expiredPublisher.subscribe(expiredSubscriber); 
	     return expiredPublisher;
	 }
	
	 /**
	  * This is the Flow.Supplier wrapper created from BlockingQueue
	  * @param activeQueue
	  * @return
	  */
	 private static Supplier<ActivatedTick> getSupplierFromQueue(BlockingQueue<ActivatedTick> activeQueue) {
			return new Supplier<ActivatedTick>() {
				@Override
				public ActivatedTick get() {
					try {
						return activeQueue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
	        	
	        }; 
		}
     
}
