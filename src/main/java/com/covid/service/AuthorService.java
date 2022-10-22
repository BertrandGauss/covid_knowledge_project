package com.covid.service;

import com.covid.dao.AuthorDao;
import com.covid.dao.PaperDao;
import com.covid.entity.AuthorView;
import com.covid.entity.PaperView;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AuthorService {

    private AuthorDao authorDao;

    private  Neo4jClient neo4jClient;

    private  Driver driver;

    private  DatabaseSelectionProvider databaseSelectionProvider;
    //通过部分名字查找包含该名字的作者
    public List<AuthorView> searchAuthorsByName(String name) {
        return authorDao.findSearchResults(name)
                .stream()
                .map(AuthorView::new)
                .collect(Collectors.toList());
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
}
