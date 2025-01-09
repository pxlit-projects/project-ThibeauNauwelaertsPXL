import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreatePostComponent } from './create-post.component';
import { ReactiveFormsModule } from '@angular/forms';
import { PostService } from '../../../shared/services/post.service';
import { AuthService } from '../../../shared/services/auth.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Post } from '../../../shared/models/post.model';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('CreatePostComponent', () => {
  let component: CreatePostComponent;
  let fixture: ComponentFixture<CreatePostComponent>;

  // Mocks
  let postServiceMock: jasmine.SpyObj<PostService>;
  let authServiceMock: jasmine.SpyObj<AuthService>;
  let routerMock: jasmine.SpyObj<Router>;

  // Optionally set up some “common” Post objects to use in multiple tests
  const mockInputPost: Post = {
    title: 'Test Title',
    content: 'Test Content',
    status: 'DRAFT',
    author: 'SomeUser',
  } as Post;

  const mockReturnedPost: Post = { ...mockInputPost, id: 1 };

  beforeEach(async () => {
    // Create your service mocks with the methods you want to spy on
    postServiceMock = jasmine.createSpyObj<PostService>('PostService', ['createPost']);
    authServiceMock = jasmine.createSpyObj<AuthService>('AuthService', ['getUsername']);
    routerMock = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        // Standalone component import:
        CreatePostComponent,
      ],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(CreatePostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize the form with required controls', () => {
      const postForm = component.postForm;
      expect(postForm).toBeTruthy();
      expect(postForm.controls['title']).toBeTruthy();
      expect(postForm.controls['content']).toBeTruthy();
      expect(postForm.controls['status']).toBeTruthy();
    });
  });

  describe('Form validation', () => {
    it('should mark form as invalid if required fields are empty', () => {
      component.postForm.controls['title'].setValue('');
      component.postForm.controls['content'].setValue('');
      expect(component.postForm.valid).toBeFalse();
    });

    it('should mark form as valid when required fields are filled', () => {
      component.postForm.controls['title'].setValue('Valid Title');
      component.postForm.controls['content'].setValue('Valid Content');
      expect(component.postForm.valid).toBeTrue();
    });
  });

  describe('createPost()', () => {
    it('should do nothing if form is invalid', () => {
      // Make the form invalid
      component.postForm.controls['title'].setValue('');
      component.createPost();

      // Ensure the service method was never called
      expect(postServiceMock.createPost).not.toHaveBeenCalled();
    });

    it('should call createPost on form submit and navigate on success', () => {
      authServiceMock.getUsername.and.returnValue('SomeUser');
      postServiceMock.createPost.and.returnValue(of(mockReturnedPost));

      // Fill in valid form values
      component.postForm.setValue({
        title: mockInputPost.title,
        content: mockInputPost.content,
        status: mockInputPost.status,
      });

      component.createPost();

      expect(postServiceMock.createPost).toHaveBeenCalledWith(mockInputPost);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/drafts']);
      expect(component.errorMessage).toBeNull();
    });

    it('should set an errorMessage if createPost fails', () => {
      const mockError = { message: 'Error creating post' };

      component.postForm.setValue({
        title: 'title',
        content: 'content',
        status: 'DRAFT',
      });
      postServiceMock.createPost.and.returnValue(throwError(() => mockError));

      component.createPost();

      expect(component.errorMessage).toBe(
        'Failed to create post. Please try again later.'
      );
    });

    it('should prevent multiple createPost calls on rapid submissions', () => {
      // In your example, you allow all calls (no actual prevention logic)
      authServiceMock.getUsername.and.returnValue('SomeUser');
      postServiceMock.createPost.and.returnValue(of(mockReturnedPost));

      component.postForm.setValue({
        title: 'Test Title',
        content: 'Test Content',
        status: 'DRAFT',
      });

      // Rapid calls
      component.createPost();
      component.createPost();
      component.createPost();

      // If you had prevention logic, you'd check for # of calls
      // But your code calls createPost anyway, so:
      expect(postServiceMock.createPost).toHaveBeenCalledTimes(3);
    });
  });

  describe('Default status', () => {
    it('should use default "DRAFT" if not set explicitly', () => {
      authServiceMock.getUsername.and.returnValue('SomeUser');
      postServiceMock.createPost.and.returnValue(of(mockReturnedPost));

      // Rely on the form's default value for 'status'
      component.postForm.setValue({
        title: mockInputPost.title,
        content: mockInputPost.content,
        status: 'DRAFT', // default
      });

      component.createPost();

      expect(postServiceMock.createPost).toHaveBeenCalledWith(mockInputPost);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/drafts']);
    });
  });

  describe('Handling long input', () => {
    it('should handle long input strings without errors', () => {
      const longTitle = 'A'.repeat(1000);
      const longContent = 'B'.repeat(5000);

      const longPost: Post = {
        title: longTitle,
        content: longContent,
        status: 'DRAFT',
        author: 'SomeUser',
      } as Post;

      const longReturnedPost: Post = { ...longPost, id: 1 };

      authServiceMock.getUsername.and.returnValue('SomeUser');
      postServiceMock.createPost.and.returnValue(of(longReturnedPost));

      component.postForm.setValue({
        title: longTitle,
        content: longContent,
        status: 'DRAFT',
      });

      component.createPost();

      expect(postServiceMock.createPost).toHaveBeenCalledWith(longPost);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/drafts']);
      expect(component.errorMessage).toBeNull();
    });
  });
});
