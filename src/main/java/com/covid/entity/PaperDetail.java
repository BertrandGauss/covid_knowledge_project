package com.covid.entity;

import java.util.List;
import java.util.Objects;

public class PaperDetail {
    private  String title;

    private List<Author> cast;

    public PaperDetail(String title, List<Author> cast) {
        this.title = title;
        this.cast = cast;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Author> getCast() {
        return cast;
    }

    public void setCast(List<Author> cast) {
        this.cast = cast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperDetail that = (PaperDetail) o;
        return Objects.equals(title, that.title) && Objects.equals(cast, that.cast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, cast);
    }
}
