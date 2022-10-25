package com.covid.service;

import com.covid.dao.AuthorDao;
import com.covid.entity.*;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
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

    public void createAuthorCraph() {
        try {
            authorDao.createAuthorGraph();
        } catch (InvalidDataAccessResourceUsageException e) {

            System.out.println("已经创建过了");
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println("已经创建过了");
        }
    }

    public Map<String, Double> toSimilarity(TypeSystem ignored, org.neo4j.driver.Record record) {

        Map<String, Double> map = new HashMap<String, Double>(2);

        Double score = record.get(2).asDouble();
        map.put(record.get(0).asString() + " " + record.get(1).asString(), score);
        return map;
    }

    public List<Map> node_similarity() {
        createAuthorCraph();
        this.neo4jClient
                .query("CALL gds.nodeSimilarity.write.estimate('authors', { " +
                        " writeRelationshipType: 'SIMILAR', " +
                        " writeProperty: 'score' })" +
                        " YIELD nodeCount, relationshipCount, bytesMin, bytesMax, requiredMemory"
                )
                .in(database());
        Collection<Map> result = this.neo4jClient
                .query("CALL gds.nodeSimilarity.stream('authors') " +
                        " YIELD node1, node2, similarity " +
                        " RETURN gds.util.asNode(node1).name AS Person1, gds.util.asNode(node2).name AS Person2, similarity " +
                        "ORDER BY similarity DESCENDING, Person1, Person2 " +
                        "Limit 10"
                )
                .in(database())
                .fetchAs(Map.class)
                .mappedBy(this::toSimilarity)
                .all();
        return (List<Map>) result;
    }

    public List toDijkstraPath(TypeSystem ignored, org.neo4j.driver.Record record) {
        Value path = record.get("nodeNames");

        return path.asList();
    }

    public List<List> DijkstraPath(String name) {
        createAuthorCraph();
        Collection<List> result = this.neo4jClient
                .query("MATCH (source:Author {name: $name}) " +
                        "CALL gds.allShortestPaths.dijkstra.stream('authors', {  sourceNode: source }) " +
                        "YIELD index, sourceNode, targetNode, totalCost, nodeIds, costs, path " +
                        "WHERE gds.util.asNode(sourceNode).name <> gds.util.asNode(targetNode).name " +
                        "RETURN index, gds.util.asNode(sourceNode).name AS sourceNodeName," +
                        "gds.util.asNode(targetNode).name AS targetNodeName, totalCost," +
                        "[nodeId IN nodeIds | gds.util.asNode(nodeId).name] AS nodeNames, costs, nodes(path) as path " +
                        "ORDER BY index"
                )
                .in(database())
                .bindAll(Map.of("name", name))
                .fetchAs(List.class)
                .mappedBy(this::toDijkstraPath)
                .all();

        return (List<List>) result;
    }
    //最短路径
//    public PaperDetail findMinPath(String name1, String name) {
//        return this.neo4jClient
//                .query("" +
//                        "MATCH (paper:Paper {title: $title}) " +
//                        "OPTIONAL MATCH (author:Author)-[r]->(paper) " +
//                        "WITH paper, COLLECT({ name: author.name, role: HEAD(r.roles) }) as cast " +
//                        "RETURN paper { .title, cast: cast }"
//                )
//                .in(database())
//                .bindAll(Map.of("title", title))
//                .fetchAs(PaperDetail.class)
//                .mappedBy(this::toPaperDetails)
//                .one()
//                .orElse(null);
//    }

}
