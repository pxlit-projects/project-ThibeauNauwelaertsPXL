package org.JavaPE.client;

import org.JavaPE.controller.Request.ReviewRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "review-service", path = "/reviews", contextId = "reviewClient")
public interface ReviewClient {
    @PostMapping("/submit")
    void submitPostForReview(@RequestBody ReviewRequest reviewRequest);
}
