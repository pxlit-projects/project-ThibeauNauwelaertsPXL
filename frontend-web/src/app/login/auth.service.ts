import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private accounts = [
    { username: 'admin', password: 'admin123', role: 'admin' },
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

      // Set authToken in localStorage to simulate authentication
      localStorage.setItem('authToken', 'true');
      localStorage.setItem('userRole', account.role);

      return true;
    }

    return false; // Return false if login fails
  }

  logout(): void {
    this.currentUser = null;
    this.currentRole = 'user';

    // Clear authToken from localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
  }

  getRole(): string {
    return localStorage.getItem('userRole') || 'user';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('authToken') === 'true';
  }
}
