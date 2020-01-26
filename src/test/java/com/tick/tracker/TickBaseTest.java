package com.tick.tracker;


import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class TickBaseTest {  
	@Autowired
	public WebApplicationContext webApplicationContext;

	public MockMvc mockMvc;
    
	/**
	 * start up and fire-up MockMvcBuilders builder before tests.
	 */
	@BeforeEach
	public void setup()  {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	

}
