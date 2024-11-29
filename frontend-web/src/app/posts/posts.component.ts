import { Component, OnInit } from '@angular/core';
import { Post, PostService } from '../services/post.service';
import { Router } from '@angular/router';
import { CommonModule, NgForOf } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css'],
  standalone: true,
  imports: [CommonModule, NgForOf, FormsModule],
})
export class PostsComponent implements OnInit {
  posts: Post[] = [];
  filterCriteria: Partial<Post> = {};
  isEditor: boolean = false;

  constructor(private postService: PostService, private router: Router) {}

  ngOnInit(): void {
    const role = localStorage.getItem('userRole') || 'VIEWER';
    this.isEditor = role === 'EDITOR';
    this.fetchPosts();
  }

  fetchPosts(filters: Partial<Post> = {}): void {
    const hasFilters = Object.keys(filters).some(
      (key) => filters[key as keyof Post] !== '' && filters[key as keyof Post] !== undefined
    );

    if (hasFilters) {
      this.postService.getFilteredPosts(filters).subscribe({
        next: (data) => (this.posts = data),
        error: (err) => console.error('Failed to load filtered posts:', err),
      });
    } else {
      this.postService.getPublishedPosts().subscribe({
        next: (data) => (this.posts = data),
        error: (err) => console.error('Failed to load posts:', err),
      });
    }
  }

  applyFilters(): void {
    const filters = { ...this.filterCriteria };

    if (!filters.createdDate) delete filters.createdDate;
    if (!filters.lastModifiedDate) delete filters.lastModifiedDate;

    this.fetchPosts(filters);
  }

  clearFilters(): void {
    this.filterCriteria = {};
    this.fetchPosts();
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']);
  }

  editPost(postId: number): void {
    this.router.navigate(['/edit-post', postId]);
  }

  viewComments(postId: number): void {
    this.router.navigate(['/comments', postId]);
  }
}
