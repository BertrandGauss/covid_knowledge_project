package com.covid.controller;

import com.covid.entity.PageRank;
import com.covid.entity.PaperDetail;
import com.covid.entity.PaperView;
import com.covid.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/paper")
public class PaperController {
    @Autowired
    private PaperService paperService;


    @RequestMapping("/graph")
    public Map<String, Object> graph(@RequestParam(value = "limit", required = false) Integer limit) {
        System.out.println(limit == null ? 100 : limit);
        return paperService.graph(limit == null ? 100 : limit);
    }

    @GetMapping("/search")
    List<PaperView> search(@RequestParam("title") String title) {
        return paperService.searchPaperByTitle((title));
    }

    @RequestMapping("/getDetail/{title}")
    PaperDetail getDetail(@PathVariable("title") String title) {
        System.out.println(title);
        return paperService.fetchDetailsByTitle((title));
    }


    @GetMapping("/pageRank")
    List<PageRank> pageRank() {
        return paperService.pageRank();

    }
}
