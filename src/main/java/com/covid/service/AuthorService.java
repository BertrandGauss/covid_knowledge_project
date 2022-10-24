package com.covid.service;

import com.covid.dao.AuthorDao;
import com.covid.entity.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class AuthorService {
    @Autowired
    private AuthorDao authorDao;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private Driver driver;
    @Autowired
    private DatabaseSelectionProvider databaseSelectionProvider;

    //通过部分名字查找包含该名字的作者
    public List<AuthorView> searchAuthorsByName(String name) {
        return authorDao.findSearchResults(name)
                .stream()
                .map(AuthorView::new)
                .collect(Collectors.toList());
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    private Map<String, Object> toD3Format(Iterator<Map<String, Object>> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        int i = 0;
        while (result.hasNext()) {
            System.out.println(i);
            Map<String, Object> row = result.next();
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


    private AuthorDetail toAuthorDetails(TypeSystem ignored, org.neo4j.driver.Record record) {
        Value author = record.get("author");
        return new AuthorDetail(
                author.get("name").asString(),
                author.get("cast").asList((member) -> {
                    Paper result = new Paper(
                            member.get("title").asString()
                    );
                    return result;

                })
        );
    }

    private AuthorCooperators toAuthorCooperators(TypeSystem ignored, org.neo4j.driver.Record record) {
        Value author = record.get("author");
        return new AuthorCooperators(
                author.get("name").asString(),
                author.get("cooperators").asList((member) -> {
                    Author result = new Author(
                            member.get("name").asString()
                    );
                    return result;

                })
        );
    }

    private String database() {
        return databaseSelectionProvider.getDatabaseSelection().getValue();
    }

    //根据作者查找他写的文章
    public AuthorDetail fetchDetailsByName(String name) {
        return this.neo4jClient
                .query("" +
                        "MATCH (author:Author {name: $name}) " +
                        "OPTIONAL MATCH (author)-[r]->(paper:Paper) " +
                        "WITH author, COLLECT({ title: paper.title, role: HEAD(r.roles) }) as cast " +
                        "RETURN author { .name, cast: cast }"
                )
                .in(database())
                .bindAll(Map.of("name", name))
                .fetchAs(AuthorDetail.class)
                .mappedBy(this::toAuthorDetails)
                .one()
                .orElse(null);
    }

    public AuthorCooperators findCooperators(String name) {
        return this.neo4jClient
                .query("" +
                        "MATCH (author:Author{name:$name })-[:write*2]-(y:Author) " +
                        "WHERE author.name <> y.name " +
                        "WITH author, COLLECT({ name: y.name }) as cooperators " +
                        "RETURN author { .name, cooperators: cooperators }"
                )
                .in(database())
                .bindAll(Map.of("name", name))
                .fetchAs(AuthorCooperators.class)
                .mappedBy(this::toAuthorCooperators)
                .one()
                .orElse(null);
    }


}
