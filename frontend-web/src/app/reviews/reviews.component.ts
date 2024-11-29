import { Component, OnInit } from '@angular/core';
import { CommonModule, NgForOf } from '@angular/common'; // Import CommonModule for date pipe
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router'; // Import Router for navigation
import { ReviewService, RejectRequest } from '../services/review.service'; // Adjust imports
import { ReviewNotificationService } from '../services/review-notification.service'; // Import notification service
import { NotificationMessage } from '../models/notification-message.model'; // Import notification model

@Component({
  selector: 'app-reviews',
  standalone: true,
  templateUrl: './reviews.component.html',
  styleUrls: ['./reviews.component.css'],
  imports: [CommonModule, NgForOf, FormsModule], // Ensure CommonModule is included for the date pipe
})
export class ReviewsComponent implements OnInit {
  reviews: any[] = [];
  loading: boolean = false;
  errorMessage: string | null = null;

  constructor(
    private reviewService: ReviewService,
    private router: Router,
    private reviewNotificationService: ReviewNotificationService // Inject the notification service
  ) {}

  ngOnInit(): void {
    this.loadReviews();

    // Subscribe to review updates
    this.reviewNotificationService.getReviewUpdate().subscribe((notification: NotificationMessage) => {
      console.log('Received notification in reviews:', notification);
      this.handleReviewNotification(notification);
    });
  }

  // Load reviews
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

  // Approve review
  approveReview(reviewId: number): void {
    this.reviewService.approveReview(reviewId, 'Editor123').subscribe({
      next: () => {
        console.log(`Review ${reviewId} approved.`);
        this.navigateToDrafts(); // Navigate to Drafts after approval
      },
      error: (error) => {
        console.error('Error approving review', error);
        this.errorMessage = `Failed to approve review ID ${reviewId}.`;
      },
    });
  }

  // Reject review
  rejectReview(reviewId: number): void {
    const remarks = prompt('Enter remarks for rejection:');
    if (!remarks) return;

    const rejectRequest: RejectRequest = {
      reviewer: 'Editor123',
      remarks,
    };

    this.reviewService.rejectReview(reviewId, rejectRequest).subscribe({
      next: () => {
        console.log(`Review ${reviewId} rejected.`);
        this.navigateToDrafts(); // Navigate to Drafts after rejection
      },
      error: (error) => {
        console.error('Error rejecting review', error);
        this.errorMessage = `Failed to reject review ID ${reviewId}.`;
      },
    });
  }

  // Handle review notification
  handleReviewNotification(notification: NotificationMessage): void {
    const review = this.reviews.find((r) => r.id === notification.postId);
  
    if (review) {
      review.status = notification.status.toUpperCase(); // Update status
      review.reviewer = notification.reviewer;
      review.remarks = notification.remarks || 'None';
  
      // Remove from the review list if rejected
      if (review.status === 'REJECTED') {
        this.reviews = this.reviews.filter((r) => r.id !== review.id);
      }
    }
  }
  
  // Navigate to Drafts
  private navigateToDrafts(): void {
    this.router.navigate(['/drafts']);
  }
}
