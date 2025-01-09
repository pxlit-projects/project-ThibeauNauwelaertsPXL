import { Component, OnInit, NgZone } from '@angular/core';
import { PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { NotificationMessage } from '../../../shared/models/notification-message.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Post } from '../../../shared/models/post.model';

@Component({
  selector: 'app-draft-posts',
  templateUrl: './drafts.component.html',
  styleUrls: ['./drafts.component.css'],
  imports: [FormsModule, CommonModule],
  standalone: true,
})
export class DraftPostsComponent implements OnInit {
  draftPosts: Post[] = [];
  errorMessage: string | null = null;
  loading: boolean = false;
  toasts: { id: number; message: string; type: string }[] = [];
  private toastCounter = 0;

  filterCriteria: Partial<Post> = {};

  constructor(
    private postService: PostService, 
    private router: Router, 
    private ngZone: NgZone // Inject NgZone to manually trigger change detection
  ) {}

  ngOnInit(): void {
    this.fetchDraftPosts();
    this.listenToReviewUpdates();
  }

  fetchDraftPosts(filters: Partial<Post> = {}): void {
    this.loading = true;
    filters.status = 'DRAFT';

    this.postService.getDraftPosts(filters).subscribe({
      next: (data) => {
        this.draftPosts = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching drafts:', error);
        this.showToast('Failed to load draft posts.', 'error');
        this.loading = false;
      },
    });
  }

  listenToReviewUpdates(): void {
    const eventSource = new EventSource('http://localhost:8083/review/reviews/notifications');

    eventSource.onmessage = (event) => {
      const notification: NotificationMessage = JSON.parse(event.data);
      this.ngZone.run(() => {
        this.updateDraftWithFeedback(notification);
      });
    };

    eventSource.onerror = (error) => {
      console.error('Error with SSE connection:', error);
      this.showToast('Error with live updates', 'error');
    };
  }

  updateDraftWithFeedback(notification: NotificationMessage): void {
    const post = this.draftPosts.find((p) => p.id === notification.postId);
    if (post) {
      post.remarks = notification.remarks || 'No remarks';
      post.status = notification.status === 'rejected' ? 'REJECTED' : post.status;

      this.postService.updatePost(post.id, post).subscribe({
        next: (updatedPost) => {
          console.log('Draft updated with feedback:', updatedPost);
          this.showToast(`Post "${post.title}" updated successfully`, 'success');
        },
        error: (err) => {
          console.error('Error updating draft post:', err);
          this.showToast(`Error updating post "${post.title}"`, 'error');
        },
      });
    } else {
      this.showToast(`Post with ID ${notification.postId} not found`, 'error');
    }
  }

  applyFilters(): void {
    const filters = { ...this.filterCriteria };
    Object.keys(filters).forEach((key) => {
      if (!filters[key as keyof Post]) {
        delete filters[key as keyof Post];
      }
    });

    this.fetchDraftPosts(filters);
  }

  clearFilters(): void {
    this.filterCriteria = {};
    this.fetchDraftPosts();
  }

  addNewDraft(post: Post): void {
    this.draftPosts.push(post);
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }

  editPost(postId: number): void {
    this.router.navigate(['/edit-post', postId]);
  }

  /**
   * Shows a toast message
   * @param message The message to display
   * @param type The type of message ('success', 'error', 'info', 'warning')
   */
  showToast(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info'): void {
    const id = this.toastCounter++;
    this.toasts.push({ id, message, type });

    // Automatically remove toast after 5 seconds
    setTimeout(() => {
      this.removeToast(id);
    }, 5000);
  }

  /**
   * Removes a toast message
   * @param id The unique ID of the toast to remove
   */
  removeToast(id: number): void {
    this.toasts = this.toasts.filter(toast => toast.id !== id);
  }
}
