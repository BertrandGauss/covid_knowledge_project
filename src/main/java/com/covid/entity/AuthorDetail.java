package com.covid.entity;


import java.util.List;
import java.util.Objects;


public class AuthorDetail {
    private String name;

    private List<Paper> cast;

    public AuthorDetail(String name, List<Paper> cast) {
        this.name = name;
        this.cast = cast;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Paper> getCast() {
        return cast;
    }

    public void setCast(List<Paper> cast) {
        this.cast = cast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorDetail that = (AuthorDetail) o;
        return Objects.equals(name, that.name) && Objects.equals(cast, that.cast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cast);
    }
}
