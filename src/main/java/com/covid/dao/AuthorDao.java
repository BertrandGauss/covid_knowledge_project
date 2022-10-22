package com.covid.dao;

import com.covid.entity.Author;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface AuthorDao extends Repository<Author, String> {

    @Query("MATCH (author:Author) WHERE author.name CONTAINS $name RETURN author")
    List<Author> findSearchResults(@Param("name") String name);
}
