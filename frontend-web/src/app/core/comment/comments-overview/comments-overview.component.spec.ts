import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommentsComponent } from './comments-overview.component';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CommentService } from '../../../shared/services/comment.service';
import { AuthService } from '../../../shared/services/auth.service';
import { provideHttpClient } from '@angular/common/http'; // New import
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AddCommentComponent } from '../add-comment/add-comment.component';
import { Comment } from '../../../shared/models/comment.model';


describe('CommentsComponent', () => {
  let component: CommentsComponent;
  let fixture: ComponentFixture<CommentsComponent>;
  let commentServiceSpy: jasmine.SpyObj<CommentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockComments: Comment[] = [
    { id: 1, postId: 1, author: 'John Doe', content: 'First comment', createdAt: '2023-12-01' },
    { id: 2, postId: 1, author: 'Jane Doe', content: 'Second comment', createdAt: '2023-12-02' },
  ];

  beforeEach(async () => {
    commentServiceSpy = jasmine.createSpyObj('CommentService', [
      'getCommentsByPostId',
      'addCommentToPost',
      'updateComment',
      'deleteComment'
    ]);

    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);

    await TestBed.configureTestingModule({
      imports: [
        CommonModule,
        FormsModule,
        AddCommentComponent,
        CommentsComponent
      ],
      providers: [
        provideHttpClient(), // Replaces HttpClientTestingModule
        { provide: CommentService, useValue: commentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ postId: '1' }) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch comments on init', () => {
    commentServiceSpy.getCommentsByPostId.and.returnValue(of(mockComments));
    component.ngOnInit();
    expect(commentServiceSpy.getCommentsByPostId).toHaveBeenCalledWith(1);
    expect(component.comments).toEqual(
      mockComments.sort((a, b) => new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime())
    );
  });

  it('should handle errors while fetching comments', () => {
    const errorResponse = new Error('Failed to fetch comments');
    commentServiceSpy.getCommentsByPostId.and.returnValue(throwError(() => errorResponse));
    spyOn(console, 'error');
    component.fetchComments();
    expect(console.error).toHaveBeenCalledWith('Failed to fetch comments:', errorResponse);
    expect(component.comments).toEqual([]);
  });

  it('should add a comment and refresh the list', () => {
    const newComment: Comment = { id: 3, postId: 1, author: 'John Doe', content: 'New comment', createdAt: '2023-12-03' };
    commentServiceSpy.addCommentToPost.and.returnValue(of(newComment));
    commentServiceSpy.getCommentsByPostId.and.returnValue(of([newComment, ...mockComments]));
    component.onCommentAdded();
    expect(commentServiceSpy.getCommentsByPostId).toHaveBeenCalledWith(1);
    expect(component.comments[0]).toEqual(newComment);
  });

  it('should edit a comment', () => {
    const updatedComment: Comment = { ...mockComments[0], content: 'Updated content' };
    commentServiceSpy.updateComment.and.returnValue(of(updatedComment));
    component.comments = [...mockComments];
    component.editComment(updatedComment);
    expect(commentServiceSpy.updateComment).toHaveBeenCalledWith(updatedComment.id, updatedComment);
    expect(component.comments[0].content).toBe('Updated content');
  });

  it('should handle errors while editing a comment', () => {
    const updatedComment: Comment = { ...mockComments[0], content: 'Updated content' };
    const errorResponse = new Error('Failed to update comment');
    commentServiceSpy.updateComment.and.returnValue(throwError(() => errorResponse));
    spyOn(console, 'error');
    component.editComment(updatedComment);
    expect(console.error).toHaveBeenCalledWith('Failed to edit comment:', errorResponse);
  });

  it('should toggle edit mode', () => {
    component.editMode[1] = false;
    component.toggleEditMode(1);
    expect(component.editMode[1]).toBeTrue();
    component.toggleEditMode(1);
    expect(component.editMode[1]).toBeFalse();
  });

  it('should delete a comment', () => {
    const commentId = 1;
    commentServiceSpy.deleteComment.and.returnValue(of(void 0));
    component.comments = [...mockComments];
    component.deleteComment(commentId);
    expect(commentServiceSpy.deleteComment).toHaveBeenCalledWith(commentId, jasmine.any(String));
    expect(component.comments.find(c => c.id === commentId)).toBeUndefined();
  });

  it('should handle errors while deleting a comment', () => {
    const commentId = 1;
    const errorResponse = new Error('Failed to delete comment');
    commentServiceSpy.deleteComment.and.returnValue(throwError(() => errorResponse));
    spyOn(console, 'error');
    component.deleteComment(commentId);
    expect(console.error).toHaveBeenCalledWith('Failed to delete comment:', errorResponse);
  });
});