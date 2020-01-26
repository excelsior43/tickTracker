package com.tick.tracker.statistics;

import java.io.File;
import java.nio.file.Files; 
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tick.tracker.MessageDTO.ActivatedTick; 
import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.MessageDTO.Status;
import com.tick.tracker.MessageDTO.TickModel;
import com.tick.tracker.statistics.TickStatisticsCalculator;
import com.tick.tracker.statistics.TickStatisticsDataStructure;

public class AverageStoreTest { 
	@Autowired
	TickStatisticsCalculator tickStatisticsCalculator;

	@BeforeEach
	void before() {  
		TickStatisticsDataStructure masterStatistics = new TickStatisticsDataStructure();
		ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap = new ConcurrentHashMap<String, TickStatisticsDataStructure>();
		tickStatisticsCalculator = new TickStatisticsCalculator(masterStatistics, instrumentMap);
	}

	@Test
	public void testStatistics() throws InterruptedException {
		loadDataFromCSV(getResourcePath("averages.csv"));
		Assertions.assertEquals(3, tickStatisticsCalculator.getStatistics().getCount());
		Assertions.assertEquals(337, tickStatisticsCalculator.getStatistics().getAverage());
		Assertions.assertEquals(1, tickStatisticsCalculator.getStatistics().getMinPrice());
		Assertions.assertEquals(1000, tickStatisticsCalculator.getStatistics().getMaxPrice());
		
		Assertions.assertEquals(2, tickStatisticsCalculator.getStatistics("IBM").getCount());
		Assertions.assertEquals(1, tickStatisticsCalculator.getStatistics("IBM").getMinPrice());
		Assertions.assertEquals(1000, tickStatisticsCalculator.getStatistics("IBM").getMaxPrice());
		Assertions.assertEquals(500.5, tickStatisticsCalculator.getStatistics("IBM").getAverage());


		Assertions.assertEquals(1, tickStatisticsCalculator.getStatistics("CNN").getCount());
		Assertions.assertEquals(10, tickStatisticsCalculator.getStatistics("CNN").getMinPrice());
		Assertions.assertEquals(10, tickStatisticsCalculator.getStatistics("CNN").getMaxPrice());
		Assertions.assertEquals(10, tickStatisticsCalculator.getStatistics("CNN").getAverage());
		
		Assertions.assertNull(tickStatisticsCalculator.getStatistics("SOMEDUMMYVALUE"));
		Assertions.assertNull(tickStatisticsCalculator.getStatistics("SUN")); 
		//Assertions.assertNull(tickStatisticsCalculator.getStatistics("WELLS")); 

		
				
	}
	

	/**
	 * @param path
	 */
	private String getResourcePath(String path) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(path).getFile());
		return file.getAbsolutePath();
		
	}

	private void loadDataFromCSV(String path) {
		try {
			List<TickModel> listOfData = Files.lines(Paths.get(path)).map((l) -> {
				String[] a = l.split(",");
				if(Status.valueOf(a[2]) == Status.RECEIVED) {
					return new ReceivedTick(new String(a[0]), Double.valueOf(a[1]).doubleValue(), Long.valueOf(a[3]).longValue());
				}else  {
					return new ActivatedTick(new String(a[0]), Double.valueOf(a[1]).doubleValue(), Long.valueOf(a[3]).longValue());
				}
			}).collect(Collectors.toList());
			
			listOfData.stream().forEach(tick -> {
				tickStatisticsCalculator.consumeTick(tick.getBasicTick());
				});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
