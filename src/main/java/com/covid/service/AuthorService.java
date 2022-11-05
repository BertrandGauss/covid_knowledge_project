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

    private Map<String, Object> toD3Format(Collection<AuthorDetail> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        List<AuthorDetail> res = (List<AuthorDetail>) result;
        int i = 0;
        for (int j = 0; j < result.size(); j++) {
            AuthorDetail row = ((List<AuthorDetail>) result).get(j);
            nodes.add(map("name", row.getName(), "label", "author"));
            int source = i;
            i++;
            for (Paper a : row.getCast()) {
                Map<String, Object> paper = map("title", a.getTitle(), "label", "paper");
                int target = nodes.indexOf(paper);
                if (target == -1) {
                    nodes.add(paper);
                    target = i++;
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

    private AuthorCooperators toAuthorCooperator(TypeSystem ignored, org.neo4j.driver.Record record) {
        return new AuthorCooperators(
                record.get("author.name").asString(),
                record.get("y.name").asString(),
                record.get("paper.title").asString()
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

    //论文作者网络力导向图
    public Map<String, Object> graph(int limit) {
        Collection<AuthorDetail> result = this.neo4jClient
                .query("MATCH (paper:Paper)<-[:write]-(author:Author) " +
                        "WITH author, collect(paper) as cast " +
                        "RETURN author { .name, cast: cast }" +
                        "LIMIT $limit"
                )
                .in(database())
                .bindAll(Map.of("limit", limit))
                .fetchAs(AuthorDetail.class)
                .mappedBy(this::toAuthorDetails)
                .all();

        return toD3Format(result);
    }

    private Map<String, Object> toD3FormatNew(Collection<AuthorCooperators> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        List<AuthorCooperators> res = (List<AuthorCooperators>) result;
        int i = 0;
        for (int j = 0; j < result.size(); j++) {
            AuthorCooperators row = ((List<AuthorCooperators>) result).get(j);
            if (j == 0) {
                nodes.add(map("name", row.getName(), "label", "author"));
            }
            Map<String, Object> paper = map("title", row.getPaper(), "label", "paper");
            Map<String, Object> cooperator = map("name", row.getCooperator(), "label", "cooperator");
            int target = nodes.indexOf(paper);
            int source = nodes.indexOf(cooperator);
            if (target == -1) {
                nodes.add(paper);
                target = ++i;
                rels.add(map("source", 0, "target", target));
            }
            if (source == -1) {
                nodes.add(cooperator);
                source = ++i;
            }
            rels.add(map("source", source, "target", target));

        }
        return map("nodes", nodes, "links", rels);
    }

    public Map<String, Object> findCooperators(String name) {
        Collection<AuthorCooperators> result = this.neo4jClient
                .query("" +
                        "MATCH (author:Author{name:$name })-[:write]->(paper:Paper)<-[:write]-(y:Author) " +
                        "WHERE author.name <> y.name " +
                        "RETURN author.name, y.name,  paper.title"
                )
                .in(database())
                .bindAll(Map.of("name", name))
                .fetchAs(AuthorCooperators.class)
                .mappedBy(this::toAuthorCooperator)
                .all();
        return toD3FormatNew(result);

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

    public void createminAuthorCraph() {
        try {
            authorDao.createminAuthorGraph();
        } catch (InvalidDataAccessResourceUsageException e) {

            System.out.println("已经创建过了");
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println("已经创建过了");
        }
    }

    public NodeSimilarity toSimilarity(TypeSystem ignored, org.neo4j.driver.Record record) {

        return new NodeSimilarity(record.get(0).asString(), record.get(1).asString(), record.get(2).asDouble());
    }

    public List<NodeSimilarity> node_similarity() {
        createminAuthorCraph();
        this.neo4jClient
                .query("CALL gds.nodeSimilarity.write.estimate('min_authors', { " +
                        " writeRelationshipType: 'SIMILAR', " +
                        " writeProperty: 'score' })" +
                        " YIELD nodeCount, relationshipCount, bytesMin, bytesMax, requiredMemory"
                )
                .in(database());
        Collection<NodeSimilarity> result = this.neo4jClient
                .query("CALL gds.nodeSimilarity.stream('min_authors') " +
                        " YIELD node1, node2, similarity " +
                        " RETURN gds.util.asNode(node1).name AS Person1, gds.util.asNode(node2).name AS Person2, similarity " +
                        "ORDER BY similarity DESCENDING, Person1, Person2"
                )
                .in(database())
                .fetchAs(NodeSimilarity.class)
                .mappedBy(this::toSimilarity)
                .all();
        return (List<NodeSimilarity>) result;
    }

    public DijkstraPath toDijkstraPath(TypeSystem ignored, org.neo4j.driver.Record record) {

        return new DijkstraPath(record.get("sourceNodeName").asString(),
                record.get("targetNodeName").asString(),
                record.get("totalCost").asDouble(),
                record.get("nodeNames").asList((member) -> {
                    String result = member.asString();
                    return result;
                }),
                record.get("costs").asList((member) -> {
                    Double result = member.asDouble();
                    return result;
                }));
    }


    public Map<String, Object> toD3FormatGraph(Collection<DijkstraPath> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        List<DijkstraPath> res = (List<DijkstraPath>) result;
        int i = 0;
        for (int j = 0; j < result.size(); j++) {
            DijkstraPath row = ((List<DijkstraPath>) result).get(j);
            List<String> nodeNames = row.getNodeNames();
            for (int k = 0; k < nodeNames.size() - 1; k++) {
                Map<String, Object> author1 = map("name", nodeNames.get(k), "label", "author");
                Map<String, Object> author2 = map("name", nodeNames.get(k + 1), "label", "author");
                int source = nodes.indexOf(author1);
                int target = nodes.indexOf(author2);
                if (source == -1) {
                    nodes.add(author1);
                    source = i++;
                }
                if (target == -1) {
                    nodes.add(author2);
                    target = i++;
                }
                Map<String, Object> rel = map("source", source, "target", target);
                int r = rels.indexOf(rel);
                if (r == -1) {
                    rels.add(rel);
                }
            }

        }
        return map("nodes", nodes, "links", rels);
    }

    public List<DijkstraPath> DijkstraPath(String name) {
        createAuthorCraph();
        Collection<DijkstraPath> result = this.neo4jClient
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
                .fetchAs(DijkstraPath.class)
                .mappedBy(this::toDijkstraPath)
                .all();

        return (List<DijkstraPath>) result;
    }


}
