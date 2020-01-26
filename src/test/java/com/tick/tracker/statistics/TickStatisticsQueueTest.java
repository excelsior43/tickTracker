package com.tick.tracker.statistics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tick.tracker.statistics.TickStatisticsDataStructure;

class TickStatisticsQueueTest {
	TickStatisticsDataStructure map;
	@BeforeEach
	void before() {
		map=new TickStatisticsDataStructure();
	}
	
	@Test
	void testStatisticsQueue() {
		double[] testValues=new double[]{ 1.1, 2.2, 3.3, 4.4, 5.5, 9.0, 7.7, 8.8, 11.0, 1.0};  
		for (double unitValue : testValues) {
			map.checkin(Double.valueOf(unitValue));
		}	
		assertTrue(map.getMinPrice()==1.0);
		assertTrue(map.getMaxPrice()==11.0);
		assertTrue(map.getCount()==10);
		assertTrue(map.getAverage()==5.4);	
		for (double unitValue : testValues) {
			map.checkout(Double.valueOf(unitValue));
		}
		assertTrue(map.isEmpty());
	}
}
