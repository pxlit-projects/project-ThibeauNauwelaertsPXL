package org.JavaPE.controller.dto;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class NotificationMessage implements Serializable {
    // Getters and Setters
    private Long postId;
    private String status;
    private String reviewer;
    private String remarks;

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "postId=" + postId +
                ", status='" + status + '\'' +
                ", reviewer='" + reviewer + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
