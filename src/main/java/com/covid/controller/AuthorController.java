package com.covid.controller;

import com.covid.entity.AuthorDetail;
import com.covid.entity.AuthorView;
import com.covid.entity.PaperView;
import com.covid.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/author")
public class AuthorController {
    @Autowired
    private AuthorService authorService;
    @GetMapping("/search")
    List<AuthorView> search(@RequestParam("name") String name) {
        return authorService.searchAuthorsByName((name));
    }

    @RequestMapping("/getDetail")
    AuthorDetail getDetail(@RequestParam("name") String name) {
        return authorService.fetchDetailsByName((name));
    }

}
