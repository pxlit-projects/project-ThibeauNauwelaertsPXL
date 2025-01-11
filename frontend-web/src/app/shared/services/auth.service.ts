import { Injectable } from '@angular/core';

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

  constructor() {}

  login(username: string, password: string): boolean {
    const account = this.accounts.find(
      (acc) => acc.username === username && acc.password === password
    );

    if (account) {
      this.currentUser = account.username;
      this.currentRole = account.role;

      localStorage.setItem('authToken', 'true');
      localStorage.setItem('userRole', account.role);
      localStorage.setItem('currentUser', account.username); 

      return true;
    }

    return false; 
  }

  logout(): void {
    this.currentUser = null;
    this.currentRole = 'user';

    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('currentUser'); 
  }

  getUsername(): string | null {
    return localStorage.getItem('currentUser'); 
  }

  getRole(): string {
    return localStorage.getItem('userRole') || 'user';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('authToken') === 'true';
  }
}
