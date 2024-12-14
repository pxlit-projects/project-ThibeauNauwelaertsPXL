import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommentService, Comment } from '../services/comment.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../login/auth.service';

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
  currentUser: string | null = '';

  constructor(
    private commentService: CommentService,
    private authService: AuthService, // Inject AuthService
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Fetch the currently logged-in username
    this.currentUser = this.authService.getUsername();

    // Extract the post ID from the route
    this.route.params.subscribe((params) => {
      this.postId = +params['postId'];
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
    const username = localStorage.getItem('currentUser') || 'Anonymous'; // Get username from AuthService or default to 'Anonymous'
    this.newComment.postId = this.postId;
    this.newComment.author = username; // Set the author field
  
    this.commentService.addCommentToPost(this.postId, this.newComment).subscribe({
      next: (comment) => {
        this.comments = [comment, ...this.comments]; // Prepend the new comment to the array
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
    console.log('Comment ID to delete:', commentId); // Debugging log
    if (!commentId) {
      console.error('Comment ID is null or undefined.');
      return;
    }
  
    const currentUser = this.authService.getUsername() || 'guest'; // Get the username from AuthService
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
