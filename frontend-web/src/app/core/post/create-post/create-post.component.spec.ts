import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreatePostComponent } from './create-post.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { AuthService } from '../../../shared/services/auth.service';
import { of, throwError } from 'rxjs';
import { CommonModule } from '@angular/common';

describe('CreatePostComponent', () => {
  let component: CreatePostComponent;
  let fixture: ComponentFixture<CreatePostComponent>;
  let postServiceSpy: jasmine.SpyObj<PostService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    postServiceSpy = jasmine.createSpyObj('PostService', ['createPost']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, CommonModule],
      declarations: [CreatePostComponent],
      providers: [
        FormBuilder,
        { provide: PostService, useValue: postServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreatePostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty values and default status as DRAFT', () => {
    const formValues = component.postForm.value;
    expect(formValues.title).toBe('');
    expect(formValues.content).toBe('');
    expect(formValues.status).toBe('DRAFT');
  });

  it('should not call postService.createPost if the form is invalid', () => {
    component.postForm.setValue({ title: '', content: '', status: 'DRAFT' });
    component.createPost();
    expect(postServiceSpy.createPost).not.toHaveBeenCalled();
  });

  it('should call postService.createPost with correct data when form is valid', () => {
    authServiceSpy.getUsername.and.returnValue('JohnDoe');

    component.postForm.setValue({ 
      title: 'Test Title', 
      content: 'Test Content', 
      status: 'DRAFT' 
    });

    const expectedPost = {
      id: 1, // Assuming `id` is required for Post
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
      author: 'JohnDoe'
    };

    postServiceSpy.createPost.and.returnValue(of(expectedPost));
    component.createPost();

    expect(postServiceSpy.createPost).toHaveBeenCalledWith(jasmine.objectContaining({
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
      author: 'JohnDoe'
    }));
  });

  it('should navigate to /drafts after successful post creation', () => {
    authServiceSpy.getUsername.and.returnValue('JohnDoe');

    component.postForm.setValue({ 
      title: 'Test Title', 
      content: 'Test Content', 
      status: 'DRAFT' 
    });

    const expectedPost = {
      id: 1,
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
      author: 'JohnDoe'
    };

    postServiceSpy.createPost.and.returnValue(of(expectedPost));
    component.createPost();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/drafts']);
  });

  it('should set errorMessage when createPost() fails', () => {
    const mockError = new Error('Server error');
    postServiceSpy.createPost.and.returnValue(throwError(() => mockError));

    component.postForm.setValue({ 
      title: 'Test Title', 
      content: 'Test Content', 
      status: 'DRAFT' 
    });

    component.createPost();

    expect(component.errorMessage).toBe('Failed to create post. Please try again later.');
    expect(postServiceSpy.createPost).toHaveBeenCalled();
  });

  it('should call authService.getUsername when creating a post', () => {
    authServiceSpy.getUsername.and.returnValue('JohnDoe');

    component.postForm.setValue({ 
      title: 'Test Title', 
      content: 'Test Content', 
      status: 'DRAFT' 
    });

    const expectedPost = {
      id: 1,
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
      author: 'JohnDoe'
    };

    postServiceSpy.createPost.and.returnValue(of(expectedPost));
    component.createPost();

    expect(authServiceSpy.getUsername).toHaveBeenCalled();
  });

  it('should not call postService.createPost if form is invalid', () => {
    component.postForm.setValue({ title: '', content: '', status: 'DRAFT' });
    component.createPost();

    expect(postServiceSpy.createPost).not.toHaveBeenCalled();
  });

  it('should call router.navigate when the post is created successfully', () => {
    authServiceSpy.getUsername.and.returnValue('JohnDoe');

    component.postForm.setValue({ 
      title: 'Test Title', 
      content: 'Test Content', 
      status: 'DRAFT' 
    });

    const expectedPost = {
      id: 1,
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
      author: 'JohnDoe'
    };

    postServiceSpy.createPost.and.returnValue(of(expectedPost));
    component.createPost();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/drafts']);
  });

  it('should update errorMessage if the createPost() fails', () => {
    const mockError = new Error('Failed to create post');
    postServiceSpy.createPost.and.returnValue(throwError(() => mockError));

    component.postForm.setValue({
      title: 'Test Title',
      content: 'Test Content',
      status: 'DRAFT',
    });

    component.createPost();

    expect(component.errorMessage).toBe('Failed to create post. Please try again later.');
    expect(postServiceSpy.createPost).toHaveBeenCalled();
  });

});
