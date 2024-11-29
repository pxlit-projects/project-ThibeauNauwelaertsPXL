package org.JavaPE.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWithPostDetailsDTO {
    private Long reviewId;
    private Long postId;
    private String status;
    private String author;
    private String reviewer;
    private String remarks;
    private String submittedAt;
    private String reviewedAt;
    private String postTitle;
    private String postContent;
}
