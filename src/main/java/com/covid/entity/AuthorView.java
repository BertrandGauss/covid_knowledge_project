package com.covid.entity;

import java.util.Objects;

public class AuthorView {
    private Author author;

    public AuthorView(Author author) {
        this.author = author;
    }

    public Author getAuthor() {
        return author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorView that = (AuthorView) o;
        return Objects.equals(author, that.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author);
    }
}
