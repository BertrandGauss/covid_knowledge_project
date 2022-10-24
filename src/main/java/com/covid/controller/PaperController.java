package com.covid.controller;

import com.covid.entity.PaperDetail;
import com.covid.entity.PaperView;
import com.covid.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/getDetail")
    PaperDetail getDetail(@RequestParam("title") String title) {
        return paperService.fetchDetailsByTitle((title));
    }

    @GetMapping("/createGDS")
    void createGDS(){
        paperService.createGDS();
    }
    @GetMapping("/pageRank")
    List<Map> pageRank(){
        return paperService.pageRank();

    }
}
