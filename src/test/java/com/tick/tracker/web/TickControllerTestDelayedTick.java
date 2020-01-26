/**
 * 
 */
package com.tick.tracker.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;


import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick.tracker.TickBaseTest;
import com.tick.tracker.Constants;
import com.tick.tracker.MessageDTO.ReceivedTick;

/**
 * Checks expired ticks
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
public class TickControllerTestDelayedTick extends TickBaseTest{ 

	/**
	 * Check if a '/ticks' returns 204 "No Content" when tick with expired time is submitted.
	 * @throws Exception
	 */
	@Test
	public void testOldTickForNoContent() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		ReceivedTick tickModel = new ReceivedTick("IBM", 100.90, new Date().getTime()- Constants.Threading.DELAY_WINDOW);
		mockMvc
		.perform(
					post("/ticks")
					.contentType("application/json")
					.content(mapper.writeValueAsString(tickModel)))
		.andExpect(status().isNoContent());
	}
	
		
}
