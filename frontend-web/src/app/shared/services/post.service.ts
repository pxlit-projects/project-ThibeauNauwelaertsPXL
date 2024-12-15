import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface Post {
  id: number;
  title: string;
  content: string;
  status: string; // e.g., "PUBLISHED", "DRAFT"
  remarks?: string; 
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

  getPublishedPosts(): Observable<Post[]> {
    const headers = this.createHeaders();
    return this.http
      .get<Post[]>(`${this.baseUrl}/published`, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to fetch published posts.')
        )
      );
  }
  
  getDraftPosts(filters: Partial<Post> = {}): Observable<Post[]> {
    const headers = this.createHeaders();
    
    // Build query parameters from the filters object
    const params = new URLSearchParams();
    if (filters.content) params.append('content', filters.content);
    if (filters.author) params.append('author', filters.author);
    if (filters.createdDate) params.append('createdDate', filters.createdDate);
    if (filters.lastModifiedDate) params.append('lastModifiedDate', filters.lastModifiedDate);
  
    return this.http
      .get<Post[]>(`${this.baseUrl}/drafts?${params.toString()}`, { headers }) // Use GET with query params
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to fetch draft posts.')
        )
      );
  }
  

  createPost(post: Post): Observable<Post> {
    const headers = this.createHeaders();
    return this.http
      .post<Post>(this.baseUrl, post, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to create the post.')
        )
      );
  }
  
  getPostById(id: number): Observable<Post> {
    const headers = this.createHeaders();
    return this.http
      .get<Post>(`${this.baseUrl}/${id}`, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to fetch post with ID ${id}.`)
        )
      );
  }
  
  updatePost(id: number, post: Post): Observable<Post> {
    const headers = this.createHeaders();
    return this.http
      .put<Post>(`${this.baseUrl}/${id}`, post, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to update post with ID ${id}.`)
        )
      );
  }

  getFilteredPosts(filters: Partial<Post>): Observable<Post[]> {
    const headers = this.createHeaders();
    
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        if (key === 'createdDate' || key === 'lastModifiedDate') {
          params.append(key, new Date(value as string).toISOString().split('T')[0]);
        } else {
          params.append(key, value as string);
        }
      }
    });
  
    return this.http
      .get<Post[]>(`${this.baseUrl}/filtered?${params.toString()}`, { headers }) 
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to fetch filtered posts.')
        )
      );
  }
  
  

  private createHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': 'editor',
    });
  }

  /**
   * Handle HTTP errors
   * @param error The HTTP error
   * @param message A custom error message
   * @returns Observable<never>
   */
  private handleError(error: any, message: string): Observable<never> {
    console.error(message, error);
    return throwError(() => new Error(message));
  }
}
