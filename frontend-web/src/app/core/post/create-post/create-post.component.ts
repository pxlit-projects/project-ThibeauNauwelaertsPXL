import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Post, PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-create-post',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css'],
})
export class CreatePostComponent implements OnInit {
  postForm: FormGroup;

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

    this.postService.createPost(newPost).subscribe(
      (post) => {
        console.log('Post created:', post);
        // Emit the post to the DraftPostsComponent
        this.router.navigate(['/drafts']); // Navigate back to drafts
      },
      (error) => {
        console.error('Error creating post:', error);
      }
    );
  }
}
