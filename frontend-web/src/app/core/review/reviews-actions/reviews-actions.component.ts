import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ReviewService, RejectRequest } from '../../../shared/services/review.service';
import { AuthService } from '../../../shared/services/auth.service';

@Component({
  selector: 'app-review-actions',
  standalone: true,
  template: `
    <div class="actions">
      <button (click)="approve()" class="bg-green-500 text-white px-4 py-2 rounded">Approve</button>
      <button (click)="reject()" class="bg-red-500 text-white px-4 py-2 rounded">Reject</button>
    </div>
  `,
})
export class ReviewActionsComponent {
  @Input() reviewId!: number;
  @Output() reviewApproved = new EventEmitter<number>();
  @Output() reviewRejected = new EventEmitter<number>();

  constructor(private reviewService: ReviewService, private authService: AuthService) {}

  approve(): void {
    const reviewer = this.authService.getUsername() || 'Unknown';
    this.reviewService.approveReview(this.reviewId, reviewer).subscribe({
      next: () => {
        console.log(`Review ${this.reviewId} approved.`);
        this.reviewApproved.emit(this.reviewId); // Notify parent component
      },
      error: (error) => {
        console.error(`Error approving review ${this.reviewId}:`, error);
      },
    });
  }

  reject(): void {
    const reviewer = this.authService.getUsername() || 'Unknown';
    const remarks = prompt('Enter remarks for rejection:');
    if (!remarks) return;

    const rejectRequest: RejectRequest = { reviewer, remarks };
    this.reviewService.rejectReview(this.reviewId, rejectRequest).subscribe({
      next: () => {
        console.log(`Review ${this.reviewId} rejected.`);
        this.reviewRejected.emit(this.reviewId); // Notify parent component
      },
      error: (error) => {
        console.error(`Error rejecting review ${this.reviewId}:`, error);
      },
    });
  }
}
