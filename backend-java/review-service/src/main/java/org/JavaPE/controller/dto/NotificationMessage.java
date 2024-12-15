package org.JavaPE.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class NotificationMessage implements Serializable {
    // Getters and Setters
    private Long postId;
    private String status;
    private String reviewer;
    private String remarks;

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
