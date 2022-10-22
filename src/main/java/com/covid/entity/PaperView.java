package com.covid.entity;

import java.util.Objects;

public class PaperView {
    private Paper paper;

    public PaperView(Paper paper) {
        this.paper = paper;
    }

    public Paper getPaper() {
        return paper;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperView that = (PaperView) o;
        return Objects.equals(paper, that.paper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paper);
    }
}
