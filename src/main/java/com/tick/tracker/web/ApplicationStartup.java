/**
 * 
 */
package com.tick.tracker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.tick.tracker.MessageDTO.ActivatedTick;
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.reactive_stream.TickPublisher;

 
/**
 * 
 * This is the startup hook for firing up up Reactive Producer and subscribers
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationStartedEvent> { 

    private static final Logger log=LoggerFactory.getLogger(ApplicationStartup.class); 

  /**
   * This event is executed as late as conceivably possible to indicate that 
   * the application is ready to service requests.
   */
	@Autowired
	@Qualifier("tickPublisher")
	TickPublisher<ReceivedTick>  reactivePublisher;  
	
	@Autowired
	@Qualifier("tickExpirer")
	TickPublisher<ActivatedTick>  reactiveConsumer; 
	
  @Override
  public void onApplicationEvent(final ApplicationStartedEvent event) {
	  reactivePublisher.init();
	  log.debug("Initialized reactive consumer and publisher");
      reactiveConsumer.init();
      
  }
}