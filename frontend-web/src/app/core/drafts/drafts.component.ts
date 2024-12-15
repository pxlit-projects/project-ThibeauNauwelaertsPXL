import { Component, OnInit } from '@angular/core';
import { Post, PostService } from '../../shared/services/post.service';
import { Router } from '@angular/router';
import { NotificationMessage } from '../../shared/models/notification-message.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-draft-posts',
  templateUrl: './drafts.component.html',
  styleUrls: ['./drafts.component.css'],
  imports: [CommonModule, FormsModule],
  standalone: true,
})
export class DraftPostsComponent implements OnInit {
  draftPosts: Post[] = [];
  errorMessage: string | null = null;
  loading: boolean = false;

  filterCriteria: Partial<Post> = {};

  constructor(private postService: PostService, private router: Router) {}

  ngOnInit(): void {
    this.fetchDraftPosts();
    this.listenToReviewUpdates();
  }

  fetchDraftPosts(filters: Partial<Post> = {}): void {
    this.loading = true;

    // Add "DRAFT" status filter
    filters.status = 'DRAFT';

    // Convert date fields to ISO strings
    if (filters.createdDate) {
      filters.createdDate = new Date(filters.createdDate).toISOString().split('T')[0];
    }
    if (filters.lastModifiedDate) {
      filters.lastModifiedDate = new Date(filters.lastModifiedDate).toISOString().split('T')[0];
    }

    this.postService.getDraftPosts(filters).subscribe({
      next: (data) => {
        this.draftPosts = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching drafts:', error);
        this.errorMessage = 'Failed to load draft posts.';
        this.loading = false;
      },
    });
  }

  listenToReviewUpdates(): void {
    const eventSource = new EventSource('http://localhost:8083/review/reviews/notifications');

    eventSource.onmessage = (event) => {
      const notification: NotificationMessage = JSON.parse(event.data);
      this.updateDraftWithFeedback(notification);
    };

    eventSource.onerror = (error) => {
      console.error('Error with SSE connection:', error);
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
        },
        error: (err) => {
          console.error('Error updating draft post:', err);
        },
      });
    }
  }

  applyFilters(): void {
    const filters = { ...this.filterCriteria };

    // Convert dates to ISO strings
    if (filters.createdDate) {
      filters.createdDate = new Date(filters.createdDate).toISOString().split('T')[0];
    }
    if (filters.lastModifiedDate) {
      filters.lastModifiedDate = new Date(filters.lastModifiedDate).toISOString().split('T')[0];
    }

    // Remove empty fields
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
}
