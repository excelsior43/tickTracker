/**
 * 
 */
package com.tick.tracker.reactive_stream;

import java.util.Arrays;
import java.util.Date;

import java.util.concurrent.BlockingQueue;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tick.tracker.Constants;
import com.tick.tracker.Constants.Threading;
import com.tick.tracker.MessageDTO.ActivatedTick;
import com.tick.tracker.MessageDTO.BasicTick;
import com.tick.tracker.MessageDTO.ExitedTick;
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.MessageDTO.StatisticsCalculatorInterface;
import com.tick.tracker.MessageDTO.StatisticsInterface;
import com.tick.tracker.MessageDTO.StatisticsNotFoundException;
import com.tick.tracker.MessageDTO.Status;
import com.tick.tracker.reactive_stream.TickSupplier;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
  
public class TickSupplierReactiveIntegTest {   

	 TickSupplier<ReceivedTick> supplier;
	 TickPublisher<ReceivedTick> thePublisher;
	 BlockingQueue<String> orderQueue ; 
    
	@BeforeEach
	public void init() throws InterruptedException  {
		 orderQueue=new LinkedBlockingQueue<String>(Arrays.asList("DIAMOND","HDFC","SUN","IBM","SBI","DIAMOND","WELLS"));
    	 BlockingQueue<ActivatedTick> activeQueue=new DelayQueue<ActivatedTick>();
    	 StatisticsCalculatorInterface  statistics=getTestStatistics();
    	// Active Ticks 
    	supplier=new TickSupplier<ReceivedTick>(); 
        thePublisher = new TickPublisher<ReceivedTick>( Constants.Threading.SCHEDULER, supplier);
        final TickSubscriber<ReceivedTick, ActivatedTick> theSubscriber = new TickSubscriber<ReceivedTick, ActivatedTick>(statistics, activeQueue, ActivatedTick.class); //Constants.Threading.IO_BOUND 
        thePublisher.subscribe(theSubscriber);
        thePublisher.init();
        //Thread.sleep(500);
        // Expired Ticks
        Supplier<ActivatedTick> expiredSupplier=getSupplierFromQueue(activeQueue);
        final TickPublisher<ActivatedTick> expiredPublisher = new TickPublisher<ActivatedTick>( Constants.Threading.EXPIRED_TICKS_SCHEDULER, expiredSupplier);
        final TickSubscriber<ActivatedTick, ExitedTick> expiredSubscriber = new TickSubscriber<ActivatedTick, ExitedTick>(statistics, ExitedTick.class); //Constants.Threading.IO_BOUND
        expiredPublisher.subscribe(expiredSubscriber);
        expiredPublisher.init();
        Thread.sleep(500);
        long delay = Threading.DELAY_WINDOW;
        supplier.insertValue(new ReceivedTick("WELLS", 0.001, new Date().getTime()-delay + 59));
		supplier.insertValue(new ReceivedTick("IBM", 11.89, new Date().getTime()-delay  + 40));
		supplier.insertValue(new ReceivedTick("DIAMOND", 0.001, new Date().getTime()-delay + 0));
		supplier.insertValue(new ReceivedTick("SBI", 0.59, new Date().getTime()-delay + 48));
		supplier.insertValue(new ReceivedTick("DIAMOND", 0.001, new Date().getTime()-delay  + 55));
		supplier.insertValue(new ReceivedTick("HDFC", 10.89, new Date().getTime()-delay  + 23));
		supplier.insertValue(new ReceivedTick("SUN", 0.001, new Date().getTime()-delay + 30));
		Thread.sleep(500);
		}
	
	

	@Test
	public void integTestToTestIfTheTickIsExpiringAsPerTimestamp() {
		Assertions.assertTrue(orderQueue.size()==0);
	}
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
	private StatisticsCalculatorInterface getTestStatistics() {
		return new StatisticsCalculatorInterface() {

			@Override
			public void consumeTick(BasicTick tick) {
				if(tick.getStatus()==Status.ACTIVATED) {
					try {
						Assertions.assertEquals(tick.getInstrument(), orderQueue.take());
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
				}
			}

			@Override
			public StatisticsInterface getStatistics() throws StatisticsNotFoundException { return null;}
			@Override
			public boolean checkHealth() {return false;	}
			@Override
			public StatisticsInterface getStatistics(String instrument) throws StatisticsNotFoundException {	return null;}};
	}
}
