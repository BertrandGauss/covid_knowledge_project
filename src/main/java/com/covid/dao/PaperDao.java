package com.covid.dao;

import com.covid.entity.Paper;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaperDao extends Repository<Paper, String> {

    @Query("MATCH (paper:Paper) WHERE paper.title CONTAINS $title RETURN paper")
    List<Paper> findSearchResults(@Param("title") String title);

    @Query("CALL gds.graph.project( 'author', ['Author','Paper'],['write'])")
    void createGds();

    @Query("CALL gds.pageRank.write.estimate('authors_and_papers', {" +
            "  writeProperty: 'pageRank'," +
            "  maxIterations: 20," +
            "  dampingFactor: 0.85" +
            "})" +
            "YIELD nodeCount, relationshipCount, bytesMin, bytesMax, requiredMemory")
    void createPageRank();

}

