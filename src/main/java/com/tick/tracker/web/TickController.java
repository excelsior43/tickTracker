package com.tick.tracker.web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tick.tracker.Constants;
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.MessageDTO.StatisticsInterface;
import com.tick.tracker.MessageDTO.StatisticsNotFoundException;

import org.springframework.web.bind.annotation.RequestBody;
/**
 * 
 * This component has the following 3 mappings defined.
 * 1. POST /ticks to send ticks into reactive_stream component
 * 
 * 2. GET /statistics returns all statistics in the system
 * 3. GET /statistics/{instrument} returns Instrument specific statistics 
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
@RestController
public class TickController { 

	@Autowired
	private TickService tickService;
	
	@PostMapping("/ticks")
	public ResponseEntity<String> tick(@RequestBody ReceivedTick receivedTick) throws InterruptedException {
		
		if(receivedTick.getTimestamp() < (System.currentTimeMillis() - Constants.Threading.DELAY_WINDOW))
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
			tickService.consumeTick(receivedTick);  
			return new ResponseEntity<String>(HttpStatus.OK);
	} 
	
	@GetMapping("/statistics/{instrument}")
	public StatisticsInterface getStatisticsByInstrument(@PathVariable String instrument) throws StatisticsNotFoundException {
		return  tickService.getStatistics(instrument); 
	}
	
	@GetMapping("/statistics")
	public StatisticsInterface getAllStatistics() throws StatisticsNotFoundException {
		return tickService.getStatistics();
	}
}
