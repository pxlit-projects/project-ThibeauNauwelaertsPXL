import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommentService, Comment } from '../../../shared/services/comment.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../shared/services/auth.service';
import { AddCommentComponent } from '../add-comment/add-comment.component';

@Component({
  selector: 'app-comments',
  standalone: true,
  imports: [CommonModule, FormsModule, AddCommentComponent],
  templateUrl: './comments-overview.component.html',
  styleUrls: ['./comments-overview.component.css'],
})
export class CommentsComponent implements OnInit {
  comments: Comment[] = [];
  postId!: number;
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
        // Sort comments to show the newest first
        this.comments = data.sort((a, b) => {
          const dateA = new Date(a.createdAt ?? 0).getTime();
          const dateB = new Date(b.createdAt ?? 0).getTime();
          return dateB - dateA;
        });
      },
      error: (err) => {
        console.error('Failed to fetch comments:', err);
      },
    });
  }

  onCommentAdded(): void {
    this.fetchComments();
  }

  editComment(comment: Comment): void {
    this.commentService.updateComment(comment.id, comment).subscribe({
      next: (updatedComment) => {
        const index = this.comments.findIndex((c) => c.id === updatedComment.id);
        if (index !== -1) {
          this.comments[index] = updatedComment;
          this.editMode[comment.id] = false;
        }
      },
      error: (err) => {
        console.error('Failed to edit comment:', err);
      },
    });
  }

  deleteComment(commentId: number): void {
    if (!commentId) {
      console.error('Comment ID is missing. Cannot delete.');
      return;
    }

    const username = this.authService.getUsername() || 'guest';
    this.commentService.deleteComment(commentId, username).subscribe({
      next: () => {
        console.log(`Comment with ID ${commentId} deleted successfully.`);
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
