package org.JavaPE.client;

import org.JavaPE.controller.dto.PostResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "post-service", path = "/posts")
public interface PostClient {
    @GetMapping("/{id}")
    PostResponse getPostById(
            @PathVariable("id") Long postId,
            @RequestHeader("X-User-Role") String role);

    @PostMapping("/{id}/publish")
    void publishPost(
            @PathVariable("id") Long postId,
            @RequestHeader("X-User-Role") String role);
}