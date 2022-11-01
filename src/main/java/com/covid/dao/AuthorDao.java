package com.covid.dao;

import com.covid.entity.Author;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthorDao extends Repository<Author, String> {

    @Query("MATCH (author:Author) WHERE author.name CONTAINS $name RETURN author")
    List<Author> findSearchResults(@Param("name") String name);

    @Query("CALL gds.graph.project.cypher(\n" +
            "  'authors',\n" +
            "  'MATCH (a:Author ) RETURN id(a) AS id',\n" +
            "  'MATCH (x:Author)-[r:write*2]-(y:Author) WHERE x.name <> y.name WITH x, r,y LIMIT 1000000 RETURN id(x) as target , id(y) as source, \"cooperate\" as type,   COUNT(*) AS cooperate_times')\n" +
            "YIELD graphName AS graph, nodeQuery, nodeCount AS nodes, relationshipQuery, relationshipCount AS rels")
    void createAuthorGraph();

}
