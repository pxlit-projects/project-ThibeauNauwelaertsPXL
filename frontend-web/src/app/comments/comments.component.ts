import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommentService, Comment } from '../services/comment.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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

  constructor(
    private commentService: CommentService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.postId = +params['postId']; // Extract the post ID from the route
      this.fetchComments();
    });
  }

  /**
   * Fetch all comments for the current post ID
   */
  fetchComments(): void {
    this.commentService.getCommentsByPostId(this.postId).subscribe({
      next: (data) => {
        this.comments = data; // Assign fetched comments
      },
      error: (err) => {
        console.error('Failed to fetch comments:', err); // Handle errors
      },
    });
  }

  /**
   * Add a new comment to the current post
   */
  addComment(): void {
    // Set the post ID in the new comment
    this.newComment.postId = this.postId;

    this.commentService.addCommentToPost(this.postId, this.newComment).subscribe({
      next: (comment) => {
        this.comments.push(comment); // Add the new comment to the list
        this.newComment.content = ''; // Clear the input
      },
      error: (err) => {
        console.error('Failed to add comment:', err); // Handle errors
      },
    });
  }

  /**
   * Delete a comment by its ID
   * @param commentId - ID of the comment to delete
   */
  deleteComment(commentId: number): void {
    const currentUser = localStorage.getItem('username') || 'guest'; // Adjust to your app's logic
    this.commentService.deleteComment(commentId, currentUser).subscribe({
      next: () => {
        this.comments = this.comments.filter((comment) => comment.id !== commentId); // Remove the deleted comment from the list
      },
      error: (err) => {
        console.error('Failed to delete comment:', err); // Handle errors
      },
    });
  }
}
