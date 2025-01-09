import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommentsComponent } from './comments-overview.component';
import { CommentService } from '../../../shared/services/comment.service';
import { AuthService } from '../../../shared/services/auth.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Comment } from '../../../shared/models/comment.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';

const mockComments: Comment[] = [
  { id: 1, postId: 123, author: 'UserA', content: 'First comment', createdAt: '2023-01-01T10:00:00Z' },
  { id: 2, postId: 123, author: 'UserB', content: 'Second comment', createdAt: '2023-01-02T12:00:00Z' },
  { id: 3, postId: 123, author: 'UserC', content: 'Third comment', createdAt: '2023-01-03T09:00:00Z' },
];

describe('CommentsComponent', () => {
  let component: CommentsComponent;
  let fixture: ComponentFixture<CommentsComponent>;

  // Spy objects (mocking the real services)
  let commentServiceSpy: jasmine.SpyObj<CommentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    // Create SpyObj for the services
    commentServiceSpy = jasmine.createSpyObj('CommentService', [
      'getCommentsByPostId',
      'updateComment',
      'deleteComment',
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);

    // Mock ActivatedRoute to supply a postId of 123
    const activatedRouteMock = {
      params: of({ postId: '123' }), // route.params => { postId: "123" }
    };

    await TestBed.configureTestingModule({
      imports: [
        CommentsComponent

      ],
      providers: [
        { provide: CommentService, useValue: commentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      // NO_ERRORS_SCHEMA is optional. It allows us to ignore unknown elements/attributes
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CommentsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should fetch comments for the given postId', () => {
      // Arrange
      authServiceSpy.getUsername.and.returnValue('MockUser');
      commentServiceSpy.getCommentsByPostId.and.returnValue(of(mockComments));

      // Act
      fixture.detectChanges(); // Triggers ngOnInit

      // Assert
      expect(component.postId).toBe(123);
      expect(authServiceSpy.getUsername).toHaveBeenCalled();
      expect(commentServiceSpy.getCommentsByPostId).toHaveBeenCalledWith(123);
      expect(component.comments.length).toBe(3);
    });

    describe('#onCommentAdded', () => {
      it('should re-fetch comments when a comment is added', () => {
        // Arrange
        commentServiceSpy.getCommentsByPostId.and.returnValue(of(mockComments));
        // Act
        component.onCommentAdded();
        // Assert
        expect(commentServiceSpy.getCommentsByPostId).toHaveBeenCalledWith(component.postId);
        expect(component.comments.length).toEqual(3);
      });
    });

    describe('#editComment', () => {
      it('should call updateComment and replace the comment in the array', () => {
        // Arrange
        component.comments = [...mockComments];
        const updatedComment: Comment = {
          id: 2, // We'll update the 2nd comment
          postId: 123,
          author: 'UserB',
          content: 'Updated Content',
          createdAt: '2023-01-02T12:00:00Z',
        };
        commentServiceSpy.updateComment.and.returnValue(of(updatedComment));

        // Act
        component.editComment(updatedComment);

        // Assert
        expect(commentServiceSpy.updateComment).toHaveBeenCalledWith(2, updatedComment);
        // The local comment object should be updated
        const index = component.comments.findIndex((c) => c.id === 2);
        expect(component.comments[index].content).toBe('Updated Content');
        // Edit mode should be turned off for that comment
        expect(component.editMode[2]).toBeFalse();
      });
    });

    describe('#deleteComment', () => {
      it('should log error if commentId is not provided', () => {
        // Arrange
        spyOn(console, 'error');

        // Act
        component.deleteComment(0); // invalid ID

        // Assert
        expect(console.error).toHaveBeenCalledWith('Comment ID is missing. Cannot delete.');
        expect(commentServiceSpy.deleteComment).not.toHaveBeenCalled();
      });

      it('should handle error when deleteComment fails', () => {
        // Arrange
        component.comments = [...mockComments];
        authServiceSpy.getUsername.and.returnValue('UserDeleting');
        spyOn(console, 'error');
        commentServiceSpy.deleteComment.and.returnValue(
          throwError(() => new Error('Delete error'))
        );

        // Act
        component.deleteComment(1);

        // Assert
        expect(commentServiceSpy.deleteComment).toHaveBeenCalledWith(1, 'UserDeleting');
        expect(console.error).toHaveBeenCalledWith(
          'Failed to delete comment:',
          jasmine.any(Error)
        );
        // The comment with id=1 should still exist
        expect(component.comments.find((c) => c.id === 1)).toBeTruthy();
      });
    });

    describe('#toggleEditMode', () => {
      it('should toggle edit mode for a given comment ID', () => {
        // Initially, editMode is empty
        expect(component.editMode[1]).toBeUndefined();

        // Turn it on
        component.toggleEditMode(1);
        expect(component.editMode[1]).toBeTrue();

        // Turn it off
        component.toggleEditMode(1);
        expect(component.editMode[1]).toBeFalse();
      });
    });
  })
});
