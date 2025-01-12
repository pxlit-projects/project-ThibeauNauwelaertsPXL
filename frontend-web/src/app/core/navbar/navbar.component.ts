import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule], // Include RouterModule for RouterLink
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit, OnDestroy {
  isAuthenticated: boolean = false; // Reactive property to track auth status
  private authSubscription: Subscription | undefined;

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Initialize the authentication status
    this.isAuthenticated = this.authService.isAuthenticated();

    // Subscribe to the authState$ observable to listen for auth changes
    this.authSubscription = this.authService.authState$.subscribe(
      (status: boolean) => {
        this.isAuthenticated = status;
      },
      (error) => {
        console.error('Error subscribing to authState$', error);
      }
    );
  }

  logout(): void {
    this.authService.logout();
    // Navigation is handled in AuthService's logout method via the subscription
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    // Clean up the subscription to prevent memory leaks
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }
}
