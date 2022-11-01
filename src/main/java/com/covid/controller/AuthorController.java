package com.covid.controller;

import com.covid.entity.*;
import com.covid.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/author")
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @RequestMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        return authorService.graph(limit == null ? 50 : limit);
    }

    @GetMapping("/search")
    List<AuthorView> search(@RequestParam("name") String name) {
        return authorService.searchAuthorsByName((name));
    }

    @RequestMapping("/getDetail/{name}")
    AuthorDetail getDetail(@PathVariable("name") String name) {
        return authorService.fetchDetailsByName((name));
    }

    @RequestMapping("/findCooperator")
    Map<String, Object> findCooperator(@RequestParam("name") String name) {
        return authorService.findCooperators((name));
    }

    @GetMapping("/node_similarity")
    List<NodeSimilarity> node_similarity() {
        return authorService.node_similarity();
    }

    @RequestMapping("/DijkstraPath")
    List<DijkstraPath> DijkstraPath(@RequestParam("name") String name) {
        return authorService.DijkstraPath(name);
    }

    @RequestMapping("/DijkstraPathGraph")
    Map<String, Object> DijkstraPathGraph(@RequestParam("name") String name) {
        return authorService.toD3FormatGraph(authorService.DijkstraPath(name));
    }


}
