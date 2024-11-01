package com.flavor.forge.Controller;

import com.flavor.forge.Model.Recipe;
import com.flavor.forge.Model.User;
import com.flavor.forge.Service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/recipes/{search_string}")
    public ResponseEntity<List<Recipe>> searchByStringInRecipe(
            @PathVariable String search_string,
            @RequestBody(required = false) List<String> payload
    ) {
        return new ResponseEntity<List<Recipe>>(
                searchService.searchByStringInRecipe(search_string,
                        payload != null ? payload : new ArrayList<String>()
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
