import { Component, OnInit } from '@angular/core';
import { Post, PostService } from '../services/post.service';
import { Router } from '@angular/router'; // Inject Router for navigation
import { CommonModule, NgForOf } from '@angular/common'; // Angular modules
import { FormsModule } from '@angular/forms'; // For [(ngModel)]

@Component({
  selector: 'app-draft-posts',
  templateUrl: './drafts.component.html',
  styleUrls: ['./drafts.component.css'],
  standalone: true,
  imports: [CommonModule, NgForOf, FormsModule], // Include FormsModule for ngModel
})
export class DraftPostsComponent implements OnInit {
  draftPosts: Post[] = []; // Array to store draft posts
  filterCriteria: Partial<Post> = {
    content: '',
    author: '',
    createdDate: undefined,
    lastModifiedDate: undefined,
  }; // Holds filter inputs
  isEditor: boolean = false;

  constructor(private postService: PostService, private router: Router) {}

  ngOnInit(): void {
    const role = localStorage.getItem('userRole') || 'VIEWER';
    this.isEditor = role === 'EDITOR';

    // Fetch all draft posts on initialization
    this.fetchDraftPosts();
  }

  fetchDraftPosts(filters: Partial<Post> = {}): void {
    const hasFilters = Object.keys(filters).some(
      (key) => filters[key as keyof Post] !== '' && filters[key as keyof Post] !== undefined
    );

    if (hasFilters) {
      // Fetch filtered draft posts
      this.postService.getFilteredPosts(filters).subscribe({
        next: (data) => {
          this.draftPosts = data; // Assign the response data to the draftPosts array
        },
        error: (err) => console.error('Failed to load filtered draft posts:', err),
      });
    } else {
      // Fetch all draft posts
      this.postService.getDraftPosts().subscribe({
        next: (data) => {
          this.draftPosts = data; // Assign the response data to the draftPosts array
        },
        error: (err) => console.error('Failed to load draft posts:', err),
      });
    }
  }

  applyFilters(): void {
    const filters = { ...this.filterCriteria };

    // If dates are empty, exclude them from the filters
    if (!filters.createdDate) delete filters.createdDate;
    if (!filters.lastModifiedDate) delete filters.lastModifiedDate;

    this.fetchDraftPosts(filters); // Fetch draft posts with filters
  }

  clearFilters(): void {
    this.filterCriteria = {
      content: '',
      author: '',
      createdDate: undefined,
      lastModifiedDate: undefined,
    }; // Reset filter criteria
    this.fetchDraftPosts(); // Fetch all draft posts without filters
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']); // Redirect to Create Post page
  }

  editPost(postId: number): void {
    this.router.navigate(['/edit-post', postId]); // Navigate to the edit post route
  }
}
