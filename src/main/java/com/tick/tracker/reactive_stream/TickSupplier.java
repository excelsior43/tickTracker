package com.tick.tracker.reactive_stream;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.tick.tracker.Constants;



/**
* The TickSupplier program implements a supplier that would take any tick
* and then arranges it in order from the pool of ticks that are waiting to be 
* consumed.
* 
* This is a first level arrangement that would help in ordering ticks
* based on submitted timestamp.
* 
*
* @author  sonu.yasir@gmail.com
* @version 1.0
* @since   2020-01-23
*/
public final class TickSupplier<T> implements Supplier<T> {

	private final BlockingQueue<T> queue;
	public TickSupplier() {
		this.queue=new PriorityBlockingQueue<T>();
	}
	
	/**
	 * 
	 * This method is the only way ticks are passed into reactive_stream and statistics modules.
	 * Enters a Tick into the Priority Queue and orders  it accordingly
	 * to be consumed by the publisher and then by subscriber. 
	 * 
	 * @param model
	 * @throws InterruptedException
	 */
	public void insertValue(T model) throws InterruptedException {
			queue.offer(model, Constants.Threading.DELAY_WINDOW, TimeUnit.MILLISECONDS); 
	}
	

	/**
	 * 
	 * This method is a used by executor threads to access ticks that are 
	 * available to be processed.
	 * 
	 * @param model
	 * @throws InterruptedException
	 */
	public T get() {
		try {
			T item = queue.take();
			return item;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
				
	} 
	
}
