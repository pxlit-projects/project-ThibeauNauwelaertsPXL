import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Post, PostService } from '../services/post.service';
import { Router } from '@angular/router'; // Inject Router for navigation
import { CommonModule, NgForOf } from '@angular/common'; // Angular modules

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css'],
  standalone: true,
  imports: [CommonModule, NgForOf], // Add necessary modules
})
export class PostsComponent implements OnInit {
  posts: Post[] = []; // Array to store published posts
  postForm: FormGroup;
  isEditor: boolean = false;

  constructor(
    private postService: PostService, 
    private fb: FormBuilder, 
    private router: Router // Inject Router for navigation
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
      status: ['DRAFT'], // Default status
    });
  }

  ngOnInit(): void {
    const role = localStorage.getItem('userRole') || 'VIEWER';
    this.isEditor = role === 'EDITOR';

    // Fetch the published posts
    this.postService.getPublishedPosts(role).subscribe({
      next: (data) => {
        this.posts = data; // Assign the response data to the posts array
      },
      error: (err) => console.error('Failed to load posts:', err),
    });
  }

  navigateToCreatePost(): void {
    this.router.navigate(['/create-post']); // Redirect to Create Post page
  }
}
