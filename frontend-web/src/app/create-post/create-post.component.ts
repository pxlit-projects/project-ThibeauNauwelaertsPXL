import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PostService } from '../services/post.service';
import { Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms'; // Import ReactiveFormsModule
import { CommonModule } from '@angular/common'; // Import CommonModule for standalone component
import { AuthService } from '../login/auth.service';

@Component({
  selector: 'app-create-post',
  standalone: true, // Mark the component as standalone
  imports: [ReactiveFormsModule, CommonModule], // Add necessary modules here
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css'],
})
export class CreatePostComponent implements OnInit {
  postForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private router: Router,
    private authService: AuthService // Inject AuthService
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
      status: ['DRAFT'], // Default status
    });
  }

  ngOnInit(): void {}

  createPost(): void {
    if (this.postForm.invalid) {
      return;
    }

    const newPost = {
      ...this.postForm.value,
      author: this.authService.getUsername(), // Get the current username as author
    };

    const role = 'EDITOR'; // Use appropriate role logic

    this.postService.createPost(newPost).subscribe(
      (post) => {
        console.log('Post created:', post);
        this.router.navigate(['/posts']); // Navigate back to the posts page after creation
      },
      (error) => {
        console.error('Error creating post:', error);
      }
    );
  }
}
