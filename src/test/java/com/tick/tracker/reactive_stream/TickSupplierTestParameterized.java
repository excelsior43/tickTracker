/**
 * 
 */
package com.tick.tracker.reactive_stream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tick.tracker.MessageDTO.ReceivedTick;
import com.tick.tracker.reactive_stream.TickSupplier;


/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
  
public class TickSupplierTestParameterized {   

	TickSupplier<ReceivedTick> supplier;
	
	@BeforeEach
	public void init()  {
		supplier=new TickSupplier<ReceivedTick>();
		
		}
	
	private void loadDataFromCSV(String path) {
		try {
			List<ReceivedTick> listOfData = Files.lines(Paths.get(path))
			.map((l) -> {
				String[] a = l.split(",");
				return new ReceivedTick(new String(a[0]), Double.valueOf(a[1]).doubleValue(), 
						new Date().getTime()+Long.valueOf(a[2]).longValue());
			})
			.collect(Collectors.toList());
			listOfData.stream().forEach(model-> {
				try {
					supplier.insertValue(model);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			
			
	
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testSupplierWithParameters() throws InterruptedException {  
		CompletableFuture.runAsync(() -> loadDataFromCSV( getResourcePath("stockWithDifferentTimestamps.csv")))
		.whenCompleteAsync((a,b)->Assertions.assertEquals(supplier.get().getInstrument(), "IBM"));		
	}
	
	/**
	 * @param path
	 */
	private String getResourcePath(String path) {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(path).getFile());
		return file.getAbsolutePath();
		
	}
	
}
