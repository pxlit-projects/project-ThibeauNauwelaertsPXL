import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private accounts = [
    { username: 'editor', password: 'admin123', role: 'editor' },
    { username: 'editor2', password: 'admin123', role: 'editor' },
    { username: 'user', password: 'user123', role: 'user' },
  ];

  private currentUser: string | null = null;
  private currentRole: string = 'user';

  // BehaviorSubject to hold the authentication state
  private authStateSubject: BehaviorSubject<boolean>;
  public authState$: Observable<boolean>;

  constructor() {
    const isAuth = this.isAuthenticated();
    this.authStateSubject = new BehaviorSubject<boolean>(isAuth);
    this.authState$ = this.authStateSubject.asObservable();
  }

  login(username: string, password: string): boolean {
    const account = this.accounts.find(
      (acc) => acc.username === username && acc.password === password
    );

    if (account) {
      this.currentUser = account.username;
      this.currentRole = account.role;

      localStorage.setItem('authToken', 'true'); // ✅ Set authToken
      localStorage.setItem('userRole', account.role);
      localStorage.setItem('currentUser', account.username); 

      console.log(`Logged in as ${account.username} with role ${account.role}`);
      this.authStateSubject.next(true); // Emit authentication state
      return true;
    }

    console.log('Login failed: Invalid credentials');
    return false; 
  }

  logout(): void {
    this.currentUser = null;
    this.currentRole = 'user';

    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('currentUser'); 

    console.log('Logged out successfully');
    this.authStateSubject.next(false); // Emit authentication state
  }

  getUsername(): string | null {
    const username = localStorage.getItem('currentUser');
    console.log(`Retrieved username from localStorage: ${username}`);
    return username;
  }

  getRole(): string {
    const role = localStorage.getItem('userRole') || 'user';
    console.log(`Retrieved role from localStorage: ${role}`);
    return role;
  }

  isAuthenticated(): boolean {
    const isAuth = localStorage.getItem('authToken') === 'true';
    console.log(`Is authenticated: ${isAuth}`);
    return isAuth;
  }

  getAuthState(): Observable<boolean> {
    return this.authState$;
  }
}
