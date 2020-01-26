/**
 * 
 */
package com.tick.tracker.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.tick.tracker.MessageDTO.StatisticsNotFoundException;
import com.tick.tracker.web.TickController;
import com.tick.tracker.web.TickService;

/**
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
@WebMvcTest(controllers = TickController.class)
public class TickControllerForNoContentTest {

	@MockBean 
    public TickService tickService;
	
	
	@InjectMocks
	public TickController tickController;
	
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	public WebApplicationContext webApplicationContext;
	
	@BeforeEach
	public void init() throws StatisticsNotFoundException {
		 mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	     MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testAllStatisticsNotFound() throws Exception {		
		when(tickService.getStatistics()).thenThrow(new StatisticsNotFoundException()); 
		mockMvc.perform(get("/statistics")).andExpect(status().isNoContent());

	}
	@Test
	public void testForInstrumentStatisticsNotFound() throws Exception {		
		when(tickService.getStatistics("DummyInstrument")).thenThrow(new StatisticsNotFoundException());
		mockMvc.perform(get("/statistics/DummyInstrument")).andExpect(status().isNoContent());

	}
}
