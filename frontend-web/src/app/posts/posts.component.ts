import { Component, OnInit } from '@angular/core';
import { Post, PostService } from '../services/post.service';
import { Router } from '@angular/router'; // Inject Router for navigation
import { CommonModule, NgForOf } from '@angular/common'; // Angular modules
import { FormsModule } from '@angular/forms'; // For [(ngModel)]

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css'],
  standalone: true,
  imports: [CommonModule, NgForOf, FormsModule], // Include FormsModule for ngModel
})
export class PostsComponent implements OnInit {
  posts: Post[] = []; // Array to store posts
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

    // Fetch all posts on initialization
    this.fetchPosts();
  }

  fetchPosts(filters: Partial<Post> = {}): void {
    const hasFilters = Object.keys(filters).some(
      (key) => filters[key as keyof Post] !== '' && filters[key as keyof Post] !== undefined
    );
  
    if (hasFilters) {
      // Fetch filtered posts
      this.postService.getFilteredPosts(filters).subscribe({
        next: (data) => {
          this.posts = data; // Assign the response data to the posts array
        },
        error: (err) => console.error('Failed to load filtered posts:', err),
      });
    } else {
      // Fetch all published posts
      this.postService.getPublishedPosts().subscribe({
        next: (data) => {
          this.posts = data; // Assign the response data to the posts array
        },
        error: (err) => console.error('Failed to load posts:', err),
      });
    }
  }
  

  applyFilters(): void {
    const filters = { ...this.filterCriteria };

    // If dates are empty, exclude them from the filters
    if (!filters.createdDate) delete filters.createdDate;
    if (!filters.lastModifiedDate) delete filters.lastModifiedDate;

    this.fetchPosts(filters); // Fetch posts with filters
  }

  clearFilters(): void {
    this.filterCriteria = {
      content: '',
      author: '',
      createdDate: undefined,
      lastModifiedDate: undefined,
    }; // Reset filter criteria
    this.fetchPosts(); // Fetch all posts without filters
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']); // Redirect to Create Post page
  }

  editPost(postId: number): void {
    this.router.navigate(['/edit-post', postId]); // Navigate to the edit post route
  }
}
