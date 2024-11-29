package org.JavaPE.controller.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostResponse {
    // Getters and Setters
    private Long id;
    private String title;
    private String content;
    private String author;
}
