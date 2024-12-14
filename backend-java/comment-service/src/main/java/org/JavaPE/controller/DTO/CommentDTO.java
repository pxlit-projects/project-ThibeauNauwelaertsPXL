package org.JavaPE.controller.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long postId;
    private String author;
    private String content;
    private LocalDateTime createdAt;
}
