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

      // Set authToken and username in localStorage to simulate authentication
      localStorage.setItem('authToken', 'true');
      localStorage.setItem('userRole', account.role);
      localStorage.setItem('currentUser', account.username); // Store the username

      return true;
    }

    return false; // Return false if login fails
  }

  logout(): void {
    this.currentUser = null;
    this.currentRole = 'user';

    // Clear authToken and username from localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('currentUser'); // Clear the username
  }

  getUsername(): string | null {
    return localStorage.getItem('currentUser'); // Retrieve the username from localStorage
  }

  getRole(): string {
    return localStorage.getItem('userRole') || 'user';
  }

  isAuthenticated(): boolean {
    return localStorage.getItem('authToken') === 'true';
  }
}
