package com.tick.tracker.web;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.MessageDTO.StatisticsInterface;
import com.tick.tracker.MessageDTO.StatisticsNotFoundException;
import com.tick.tracker.reactive_stream.TickSupplier;
import com.tick.tracker.statistics.TickStatisticsDataStructure;


/**
 * 
 * This Service interacts with 2 different components
 *  1. tickSupplier     :  to send data into reactive streams, only to be consumed by statistics component
 *  2. masterStatistics :  reference to master statistics component to display overall statistics
 *  3. instrumentMap    :  reference to map of all instrument statistics
 *  
 *   
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
@Service
public class TickService {

	/**
	 * reference to masterStatistics component that holds master statistics of all ticks
	 */
	@Autowired()
	TickStatisticsDataStructure masterStatistics;
	
	/**
	 * reference to instrumentMap that holds Entity where
	 *  key is the instrument name
	 *  value is  @TickStatisticsDataStructure
	 */
	
	@Autowired
	ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap;
	
	/**
	 * 
	 */
	@Autowired
	TickSupplier<ReceivedTick> tickSupplier;
	
	/**
	 * This method returns master overall statistics from statistics component.
	 * @return
	 * @throws StatisticsNotFoundException
	 */
	public StatisticsInterface getStatistics() throws StatisticsNotFoundException {
		return getSnapshot(masterStatistics); 
		
	}
	
	/**
	 * This method returns interface specific statistics from statistics component.
	 * 
	 * @return
	 * @throws StatisticsNotFoundException
	 */
	public  StatisticsInterface getStatistics(String instrument)  throws StatisticsNotFoundException {
		TickStatisticsDataStructure instrumentTickMap = instrumentMap.get(instrument);
		return getSnapshot(instrumentTickMap);
		}
	
	/**
	 * This method returns an immutable class that holds data of type @StatisticsInterface  
	 * 
	 * @param instrumentTickMap
	 * @return
	 * @throws StatisticsNotFoundException 
	 */
	private StatisticsInterface getSnapshot(TickStatisticsDataStructure instrumentTickMap) throws StatisticsNotFoundException {
		if(instrumentTickMap==null || instrumentTickMap.getCount()==0) {
			 throw new StatisticsNotFoundException();
		 }else {
			 return instrumentTickMap.getSnapshot() ;
		 }
	}
	/**
	 * This is the only method where Ticks can be sent into the reactive_stream component.
	 * 
	 * @param tick
	 * @throws InterruptedException
	 */
	public void consumeTick(ReceivedTick tick) throws InterruptedException {
		tickSupplier.insertValue(tick);
	}
}
