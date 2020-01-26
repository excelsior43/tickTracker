package com.tick.tracker.statistics;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tick.tracker.MessageDTO.BasicTick;
import com.tick.tracker.MessageDTO.Status;
import com.tick.tracker.statistics.TickStatisticsCalculator;
import com.tick.tracker.statistics.TickStatisticsDataStructure;

class TickStatisticsComponentWithRandomData {
	private static int DATA_SET_SIZE=10000;
	private TickStatisticsCalculator calculator;
	private static int thread_count=10;
	private BlockingQueue<BasicTick> queue;
	private BasicTick corruptTick;

	@BeforeEach
	void before() {
		ConcurrentHashMap<String, TickStatisticsDataStructure> instrumentMap = new ConcurrentHashMap<String, TickStatisticsDataStructure>();
		this.calculator = new TickStatisticsCalculator(new TickStatisticsDataStructure(), instrumentMap);
		queue = new LinkedBlockingQueue<BasicTick>();
	}

	@Test
	void testAutomatedTicks() throws InterruptedException {
		
		ExecutorService generationExecutor = null;
		ExecutorService executor = null;
		try {
		    generationExecutor = Executors.newFixedThreadPool(thread_count);
			generationExecutor.execute(producer);
			Thread.sleep(1000);
			executor = Executors.newFixedThreadPool(thread_count);
			executor.execute(consumer);
			while (queue.size() >0) {
				Thread.sleep(10);
			}
			
		} finally {
			
			generationExecutor.shutdown();
			executor.shutdown();
			Assertions.assertEquals(calculator.getStatistics().getCount(), 0);
		}
		Assertions.assertTrue(calculator.checkHealth());
	}

	@Test
	void testAutomatedTicksWithException() throws InterruptedException {
		int corruptIndex = ThreadLocalRandom.current().nextInt(0, DATA_SET_SIZE + 1);
		AtomicInteger count=new AtomicInteger(0);
		ExecutorService generationExecutor = null;
		ExecutorService executor = null;
		try {
			generationExecutor = Executors.newFixedThreadPool(thread_count);
			generationExecutor.execute(producer);
			Thread.sleep(1000);
			executor = Executors.newFixedThreadPool(thread_count);
			executor.execute(()->{
				while (queue.size() >0 ) {
					try {
						BasicTick item = queue.take();
						if(count.getAndIncrement()==corruptIndex) {
							corruptTick= new BasicTick(item.getInstrument(), item.getPrice(), item.getStatus(), item.getTimestamp()); 
						}else {
							calculator.consumeTick(new BasicTick(item.getInstrument(), item.getPrice(), Status.ACTIVATED , item.getTimestamp()));
							
					
						}
					} catch (InterruptedException e) {

					}
				}
			});
			
			while (queue.size() >0) {
				Thread.sleep(1000);
			}
			
		} finally {
			
			generationExecutor.shutdown();
			executor.shutdown();
			Assertions.assertEquals(calculator.getStatistics().getMinPrice(), this.corruptTick.getPrice().doubleValue());
			Assertions.assertEquals(calculator.getStatistics().getMaxPrice(), this.corruptTick.getPrice().doubleValue());
			Assertions.assertEquals(calculator.getStatistics().getCount(), 1);
			
		}
	}
	

	private BasicTick getRandomBasicTick() {
		final String[] instruments = new String[] { "Google", "IBM", "SUN" };
		final int[] timestamp = new int[] { 10, 300, 600 };
		double rangeMin = 0.0;
		double rangeMax = 100.0;
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

		BasicTick tick = new BasicTick(instruments[((int) randomValue) % 3], Double.valueOf(randomValue),
				Status.RECEIVED, Long.valueOf(System.currentTimeMillis() + timestamp[((int) randomValue) % 3]));

		return tick;

	}

	
	
	private Runnable producer = new Runnable() {
			@Override
			public void run() {
				{
					while (queue.size() < DATA_SET_SIZE) {
						BasicTick tick = getRandomBasicTick();
						calculator.consumeTick(tick);
						queue.add(tick);
					}
				}
				
			}
			
		};
	private Runnable consumer= new Runnable() {
			@Override
			public void run() {
				while (queue.size() >0) {
					try {
						BasicTick item = queue.take();
						calculator.consumeTick(new BasicTick(item.getInstrument(), item.getPrice(), Status.ACTIVATED , item.getTimestamp()));
					} catch (InterruptedException e) {

					}
				}
			}
		};
}
