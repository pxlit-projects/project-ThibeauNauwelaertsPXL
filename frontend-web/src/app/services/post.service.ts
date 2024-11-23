import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface Post {
  id?: number;
  title: string;
  content: string;
  status: string; // e.g., "PUBLISHED", "DRAFT"
  createdDate?: string;
  lastModifiedDate?: string;
  author?: string;
}

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = 'http://localhost:8083/post/posts';
  private authToken =
    'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9FRElUT1IifQ.WyhcB0Og8qV2HPLMlc5gG5wkl3F5oqhZ0R_Dd3pZeqo';

  constructor(private http: HttpClient) {}

  /**
   * Get Published Posts
   * @param role The user role
   * @returns Observable<Post[]>
   */
  getPublishedPosts(role: string): Observable<Post[]> {
    const headers = this.createHeaders(role);
    return this.http.get<Post[]>(`${this.baseUrl}/published`, { headers }).pipe(
      map((response: Post[]) => response), // Optional: Transform data
      catchError((error) => {
        console.error('Error fetching published posts:', error);
        return throwError(() => new Error('Failed to fetch published posts.'));
      })
    );
  }

  /**
   * Create a New Post
   * @param role The user role
   * @param post The post data
   * @returns Observable<Post>
   */
  createPost(role: string, post: Post): Observable<Post> {
    const headers = this.createHeaders(role);
    return this.http.post<Post>(this.baseUrl, post, { headers }).pipe(
      map((response: Post) => response), // Optional: Transform data
      catchError((error) => {
        console.error('Error creating post:', error);
        return throwError(() => new Error('Failed to create the post.'));
      })
    );
  }

  /**
   * Create HTTP headers with Authorization and Role
   * @param role The user role
   * @returns HttpHeaders
   */
  private createHeaders(role: string): HttpHeaders {
    return new HttpHeaders({
      Authorization: this.authToken, // Token added here
      'X-User-Role': role,
    });
  }
}
