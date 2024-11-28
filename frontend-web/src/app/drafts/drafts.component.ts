import { Component, OnInit } from '@angular/core';
import { Post, PostService } from '../services/post.service';
import { Router } from '@angular/router';
import { NotificationMessage } from '../models/notification-message.model';
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

  filterCriteria: Partial<Post> = {
    content: '',
    author: '',
    createdDate: undefined,
    lastModifiedDate: undefined,
  };

  constructor(
    private postService: PostService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.fetchDraftPosts();
    this.listenToReviewUpdates();
  }

  fetchDraftPosts(filters: Partial<Post> = {}): void {
    this.loading = true;
    this.postService.getDraftPosts(filters).subscribe({
      next: (data) => {
        this.draftPosts = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error fetching drafts', error);
        this.errorMessage = 'Failed to load draft posts.';
        this.loading = false;
      },
    });
  }
  

  listenToReviewUpdates(): void {
    const eventSource = new EventSource('http://localhost:8083/review/reviews/notifications');

    eventSource.onmessage = (event) => {
      const notification: NotificationMessage = JSON.parse(event.data);
      console.log('Received notification:', notification);
      this.updateDraftWithFeedback(notification); // Update draft post with feedback
    };

    eventSource.onerror = (error) => {
      console.error("Error with SSE connection:", error);
    };
  }

  updateDraftWithFeedback(notification: NotificationMessage): void {
    const postId = notification.postId;
    console.log('Received postId:', postId); // Check the received postId
    console.log('Draft posts:', this.draftPosts); // Check the draft posts
    
    const post = this.draftPosts.find((p) => p.id === postId);
    console.log('Draft post:', post); // Ensure the post is found
    
    if (post) {
      // Check if remarks are received
      post.remarks = notification.remarks || 'No remarks';  // If no remarks, set to 'No remarks'
      console.log('Updated remarks:', post.remarks); // Log remarks to see what we received
  
      // Update the post status if it is rejected
      post.status = notification.status === 'rejected' ? 'REJECTED' : post.status;
  
      // Save the updated post (API call to update the post in backend)
      this.postService.updatePost(post.id, post).subscribe({
        next: (updatedPost) => {
          console.log('Draft updated with rejection feedback:', updatedPost);
        },
        error: (err) => {
          console.error('Error updating post:', err);
        },
      });
    }
  }
  

  addNewDraft(post: Post): void {
    this.draftPosts.push(post); // Directly add the new post to the drafts list
    console.log('New draft added:', post);
  }

  applyFilters(): void {
    const filters = { ...this.filterCriteria };
    this.fetchDraftPosts(filters); 
  }

  clearFilters(): void {
    this.filterCriteria = {
      content: '',
      author: '',
      createdDate: undefined,
      lastModifiedDate: undefined,
    };
    this.fetchDraftPosts(); 
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }

  editPost(postId: number): void {
    this.router.navigate(['/edit-post', postId]);
  }
}
