import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommentService, Comment } from '../../../shared/services/comment.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css'],
})
export class CommentsListComponent implements OnInit {
  comments: Comment[] = [];
  postId!: number;
  newComment: Comment = { id: 0, postId: 0, author: '', content: '' };
  editMode: { [key: number]: boolean } = {}; // Track edit mode for each comment
  currentUser: string | null = '';

  constructor(
    private commentService: CommentService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUsername();
    this.route.params.subscribe((params) => {
      this.postId = +params['postId'];
      this.fetchComments();
    });
  }

  fetchComments(): void {
    this.commentService.getCommentsByPostId(this.postId).subscribe({
      next: (data) => {
        this.comments = data;
      },
      error: (err) => {
        console.error('Failed to fetch comments:', err);
      },
    });
  }

  addComment(): void {
    const username = localStorage.getItem('currentUser') || 'Anonymous';
    this.newComment.postId = this.postId;
    this.newComment.author = username;
  
    this.commentService.addCommentToPost(this.postId, this.newComment).subscribe({
      next: (comment) => {
        this.comments = [comment, ...this.comments];
        this.newComment.content = '';
      },
      error: (err) => {
        console.error('Failed to add comment:', err);
      },
    });
  }
  
  editComment(comment: Comment): void {
    this.commentService.updateComment(comment.id, comment).subscribe({
      next: (updatedComment) => {
        const index = this.comments.findIndex((c) => c.id === updatedComment.id);
        if (index !== -1) {
          this.comments[index] = updatedComment;
          this.editMode[comment.id] = false; // Exit edit mode for the comment
        }
      },
      error: (err) => {
        console.error('Failed to edit comment:', err);
      },
    });
  }

  deleteComment(commentId: number): void {
    const currentUser = this.authService.getUsername() || 'guest';
    this.commentService.deleteComment(commentId, currentUser).subscribe({
      next: () => {
        this.comments = this.comments.filter((comment) => comment.id !== commentId);
      },
      error: (err) => {
        console.error('Failed to delete comment:', err);
      },
    });
  }

  toggleEditMode(commentId: number): void {
    this.editMode[commentId] = !this.editMode[commentId];
  }
}
