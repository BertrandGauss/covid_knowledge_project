package com.covid.service;

import com.covid.dao.AuthorDao;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;


@Service
public class AuthorService {

    private AuthorDao AuthorDao;

    private  Neo4jClient neo4jClient;

    private  Driver driver;

    private  DatabaseSelectionProvider databaseSelectionProvider;


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
