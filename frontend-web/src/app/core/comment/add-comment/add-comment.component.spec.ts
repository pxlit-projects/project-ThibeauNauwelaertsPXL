import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddCommentComponent } from './add-comment.component';
import { CommentService } from '../../../shared/services/comment.service';
import { AuthService } from '../../../shared/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Comment } from '../../../shared/models/comment.model';

describe('AddCommentComponent', () => {
  let component: AddCommentComponent;
  let fixture: ComponentFixture<AddCommentComponent>;

  // Spies
  let commentServiceSpy: jasmine.SpyObj<CommentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    commentServiceSpy = jasmine.createSpyObj('CommentService', ['addCommentToPost']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);

    await TestBed.configureTestingModule({
      imports: [
        FormsModule, 
        AddCommentComponent,
      ],
      providers: [
        { provide: CommentService, useValue: commentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AddCommentComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('#addComment', () => {
    it('should log an error and return if postId is missing', () => {
      // Arrange
      component.postId = undefined as any; 
      spyOn(console, 'error');

      // Act
      component.addComment();

      // Assert
      expect(console.error).toHaveBeenCalledWith('Post ID is missing. Cannot add a comment.');
      expect(commentServiceSpy.addCommentToPost).not.toHaveBeenCalled();
    });

    it('should handle error when addCommentToPost fails', () => {
      // Arrange
      component.postId = 456;
      authServiceSpy.getUsername.and.returnValue('FailUser');
      spyOn(console, 'error');
      commentServiceSpy.addCommentToPost.and.returnValue(
        throwError(() => new Error('Some error'))
      );

      // Act
      component.addComment();

      // Assert
      expect(console.error).toHaveBeenCalledWith('Failed to add comment:', jasmine.any(Error));

    });
  });
});
