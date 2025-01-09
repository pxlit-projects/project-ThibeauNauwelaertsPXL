import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Post, PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';

import { ReactiveFormsModule } from '@angular/forms';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css'],
})
export class CreatePostComponent implements OnInit {
  postForm: FormGroup;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private router: Router,
    private authService: AuthService
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
      status: ['DRAFT'],
    });
  }

  ngOnInit(): void {}

  createPost(): void {
    if (this.postForm.invalid) {
      return;
    }

    const newPost: Post = {
      ...this.postForm.value,
      author: this.authService.getUsername(), // Use the current username as author
    };

    this.postService.createPost(newPost).pipe(
      tap((post) => {
        console.log('Post created:', post);
        this.router.navigate(['/drafts']); // Navigate back to drafts
      }),
      catchError((error) => {
        console.error('Error creating post:', error);
        this.errorMessage = 'Failed to create post. Please try again later.';
        return of(null);
      })
    ).subscribe();
  }
}
