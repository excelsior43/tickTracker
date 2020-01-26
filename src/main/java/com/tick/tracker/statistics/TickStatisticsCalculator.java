package com.tick.tracker.statistics;

import java.util.Optional; 
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 

import com.tick.tracker.MessageDTO.BasicTick;
import com.tick.tracker.MessageDTO.StatisticsCalculatorInterface;
import com.tick.tracker.MessageDTO.StatisticsInterface;
import com.tick.tracker.MessageDTO.Status;
/***
 * 
 * This is the main Statistics Calculator holds 2 objects
 *   1. The master Statistics object that serves to GET  /statistics requests and holds the master statistics
 *   2. Map of instruments that hold individual TickStatisticsMaps.
 * 
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
public class TickStatisticsCalculator implements StatisticsCalculatorInterface{
	
    private final static Logger log=LoggerFactory.getLogger("com.tick.tracker.statistics.TickStatisticsCalculator"); 

	private final TickStatisticsDataStructure masterCalculator;
	private final ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap;
	public TickStatisticsCalculator(TickStatisticsDataStructure masterCalculator, ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap) {
		super();
		this.masterCalculator = masterCalculator;
		this.instrumentMap = instrumentMap;
	}
	
	private void add(final String instrument, final Double value) {	
		TickStatisticsDataStructure tickStatisticsMap = instrumentMap.getOrDefault(instrument, new TickStatisticsDataStructure());
		masterCalculator.checkin(value);
		tickStatisticsMap.checkin(value);
		instrumentMap.putIfAbsent(instrument, tickStatisticsMap);
		
	}
	
	private void remove(final String instrument, final Double value) {
		masterCalculator.checkout(value);
		Optional
		.ofNullable(instrumentMap.getOrDefault(instrument, null))
		.ifPresent(x->x.checkout(value));
	}

	@Override
	public void consumeTick(final BasicTick tick) {
		if(tick.getStatus() == Status.RECEIVED) {
			this.add(tick.getInstrument(), tick.getPrice());
		}else {
			this.remove(tick.getInstrument(), tick.getPrice());
		}
		
	}

	@Override
	public StatisticsInterface getStatistics() {
		return masterCalculator;
	}

	@Override
	public StatisticsInterface getStatistics(String instrument) {
		log.debug("checkHealth() : "+this.checkHealth()); 
		return instrumentMap.get(instrument);
	}
	public boolean checkHealth() {
		return (instrumentMap.values().stream().mapToInt(e-> e.getCount()).sum() == masterCalculator.getCount());
	}
}
