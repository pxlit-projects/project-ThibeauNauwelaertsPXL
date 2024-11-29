package org.JavaPE.controller.converter;

import org.JavaPE.controller.DTO.CommentDTO;
import org.JavaPE.domain.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentConverter {

    // Convert CommentDTO to Comment entity
    public Comment toEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setPostId(commentDTO.getPostId());
        comment.setAuthor(commentDTO.getAuthor());
        comment.setContent(commentDTO.getContent());
        return comment;
    }

    // Convert Comment entity to CommentDTO
    public CommentDTO toDTO(Comment comment) {
        return new CommentDTO(
                comment.getPostId(),
                comment.getAuthor(),
                comment.getContent()
        );
    }
}
