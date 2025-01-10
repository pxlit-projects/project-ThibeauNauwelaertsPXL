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
    const localPost = this.draftPosts.find((p) => p.id === notification.postId);
    if (!localPost) return;
  
    // Just display remarks in the UI for now
    localPost.remarks = notification.remarks || 'No remarks';
    localPost.status = notification.status === 'rejected' ? 'REJECTED' : localPost.status;
    this.showToast(`Post rejected with remarks: ${localPost.remarks}`, 'info');
  
    // Now actually save remarks in DB (without losing the updated content):
    // 1) fetch the real post from the server
    this.postService.getPostById(localPost.id).subscribe({
      next: (freshPost) => {
        // 2) merge the new remarks and status
        freshPost.remarks = localPost.remarks;
        freshPost.status = localPost.status;
  
        // 3) update the server with the merged data
        this.postService.updatePost(localPost.id, freshPost).subscribe({
          next: (saved) => console.log('Remarks/status saved without losing content:', saved),
          error: (err) => console.error('Error saving remarks to DB:', err)
        });
      },
      error: (err) => console.error('Failed to fetch latest post from server:', err),
    });  
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

  showToast(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info'): void {
    const id = this.toastCounter++;
    this.toasts.push({ id, message, type });

    // Automatically remove toast after 5 seconds
    setTimeout(() => {
      this.removeToast(id);
    }, 5000);
  }

  removeToast(id: number): void {
    this.toasts = this.toasts.filter(toast => toast.id !== id);
  }
}