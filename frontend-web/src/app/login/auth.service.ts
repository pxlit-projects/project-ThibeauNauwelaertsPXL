import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://your-backend-api-url/auth/login'; // Your backend login endpoint

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    const loginData = { username, password };
    return this.http.post(this.apiUrl, loginData).pipe(
      catchError((error) => {
        // Handle error
        throw error;
      })
    );
  }
}
