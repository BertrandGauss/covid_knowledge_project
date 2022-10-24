package com.covid.dao;

import com.covid.entity.Author;
import com.covid.entity.Paper;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Map;

public interface AuthorDao extends Repository<Author, String> {

    @Query("MATCH (author:Author) WHERE author.name CONTAINS $name RETURN author")
    List<Author> findSearchResults(@Param("name") String name);

}
