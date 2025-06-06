package com.flavor.forge;

import com.flavor.forge.Service.CommentService;
import com.flavor.forge.Service.RecipeService;
import com.flavor.forge.Service.UserService;
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
	void userServiceLoads() throws Exception {
		assertThat(userService).isNotNull();
	}

}
