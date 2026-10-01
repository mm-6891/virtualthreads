package com.pildora.apps.virtualthreads;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.benchmark.reset-enabled=true")
@AutoConfigureMockMvc
class VirtualthreadsApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void benchmarkResetClearsCreditsAndRestartsIds() throws Exception {
		String credit = "{\"holder\":\"usuario1\",\"quantity\":1000.0}";

		mockMvc.perform(post("/api/credits")
				.contentType("application/json")
				.content(credit))
			.andExpect(status().isOk());

		mockMvc.perform(delete("/api/credits/_benchmark/reset"))
			.andExpect(status().isNoContent());

		String emptyCredits = mockMvc.perform(get("/api/credits"))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andReturn().getResponse().getContentAsString();
		assertEquals("[]", emptyCredits);

		String createdCredit = mockMvc.perform(post("/api/credits")
				.contentType("application/json")
				.content(credit))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		assertTrue(createdCredit.contains("\"id\":1"));
	}

}
