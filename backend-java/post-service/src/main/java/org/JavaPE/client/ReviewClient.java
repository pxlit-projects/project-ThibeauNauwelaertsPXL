package org.JavaPE.client;

import org.JavaPE.controller.Request.ReviewRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "review-service", path = "/reviews", contextId = "reviewClient")
public interface ReviewClient {
    @PostMapping("/submit")
    void submitPostForReview(@RequestBody ReviewRequest reviewRequest);
    @DeleteMapping("/pending/{postId}")
    void deletePendingReviewForPost(@PathVariable("postId") Long postId);
}
