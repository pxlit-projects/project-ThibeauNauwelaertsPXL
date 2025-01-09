import { Component, OnInit } from '@angular/core';
import { CommonModule, NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router'; 
import { ReviewService } from '../../../shared/services/review.service'; 
import { ReviewNotificationService } from '../../../shared/services/review-notification.service'; 
import { NotificationMessage } from '../../../shared/models/notification-message.model'; 
import { AuthService } from '../../../shared/services/auth.service'; 
import { ReviewActionsComponent } from  '../reviews-actions/reviews-actions.component';
@Component({
  selector: 'app-reviews',
  standalone: true,
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css'],

  imports: [CommonModule, NgForOf, FormsModule, ReviewActionsComponent], 
})

export class ReviewsComponent implements OnInit {
  reviews: any[] = [];
  loading: boolean = false;
  errorMessage: string | null = null;

  constructor(
    private reviewService: ReviewService,
    private router: Router,
    private reviewNotificationService: ReviewNotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReviews();

    // Subscribe to review updates
    this.reviewNotificationService.getReviewUpdate().subscribe((notification: NotificationMessage) => {
      console.log('Received notification in reviews:', notification);
      this.handleReviewNotification(notification);
    });
  }

  loadReviews(): void {
    this.loading = true;
    this.reviewService.getAllReviews().subscribe({
      next: (data) => {
        this.reviews = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading reviews', error);
        this.errorMessage = 'Failed to load reviews.';
        this.loading = false;
      },
    });
  }

  handleReviewNotification(notification: NotificationMessage): void {
    const review = this.reviews.find((r) => r.reviewId === notification.postId);
  
    if (review) {
      review.status = notification.status.toUpperCase(); 
      review.reviewer = notification.reviewer;
      review.remarks = notification.remarks || 'None';
  
      if (review.status === 'REJECTED') {
        this.removeReviewFromList(review.reviewId);
      }
    }
  }

  removeReviewFromList(reviewId: number): void {
    console.log(`Removing review with reviewId: ${reviewId}`);
    this.reviews = this.reviews.filter((r) => r.reviewId !== reviewId);
  }
}
