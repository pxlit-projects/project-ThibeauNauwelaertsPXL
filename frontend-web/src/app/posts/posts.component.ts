import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Post, PostService } from '../services/post.service';
import { CommonModule, NgForOf } from '@angular/common'; // Use provideHttpClient

@Component({
  selector: 'app-posts',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, NgForOf], // No need for HttpClientModule
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css'],
})
export class PostsComponent implements OnInit {
  posts: Post[] = [];
  postForm: FormGroup;
  isEditor: boolean = false;

  constructor(private postService: PostService, private fb: FormBuilder) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
      status: ['DRAFT'], // Default status
    });
  }

  ngOnInit(): void {
    const role = localStorage.getItem('userRole') || 'VIEWER';
    this.isEditor = role === 'EDITOR';

    this.postService.getPublishedPosts(role).subscribe({
      next: (data) => (this.posts = data),
      error: (err) => console.error('Failed to load posts:', err),
    });
  }

  createPost(): void {
    if (this.postForm.invalid) return;

    const role = localStorage.getItem('userRole') || 'VIEWER';
    this.postService.createPost(role, this.postForm.value).subscribe({
      next: (newPost) => {
        this.posts.push(newPost);
        this.postForm.reset({ status: 'DRAFT' });
      },
      error: (err) => console.error('Failed to create post:', err),
    });
  }
}
