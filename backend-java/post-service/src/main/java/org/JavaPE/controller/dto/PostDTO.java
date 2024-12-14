package org.JavaPE.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PostDTO {
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String author;

    private LocalDate createdDate;
    private LocalDate lastModifiedDate;

    @NotNull(message = "Status cannot be null")
    private String status;
    private String remarks;
}
