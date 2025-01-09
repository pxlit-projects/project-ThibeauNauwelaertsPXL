import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {PostService } from '../../../shared/services/post.service';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { Post } from '../../../shared/models/post.model';

@Component({
  selector: 'app-edit-post',
  imports: [ReactiveFormsModule],
  standalone: true,
  templateUrl: './edit-post.component.html',
  styleUrls: ['./edit-post.component.css'],
})
export class EditPostComponent implements OnInit {
  postForm: FormGroup; // Form to edit the post
  postId: number = 0; // To store the post ID from URL
  role: string = ''; // Role of the current user
  currentUser: string | null = ''; // Store the logged-in user's username
  isAuthorized: boolean = false; // Check if the user is authorized to edit the post

  constructor(
    private fb: FormBuilder,
    private postService: PostService,
    private router: Router,
    private route: ActivatedRoute, // To get the post ID from the URL
    private authService: AuthService // Inject AuthService to retrieve role
  ) {
    this.postForm = this.fb.group({
      title: ['', Validators.required],
      content: ['', Validators.required],
    });
    
  }

  ngOnInit(): void {
    // Get the user role and username from AuthService
    this.role = this.authService.getRole();
    this.currentUser = this.authService.getUsername();

    // Get the post ID from the route parameters
    this.postId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.postId) {
      this.loadPost(this.postId); // If a post ID exists, fetch the post
    }
  }

  loadPost(id: number): void {
    this.postService.getPostById(id).subscribe({
      next: (post: Post) => {
        console.log('Post fetched:', post);

        // Check if the current user is the author of the post
        if (post.author !== this.currentUser) {
          console.error('Unauthorized: You are not the author of this post');
          this.router.navigate(['/posts']); // Redirect to posts if unauthorized
          return;
        }

        this.isAuthorized = true; // Allow editing if the user is authorized

        // Populate the form with the post details
        this.postForm.patchValue({
          title: post.title,
          content: post.content,
          status: post.status,
        });
      },
      error: (err) => {
        console.error('Failed to load post:', err);
      },
    });
  }

  updatePost(): void {
    if (this.postForm.invalid || !this.isAuthorized) {
      return;
    }

    const updatedPost = this.postForm.value;
    updatedPost.id = this.postId; // Make sure the post ID is included

    // Call the post service to update the post
    this.postService.updatePost(this.postId, updatedPost).subscribe({
      next: (post) => {
        console.log('Post updated:', post);
        this.router.navigate(['/posts']); // Navigate back to the posts list after updating
      },
      error: (error) => {
        console.error('Error updating post:', error);
      },
    });
  }
}
