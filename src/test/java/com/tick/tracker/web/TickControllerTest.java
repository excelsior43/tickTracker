/**
 * 
 */
package com.tick.tracker.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick.tracker.TickBaseTest; 
import com.tick.tracker.MessageDTO.ReceivedTick;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
public class TickControllerTest extends TickBaseTest{

	ObjectMapper mapper;
	@BeforeEach
	void before() throws JsonProcessingException, Exception{
		mapper=new ObjectMapper();
		ReceivedTick tickModel = new ReceivedTick("IBM", 100.90, new Date().getTime()+3000);
		
		mockMvc
			.perform(post("/ticks")
			.contentType("application/json")
			.content(mapper.writeValueAsString(tickModel)))
			.andExpect(status().isOk());
		Thread.sleep(1000);
	}
	
	@Test
	public void testSuccessTickPerInstrument() throws Exception { 
		mockMvc.perform(get("/statistics/IBM"))
		.andExpect(status().isOk())
		.andExpect(content().contentType("application/json"))
		.andExpect(jsonPath("$.average").value(100.90))
		.andExpect(jsonPath("$.maxPrice").value(100.90))
		.andExpect(jsonPath("$.minPrice").value(100.90))
		.andExpect(jsonPath("$.count").value(1))
		
		;
		Thread.sleep(1000);
		
		mockMvc.perform(get("/statistics"))
		.andExpect(status().isOk())
		.andExpect(content().contentType("application/json"))
		.andExpect(jsonPath("$.average").value(100.90))
		.andExpect(jsonPath("$.maxPrice").value(100.90))
		.andExpect(jsonPath("$.minPrice").value(100.90))
		.andExpect(jsonPath("$.count").value(1));
	}
	
			
}
