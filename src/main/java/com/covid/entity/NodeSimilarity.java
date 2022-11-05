package com.covid.entity;

public class NodeSimilarity {
    private String node1;
    private String node2;
    private Double similarity;

    public NodeSimilarity(String node1, String node2, Double similarity) {
        this.node1 = node1;
        this.node2 = node2;
        this.similarity = similarity;
    }

    public String getNode1() {
        return node1;
    }

    public void setNode1(String node1) {
        this.node1 = node1;
    }

    public String getNode2() {
        return node2;
    }

    public void setNode2(String node2) {
        this.node2 = node2;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }
}
