import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Comment {
    id: number;
    postId: number;
    author: string;
    content: string;
    createdAt?: string; // Optional field for frontend convenience
  }
  
@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private baseUrl = 'http://localhost:8083/comment/comments';
  private authToken =
    'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9FRElUT1IifQ.WyhcB0Og8qV2HPLMlc5gG5wkl3F5oqhZ0R_Dd3pZeqo';

  constructor(private http: HttpClient) {}

  /**
   * Add a comment to a post
   * @param postId ID of the post
   * @param comment The comment to be added
   */
  addCommentToPost(postId: number, comment: Comment): Observable<Comment> {
    const headers = this.createHeaders();
    return this.http
      .post<Comment>(`${this.baseUrl}/post/${postId}`, comment, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to add comment to the post.')
        )
      );
  }

  /**
   * Get comments for a specific post
   * @param postId ID of the post
   */
  getCommentsByPostId(postId: number): Observable<Comment[]> {
    const headers = this.createHeaders();
    return this.http
      .get<Comment[]>(`${this.baseUrl}/post/${postId}`, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to fetch comments for post ${postId}.`)
        )
      );
  }

  /**
   * Update an existing comment
   * @param commentId ID of the comment
   * @param updatedComment Updated comment details
   */
  updateComment(commentId: number, updatedComment: Comment): Observable<Comment> {
    const headers = this.createHeaders();
    return this.http
      .put<Comment>(`${this.baseUrl}/${commentId}`, updatedComment, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to update comment with ID ${commentId}.`)
        )
      );
  }

  /**
   * Delete a comment
   * @param commentId ID of the comment to delete
   * @param currentUser Current user's username
   */
  deleteComment(commentId: number, currentUser: string): Observable<void> {
    const headers = this.createHeaders().set('X-User-Role', currentUser);
    return this.http
      .delete<void>(`${this.baseUrl}/${commentId}`, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to delete comment with ID ${commentId}.`)
        )
      );
  }

  /**
   * Create headers for the HTTP requests
   */
  private createHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: this.authToken,
    });
  }

  /**
   * Handle HTTP errors
   * @param error The HTTP error
   * @param message A custom error message
   */
  private handleError(error: any, message: string): Observable<never> {
    console.error(message, error);
    return throwError(() => new Error(message));
  }
}
