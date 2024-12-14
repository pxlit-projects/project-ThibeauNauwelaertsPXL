package org.JavaPE.controller.converter;

import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.domain.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentConverter {

    // Convert CommentDTO to Comment entity
    public Comment toEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setId(commentDTO.getId());
        comment.setPostId(commentDTO.getPostId());
        comment.setAuthor(commentDTO.getAuthor());
        comment.setContent(commentDTO.getContent());
        comment.setCreatedAt(commentDTO.getCreatedAt());
        return comment;
    }

    // Convert Comment entity to CommentDTO
    public CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthor(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
