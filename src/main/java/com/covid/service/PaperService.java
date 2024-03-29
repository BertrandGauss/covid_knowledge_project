package com.covid.service;

import com.covid.dao.PaperDao;
import com.covid.entity.Author;
import com.covid.entity.PageRank;
import com.covid.entity.PaperDetail;
import com.covid.entity.PaperView;
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
public class PaperService {
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private Neo4jClient neo4jClient;
    @Autowired
    private Driver driver;
    @Autowired
    private DatabaseSelectionProvider databaseSelectionProvider;

    //通过标题部分查找相关标题
    public List<PaperView> searchPaperByTitle(String title) {

        return paperDao.findSearchResults(title)
                .stream()
                .map(PaperView::new)
                .collect(Collectors.toList());
    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put(key1, value1);
        result.put(key2, value2);
        return result;
    }

    private Map<String, Object> toD3Format(Collection<PaperDetail> result) {
        List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rels = new ArrayList<Map<String, Object>>();
        List<PaperDetail> res = (List<PaperDetail>) result;
        int i = 0;
        for (int j = 0; j < result.size(); j++) {
            PaperDetail row = ((List<PaperDetail>) result).get(j);
            nodes.add(map("title", row.getTitle(), "label", "paper"));
            int target = i;
            i++;
            for (Author a : row.getCast()) {
                Map<String, Object> author = map("name", a.getName(), "label", "author");
                int source = nodes.indexOf(author);
                if (source == -1) {
                    nodes.add(author);
                    source = i++;
                }
                rels.add(map("source", source, "target", target));
            }
        }
        return map("nodes", nodes, "links", rels);
    }

    //获得论文力导向图
    public Map<String, Object> graph(int limit) {
        Collection<PaperDetail> result = this.neo4jClient
                .query("MATCH (paper:Paper)<-[:write]-(author:Author) " +
                        "WITH paper, collect(author) as cast " +
                        "RETURN paper { .title, cast: cast }" +
                        "LIMIT $limit"
                )
                .in(database())
                .bindAll(Map.of("limit", limit))
                .fetchAs(PaperDetail.class)
                .mappedBy(this::toPaperDetails)
                .all();
        return toD3Format(result);
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

    //创建gds图
    public void createGDS() {
        try {
            paperDao.createGds();

        } catch (InvalidDataAccessResourceUsageException e) {

            System.out.println("已经创建过了");
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println("已经创建过了");
        }
    }

    public PageRank toScore(TypeSystem ignored, org.neo4j.driver.Record record) {

        return new PageRank(record.get(0).asString(), record.get(1).asDouble());

    }

    public List<PageRank> pageRank() {
        createGDS();
        this.neo4jClient
                .query("CALL gds.pageRank.write.estimate('authors_and_papers', {" +
                        "  writeProperty: 'pageRank'," +
                        "  maxIterations: 20," +
                        "  dampingFactor: 0.85" +
                        "})" +
                        "YIELD nodeCount, relationshipCount, bytesMin, bytesMax, requiredMemory"
                )
                .in(database());
        Collection<PageRank> result = this.neo4jClient
                .query("CALL gds.pageRank.stream('authors_and_papers') " +
                        "YIELD nodeId, score " +
                        "RETURN gds.util.asNode(nodeId).title AS title, score " +
                        "ORDER BY score DESC, title ASC " +
                        "Limit 1000"
                )
                .in(database())
                .fetchAs(PageRank.class)
                .mappedBy(this::toScore)
                .all();
        //销毁图，防止占用内存

        return (List<PageRank>) result;
    }


}
