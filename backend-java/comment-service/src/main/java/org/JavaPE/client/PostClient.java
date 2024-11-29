package org.JavaPE.client;

import org.JavaPE.controller.DTO.PostResponse;
import org.JavaPE.controller.dto.PostDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "post-service", path = "/posts")
public interface PostClient {
    @GetMapping("/published/{id}")
    PostDTO getPublishedPostById(
            @PathVariable("id") Long postId,
            @RequestHeader("X-User-Role") String role);
}
