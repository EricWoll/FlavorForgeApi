package com.flavor.forge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ForgeApplicationTests {

	@Autowired
	private CommentService commentService;
	@Autowired
	private RecipeService recipeService;
	@Autowired
	private SearchService searchService;
	@Autowired
	private UserService userService;

	@Test
	void commentServiceLoads() throws Exception {
		assertThat(commentService).isNotNull();
	}
	@Test
	void RecipeServiceLoads() throws Exception {
		assertThat(recipeService).isNotNull();
	}
	@Test
	void searchServiceLoads() throws Exception {
		assertThat(searchService).isNotNull();
	}
	@Test
	void userSericeLoads() throws Exception {
		assertThat(userService).isNotNull();
	}

}
