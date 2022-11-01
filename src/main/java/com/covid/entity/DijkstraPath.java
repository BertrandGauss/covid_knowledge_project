package com.covid.entity;

import java.util.List;

public class DijkstraPath {
    private String sourceNodeName;
    private String targetNodeName;
    private Double totalCost;
    private List<String> nodeNames;
    private List<Double> costs;

    public DijkstraPath (String sourceNodeName,String targetNodeName,Double totalCost,List<String> nodeNames,List<Double> costs){
        this.sourceNodeName = sourceNodeName;
        this.targetNodeName = targetNodeName;
        this.totalCost = totalCost;
        this.nodeNames = nodeNames;
        this.costs = costs;
    }



    public String getSourceNodeName() {
        return sourceNodeName;
    }

    public void setSourceNodeName(String sourceNodeName) {
        this.sourceNodeName = sourceNodeName;
    }

    public String getTargetNodeName() {
        return targetNodeName;
    }

    public void setTargetNodeName(String targetNodeName) {
        this.targetNodeName = targetNodeName;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setCosts(List<Double> costs) {
        this.costs = costs;
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public void setNodeNames(List<String> nodeNames) {
        this.nodeNames = nodeNames;
    }

    public List<Double> getCosts() {
        return costs;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}
