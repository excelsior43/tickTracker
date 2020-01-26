/**
 * 
 */
package com.tick.tracker.web;

import java.util.Objects;
import java.util.concurrent.TimeUnit; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.tick.tracker.Constants;
import com.tick.tracker.MessageDTO.ActivatedTick;
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.reactive_stream.TickPublisher;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 *
 * This is the shud down hook that is used to close all open Threads and other required clean up.
 * 
 */

	@Component
	public class ApplicationShutdown implements ApplicationListener<ApplicationFailedEvent> {

	    private static final Logger log=LoggerFactory.getLogger("com.tick.tracker.web.ApplicationShutdown"); 

	    
		@Autowired
		TickPublisher<ReceivedTick> thePublisher; 
		
		@Autowired
		TickPublisher<ActivatedTick> expiredPublisher;
 
	    @Override
	    public void onApplicationEvent(final ApplicationFailedEvent event) {
	    	log.info("Stopping application: " + event);
	        registerShutdownHook(thePublisher); 
	        registerShutdownHook(expiredPublisher); 
	    }
	    
	    private static void registerShutdownHook(final TickPublisher<?> thePublisher) {
	        assert Objects.isNull(thePublisher);
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            @Override
	            public void run() {

	                try {
	                	log.debug("Shutting down application");
	                    Constants.Threading.SCHEDULER.shutdown();
	                    Constants.Threading.SCHEDULER.awaitTermination(Constants.Threading.AWAIT_TERMINATION, TimeUnit.MILLISECONDS);
	                    
	                    Constants.Threading.EXPIRED_TICKS_SCHEDULER.shutdown();
	                    Constants.Threading.EXPIRED_TICKS_SCHEDULER.awaitTermination(Constants.Threading.AWAIT_TERMINATION, TimeUnit.MILLISECONDS);

	                    thePublisher.stop();
	                } catch (InterruptedException e) {
	                	log.error("InterruptedException ");
	                } 
	            }
	        });
	    }
	}

