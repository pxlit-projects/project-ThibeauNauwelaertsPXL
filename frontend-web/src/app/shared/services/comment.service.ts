import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Comment } from '../models/comment.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class CommentService {
  private baseUrl = environment.commentUrl;
  private authToken =
    'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9FRElUT1IifQ.WyhcB0Og8qV2HPLMlc5gG5wkl3F5oqhZ0R_Dd3pZeqo';

  constructor(private http: HttpClient) {}

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

  private createHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: this.authToken,
    });
  }

  private handleError(error: any, message: string): Observable<never> {
    console.error(message, error);
    return throwError(() => new Error(message));
  }
}
