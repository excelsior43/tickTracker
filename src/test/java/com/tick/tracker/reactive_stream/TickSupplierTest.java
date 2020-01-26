/**
 * 
 */
package com.tick.tracker.reactive_stream;

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.reactive_stream.TickSupplier;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */

public class TickSupplierTest {

	TickSupplier<ReceivedTick> supplier;

	@BeforeEach
	public void init() throws InterruptedException {
		supplier = new TickSupplier<ReceivedTick>();

	}

	@Test
	public void testSupplier2() throws InterruptedException {

		
			try { 
				supplier.insertValue(new ReceivedTick("WELLS", 0.001, new Date().getTime() + 5000));
				supplier.insertValue(new ReceivedTick("IBM", 11.89, new Date().getTime() + 99));
				supplier.insertValue(new ReceivedTick("DIAMOND", 0.001, new Date().getTime() + 0));
				supplier.insertValue(new ReceivedTick("SBI", 0.59, new Date().getTime() + 2000));
				supplier.insertValue(new ReceivedTick("DIAMOND", 0.001, new Date().getTime() + 6000));
				supplier.insertValue(new ReceivedTick("HDFC", 10.89, new Date().getTime() + 500));
				supplier.insertValue(new ReceivedTick("SUN", 0.001, new Date().getTime() + 5));
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Assertions.assertEquals(supplier.get().getInstrument(), "DIAMOND");
			Assertions.assertEquals(supplier.get().getInstrument(), "SUN");
			Assertions.assertEquals(supplier.get().getInstrument(), "IBM");
			Assertions.assertEquals(supplier.get().getInstrument(), "HDFC");
			Assertions.assertEquals(supplier.get().getInstrument(), "SBI");
			Assertions.assertEquals(supplier.get().getInstrument(), "WELLS");
			Assertions.assertEquals(supplier.get().getInstrument(), "DIAMOND");
}
	
	
	@Test
	void testTickSupplier() {
		Assertions.assertNotNull(new TickSupplier<String>());
	}

	@Test
	void testInsertValue() throws InterruptedException {
		TickSupplier<String> supplier=new TickSupplier<String>();
		supplier.insertValue("Some Dummy Value");
		Assertions.assertEquals(supplier.get(), "Some Dummy Value");;
		
	}

}
