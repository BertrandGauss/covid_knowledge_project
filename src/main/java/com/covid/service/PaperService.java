package com.covid.service;

import com.covid.dao.PaperDao;
import com.covid.entity.PaperView;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaperService {
    private  PaperDao paperDao;

    //通过标题部分查找相关标题
    public List<PaperView> searchPaperByTitle(String title) {
        return paperDao.findSearchResults(title)
                .stream()
                .map(PaperView::new)
                .collect(Collectors.toList());
    }

}
