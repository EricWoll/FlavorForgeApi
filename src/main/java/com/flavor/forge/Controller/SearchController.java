package com.flavor.forge.Controller;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.Request.ListSearchRequest;
import com.flavor.forge.Model.User;
import com.flavor.forge.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/recipes")
    public ResponseEntity<List<Recipe>> defaultSearchInRecipes(
            @RequestParam(name="pageAmount", required = true) int pageAmount
    ) {
        return new ResponseEntity<List<Recipe>>(
                searchService.defaultSearchInRecipes(pageAmount),
                HttpStatus.OK
        );
    }

    @GetMapping("/recipes/{search_string}")
    public ResponseEntity<List<Recipe>> searchByStringInRecipe(
            @PathVariable String search_string,
            @RequestBody ListSearchRequest payload
    ) {
        return new ResponseEntity<List<Recipe>>(
                searchService.searchByStringInRecipe(search_string,
                        payload
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/creators/{search_string}")
    public ResponseEntity<List<User>> searchByStringInUser(@PathVariable String search_string) {
        return new ResponseEntity<List<User>>(
                searchService.searchByStringInUser(search_string),
                HttpStatus.OK
        );
    }
}
