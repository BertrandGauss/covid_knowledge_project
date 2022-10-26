package com.covid.controller;

import com.covid.entity.AuthorCooperators;
import com.covid.entity.AuthorDetail;
import com.covid.entity.AuthorView;
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
    @RequestMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        return authorService.graph(limit == null ? 100 : limit);
    }
    @GetMapping("/search")
    List<AuthorView> search(@RequestParam("name") String name) {
        return authorService.searchAuthorsByName((name));
    }

    @RequestMapping("/getDetail")
    AuthorDetail getDetail(@RequestParam("name") String name) {
        return authorService.fetchDetailsByName((name));
    }

    @RequestMapping("/findCooperator")
    AuthorCooperators findCooperator(@RequestParam("name") String name) {
        return authorService.findCooperators((name));
    }

    @GetMapping("/node_similarity")
    List<Map> node_similarity() {
        return authorService.node_similarity();
    }

    @RequestMapping("/DijkstraPath")
    List<List> DijkstraPath(@RequestParam("name") String name) {
        return authorService.DijkstraPath(name);
    }

}
