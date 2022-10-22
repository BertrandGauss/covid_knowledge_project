package com.covid.service;

import com.covid.dao.AuthorDao;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AuthorService {

    private AuthorDao AuthorDao;

    private  Neo4jClient neo4jClient;

    private  Driver driver;

    private  DatabaseSelectionProvider databaseSelectionProvider;

    public Map<String, List<Object>> fetchAuthorGraph() {

        var nodes = new ArrayList<>();
        var links = new ArrayList<>();

        try (Session session = sessionFor(database())) {
            var records = session.readTransaction(tx -> tx.run(""
                    + " MATCH (m:Paper) <- [r:write] - (p:Author)"
                    + " WITH m, p ORDER BY m.title, p.name"
                    + " RETURN m.title AS paper, collect(p.name) AS authors"
            ).list());
            records.forEach(record -> {
                var movie = Map.of("label", "paper", "title", record.get("movie").asString());

                var targetIndex = nodes.size();
                nodes.add(movie);

                record.get("authors").asList(Value::asString).forEach(name -> {
                    var actor = Map.of("label", "authors", "title", name);

                    int sourceIndex;
                    if (nodes.contains(actor)) {
                        sourceIndex = nodes.indexOf(actor);
                    } else {
                        nodes.add(actor);
                        sourceIndex = nodes.size() - 1;
                    }
                    links.add(Map.of("source", sourceIndex, "target", targetIndex));
                });
            });
        }
        return Map.of("nodes", nodes, "links", links);
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
