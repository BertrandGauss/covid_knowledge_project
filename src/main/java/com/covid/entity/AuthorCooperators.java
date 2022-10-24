package com.covid.entity;


import java.util.List;

public class AuthorCooperators {

    private String name;

    private List<Author> cooperators;

    public AuthorCooperators(String name, List<Author> cooperators) {
        this.name = name;
        this.cooperators = cooperators;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Author> getCooperators() {
        return cooperators;
    }

    public void setCooperators(List<Author> cooperators) {
        this.cooperators = cooperators;
    }
}
