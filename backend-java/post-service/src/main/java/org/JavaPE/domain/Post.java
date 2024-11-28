package org.JavaPE.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String author;

    private LocalDate createdDate;
    private LocalDate lastModifiedDate;

    @Enumerated(EnumType.STRING)
    private PostStatus status; // Status: DRAFT, PUBLISHED
    private String remarks;
}
