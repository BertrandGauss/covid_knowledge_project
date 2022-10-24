package com.covid.dao;

import com.covid.entity.Author;
import com.covid.entity.Paper;
import com.covid.entity.PaperDetail;
import org.neo4j.driver.Record;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface PaperDao extends Repository<Paper, String> {

    @Query("MATCH (paper:Paper) WHERE paper.title CONTAINS $title RETURN paper")
    List<Paper> findSearchResults(@Param("title") String title);

    @Query(value = "MATCH (p:Paper)<-[:write]-(author:Author) RETURN p.title as title, collect(a) as cast LIMIT $limit")
    List<PaperDetail> graph(@Param("limit") Integer limit);
}

