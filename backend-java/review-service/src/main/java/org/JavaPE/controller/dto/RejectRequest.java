package org.JavaPE.controller.dto;

public class RejectRequest {
    private String reviewer;
    private String remarks;

    // Getters and Setters
    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
