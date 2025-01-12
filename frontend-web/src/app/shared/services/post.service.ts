import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Post } from '../models/post.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';
@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = environment.postUrl;

  constructor(private http: HttpClient, private authService: AuthService) {}

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
    const role = this.authService.getRole();
    console.log('Creating headers with role:', role);
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
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
