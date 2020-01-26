package com.tick.tracker.statistics;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.tick.tracker.MessageDTO.StatisticsInterface;
import com.tick.tracker.MessageDTO.StatisticsModel;

/**
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 * 
 * 
 * @TickStatisticsDataStructure is implemented on top of ConcurrentSkipListMap
 * 
 * 1. As Used ConcurrentSkipListMap as first() and last() has O(1) time complexity
 * 
 * 2. To have an optimal Memory usage I have used the following data structure:
 *       ConcurrentSkipListMap<Double , AtomicInteger >  
 *       where 
 *       - Double is the price  
 *       - AtomicInteger is the number of occurrence, it is a counter variable, utilizing the memory when the load is too high.
 *        
 * 3. This component only takes care of generating statistics. 
 * 4. This component only takes INSTRUMENT and PRICE, as expiry time is taken care by reactive_stream component
 * 5. If two prices are sent into this component for the same Instrument, instead of adding it in the map, it only increment its counter.
 *    
 * instead of saving every price in the store, I choose this  ConcurrentSkipListMap
 * 
 * example of data input :
 *         
 *         [ 1.2, 1.3, 1.1, 1.4, 1.1, 1.2, 1.4, 1.2, 1.1] 
 * 
 * The above stream could be represented as follows in @TickStatisticsDataStructure :
 * 
 *        { 1.1, 3}, { 1.2, 3}, { 1.3, 1}, { 1.4, 2}
 * 
 * 
 *
 */
public class TickStatisticsDataStructure extends ConcurrentSkipListMap<Double, AtomicInteger>
		implements StatisticsInterface, Serializable { 

	private static final long serialVersionUID = 1L;
	private AtomicReference<Double> sumOfActiveTicks;
	/**
	 * This is the counter of Double item.
	 * 
	 * 
	 */
	
	private AtomicInteger doubleCounter;
	

	public TickStatisticsDataStructure() {
		super();
		sumOfActiveTicks = new AtomicReference<Double>(Double.valueOf(0.0));
		doubleCounter=new AtomicInteger(0);
	}

	private double addPrice(double delta) {
		while (true) {
			Double currentValue = sumOfActiveTicks.get();
			Double newValue = Double.valueOf(currentValue.doubleValue() + delta);
			if (sumOfActiveTicks.compareAndSet(currentValue, newValue)) {
				return currentValue.doubleValue();
			}
		}
		
	}

	public void checkin(Double value) {
		addPrice(value);
		doubleCounter.getAndIncrement();
		AtomicInteger count = this.getOrDefault(value, new AtomicInteger(0));
		this.putIfAbsent(value, count);
		count.incrementAndGet();		
	}

	public void checkout(Double value) {
		addPrice(-value);
		doubleCounter.decrementAndGet();
		this.computeIfPresent(value, (k, v) -> v.decrementAndGet() == 0 ? null : v);
		}

	public StatisticsModel getSnapshot() {
		return new StatisticsModel(doubleCounter.get(), this.getMinPrice(), this.getMaxPrice(), this.getAverage());
	} 
	@Override
	public int getCount() {
		return doubleCounter.get();
	}

	@Override
	public double getMinPrice() {
		return this.firstKey().doubleValue();
	}

	@Override
	public double getMaxPrice() {
		return this.lastKey().doubleValue();
	}

	@Override
	public double getAverage() {
		return this.getCount() == 0 ? 0 : (sumOfActiveTicks.get() / this.getCount());
	}
}
