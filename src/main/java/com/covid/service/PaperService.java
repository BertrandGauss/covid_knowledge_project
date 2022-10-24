package com.covid.service;

import com.covid.dao.PaperDao;

import com.covid.entity.Author;
import com.covid.entity.PaperDetail;
import org.neo4j.driver.Driver;
import com.covid.entity.AuthorView;
import com.covid.entity.PaperView;
import org.neo4j.driver.*;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.neo4j.driver.Session;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaperService {
    @Autowired
    private  PaperDao paperDao;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private  Driver driver;
    @Autowired
    private  DatabaseSelectionProvider databaseSelectionProvider;

    //通过标题部分查找相关标题
    public List<PaperView> searchPaperByTitle(String title) {

        return paperDao.findSearchResults(title)
                .stream()
                .map(PaperView::new)
                .collect(Collectors.toList());
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String,Object>(2);
        result.put(key1,value1);
        result.put(key2,value2);
        return result;
    }
    private Map<String, Object> toD3Format(Iterator<Record> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 0;
        while (result.hasNext()) {
            System.out.println(i);
            Record row = result.next();
            nodes.add(map("title", row.get("author"), "label", "author"));
            int target = i;
            i++;
            for (Object name : (Collection) row.get("cast")) {
                Map<String, Object> actor = map("title", name, "label", "author");
                int source = nodes.indexOf(actor);
                if (source == -1) {
                    nodes.add(actor);
                    source = i++;
                }
                rels.add(map("source", source, "target", target));
            }
        }
        return map("nodes", nodes, "links", rels);
    }

    //获得论文力导向图
    public Map<String, Object> graph(int limit) {
        System.out.println(paperDao.graph(limit));
        List<Record> result = paperDao.graph(limit);
        return toD3Format(result.iterator());
    }


    private Session sessionFor(String database) {
        if (database == null) {
            return driver.session();
        }
        return driver.session(SessionConfig.forDatabase(database));
    }

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }

    private PaperDetail toPaperDetails(TypeSystem ignored, org.neo4j.driver.Record record) {
        Value paper = record.get("paper");
        return new PaperDetail(
                paper.get("title").asString(),
                paper.get("cast").asList((member) -> {
                    Author result = new Author(
                            member.get("name").asString()
                    );
                    return result;

                })
        );
    }
   //根据论文题目查找作者
    public PaperDetail fetchDetailsByTitle(String title) {
        return this.neo4jClient
                .query("" +
                        "MATCH (paper:Paper {title: $title}) " +
                        "OPTIONAL MATCH (author:Author)-[r]->(paper) " +
                        "WITH paper, COLLECT({ name: author.name, role: HEAD(r.roles) }) as cast " +
                        "RETURN paper { .title, cast: cast }"
                )
                .in(database())
                .bindAll(Map.of("title", title))
                .fetchAs(PaperDetail.class)
                .mappedBy(this::toPaperDetails)
                .one()
                .orElse(null);
    }


}
