import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Post {
  id?: number;
  title: string;
  content: string;
  status: 'PUBLISHED' | 'DRAFT'; // Status can be explicitly defined
}

@Injectable({
  providedIn: 'root',
})
export class PostService {
  private baseUrl = 'http://localhost:8083/post/posts';

  constructor(private http: HttpClient) {}

  // Fetch all published posts
  getPublishedPosts(role: string): Observable<Post[]> {
    const headers = this.createHeaders(role);
    return this.http.get<Post[]>(`${this.baseUrl}/published`, { headers });
  }

  // Create a new post
  createPost(role: string, post: Post): Observable<Post> {
    const headers = this.createHeaders(role);
    return this.http.post<Post>(`${this.baseUrl}`, post, { headers });
  }

  // Save a post as a draft
  saveDraft(role: string, post: Post): Observable<Post> {
    const headers = this.createHeaders(role);
    return this.http.post<Post>(`${this.baseUrl}/draft`, post, { headers });
  }

  // Update an existing post
  updatePost(role: string, id: number, post: Post): Observable<Post> {
    const headers = this.createHeaders(role);
    return this.http.put<Post>(`${this.baseUrl}/${id}`, post, { headers });
  }

  // Fetch posts based on filters
  getFilteredPosts(role: string, filter: Partial<Post>): Observable<Post[]> {
    const headers = this.createHeaders(role);
    return this.http.post<Post[]>(`${this.baseUrl}/filtered`, filter, { headers });
  }

  // Utility to create headers for HTTP requests
  private createHeaders(role: string): HttpHeaders {
    return new HttpHeaders({ 'X-User-Role': role });
  }
}
