package com.covid.entity;


import java.util.List;

public class AuthorCooperators {

    private String name;

    private String cooperator;

    private String paper;

    public AuthorCooperators(String name, String cooperator, String paper) {
        this.name = name;
        this.cooperator = cooperator;
        this.paper = paper;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCooperator() {
        return cooperator;
    }

    public void setCooperator(String cooperator) {
        this.cooperator = cooperator;
    }

    public String getPaper() {
        return paper;
    }

    public void setPaper(String paper) {
        this.paper = paper;
    }
}
