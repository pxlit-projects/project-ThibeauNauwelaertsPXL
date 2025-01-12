import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Review } from '../models/review.model';
import { RejectRequest } from '../models/reject-request.model';
import { ReviewRequest } from '../models/ReviewRequest.model';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class ReviewService {
  private baseUrl = environment.reviewUrl;

  constructor(private http: HttpClient, private authService: AuthService) {}

  submitPostForReview(reviewRequest: ReviewRequest): Observable<string> {
    const headers = this.createHeaders();
    return this.http
      .post<string>(`${this.baseUrl}/submit`, reviewRequest, { headers })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Failed to submit post for review.')
        )
      );
  }

  getAllReviews(): Observable<Review[]> {
    const headers = this.createHeaders();
    return this.http
      .get<Review[]>(`${this.baseUrl}`, { headers })
      .pipe(
        catchError((error) => this.handleError(error, 'Failed to fetch reviews'))
      );
  }  

  approveReview(reviewId: number, reviewer: string): Observable<string> {
    const headers = this.createHeaders();
    return this.http
      .put(`${this.baseUrl}/${reviewId}/approve`, null, {
        headers,
        params: { reviewer },
        responseType: 'text',
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to approve review with ID ${reviewId}.`)
        )
      );
  }

  rejectReview(reviewId: number, rejectRequest: RejectRequest): Observable<string> {
    const headers = this.createHeaders();
    return this.http
      .put(`${this.baseUrl}/${reviewId}/reject`, rejectRequest, {
        headers,
        responseType: 'text',
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, `Failed to reject review with ID ${reviewId}.`)
        )
      );
  }  

  private createHeaders(): HttpHeaders {
    const role = this.authService.getRole();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Role': role,
    });
  }

  private handleError(error: any, message: string): Observable<never> {
    console.error(message, error);
    return throwError(() => new Error(message));
  }
}
