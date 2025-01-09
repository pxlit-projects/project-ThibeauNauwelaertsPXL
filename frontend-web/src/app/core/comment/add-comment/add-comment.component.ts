import { Component, Input, Output, EventEmitter } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { CommentService } from '../../../shared/services/comment.service';
import { AuthService } from '../../../shared/services/auth.service';
import { Comment } from '../../../shared/models/comment.model';

@Component({
  selector: 'app-add-comment',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './add-comment.component.html',
  styleUrls: ['./add-comment.component.css'],
})
export class AddCommentComponent {
  @Input() postId!: number; 
  @Output() commentAdded = new EventEmitter<void>(); 

  newComment: Comment = { id: 0, postId: 0, author: '', content: '' };

  constructor(
    private commentService: CommentService,
    private authService: AuthService
  ) {}

  addComment(): void {
    if (!this.postId) {
      console.error('Post ID is missing. Cannot add a comment.');
      return;
    }

    const username = this.authService.getUsername() || 'Anonymous';
    this.newComment.postId = this.postId;
    this.newComment.author = username;

    this.commentService.addCommentToPost(this.newComment.postId, this.newComment).subscribe({
      next: () => {
        this.newComment.content = '';
        this.commentAdded.emit(); 
      },
      error: (err) => {
        console.error('Failed to add comment:', err);
      },
    });
  }
}
