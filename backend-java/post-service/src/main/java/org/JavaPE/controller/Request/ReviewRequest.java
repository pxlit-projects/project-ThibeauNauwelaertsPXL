package org.JavaPE.controller.Request;

import lombok.Getter;

@Getter
public class ReviewRequest {
    private Long postId;
    private String author;

    public ReviewRequest(Long postId, String author) {
        this.postId = postId;
        this.author = author;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}

