import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostsComponent } from './posts.component'; // Adjust to your file path
import { PostService } from '../../../shared/services/post.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Post } from '../../../shared/models/post.model'; // Ensure you have this import

/**
 * Mock data adhering to the Post interface
 */
const mockPosts: Post[] = [
  {
    id: 1,
    title: 'First Post',
    content: 'Content of the first post',
    author: 'AuthorA',
    createdDate: '2023-01-01T10:00:00Z',
    lastModifiedDate: '2023-01-02T12:00:00Z',
    status: 'PUBLISHED',
  },
  {
    id: 2,
    title: 'Second Post',
    content: 'Content of the second post',
    author: 'AuthorB',
    createdDate: '2023-02-01T09:30:00Z',
    lastModifiedDate: '2023-02-02T11:15:00Z',
    status: 'DRAFT',
  },
];

describe('PostsComponent', () => {
  let component: PostsComponent;
  let fixture: ComponentFixture<PostsComponent>;

  // Spy objects (mocks)
  let postServiceSpy: jasmine.SpyObj<PostService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    // Create SpyObj for PostService and Router
    postServiceSpy = jasmine.createSpyObj('PostService', ['getPublishedPosts', 'getFilteredPosts']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        FormsModule,
        // Since PostsComponent is standalone, import it directly
        PostsComponent,
      ],
      providers: [
        { provide: PostService, useValue: postServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA], // Ignore unknown elements/attributes
    }).compileComponents();

    fixture = TestBed.createComponent(PostsComponent);
    component = fixture.componentInstance;
  });

  it('should create the PostsComponent', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    beforeEach(() => {
      // Reset spies before each test
      postServiceSpy.getPublishedPosts.calls.reset();
      postServiceSpy.getFilteredPosts.calls.reset();
      routerSpy.navigate.calls.reset();
      // Clear localStorage mock
      spyOn(localStorage, 'getItem').and.callFake((key: string): string | null => {
        if (key === 'userRole') {
          return 'VIEWER';
        }
        return null;
      });
    });

    it('should set isEditor to true if userRole is EDITOR and fetch published posts', () => {
      // Arrange
      (localStorage.getItem as jasmine.Spy).and.returnValue('editor'); // Changed to lowercase
      postServiceSpy.getPublishedPosts.and.returnValue(of(mockPosts));
    
      // Act
      fixture.detectChanges(); // Triggers ngOnInit
    
      // Assert
      expect(localStorage.getItem).toHaveBeenCalledWith('userRole');
      expect(component.isEditor).toBeTrue(); // Now passes
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(component.posts).toEqual(mockPosts);
    });
    

    it('should set isEditor to false if userRole is not editor and fetch published posts', () => {
      // Arrange
      (localStorage.getItem as jasmine.Spy).and.returnValue('VIEWER');
      postServiceSpy.getPublishedPosts.and.returnValue(of(mockPosts));

      // Act
      fixture.detectChanges(); // Triggers ngOnInit

      // Assert
      expect(localStorage.getItem).toHaveBeenCalledWith('userRole');
      expect(component.isEditor).toBeFalse();
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(component.posts).toEqual(mockPosts);
    });

    it('should handle error when fetching published posts fails', () => {
      // Arrange
      (localStorage.getItem as jasmine.Spy).and.returnValue('editor'); // Changed to lowercase
      postServiceSpy.getPublishedPosts.and.returnValue(
        throwError(() => new Error('Network error'))
      );
      spyOn(console, 'error');
    
      // Act
      fixture.detectChanges(); // Triggers ngOnInit
    
      // Assert
      expect(localStorage.getItem).toHaveBeenCalledWith('userRole');
      expect(component.isEditor).toBeTrue();
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(component.posts).toEqual([]);
      expect(console.error).toHaveBeenCalledWith('Failed to load posts:', jasmine.any(Error));
    });
    
  });

  describe('fetchPosts', () => {
    beforeEach(() => {
      // Reset spies before each test
      postServiceSpy.getPublishedPosts.calls.reset();
      postServiceSpy.getFilteredPosts.calls.reset();
      component.posts = [];
    });

    it('should fetch published posts when no filters are provided', () => {
      // Arrange
      postServiceSpy.getPublishedPosts.and.returnValue(of(mockPosts));

      // Act
      component.fetchPosts();

      // Assert
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(postServiceSpy.getFilteredPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual(mockPosts);
    });

    it('should fetch filtered posts when filters are provided', () => {
      // Arrange
      const filters: Partial<Post> = { author: 'AuthorA', status: 'PUBLISHED' };
      const filteredPosts: Post[] = [mockPosts[0]];
      postServiceSpy.getFilteredPosts.and.returnValue(of(filteredPosts));

      // Act
      component.fetchPosts(filters);

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith(filters);
      expect(postServiceSpy.getPublishedPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual(filteredPosts);
    });

    it('should handle error when fetching filtered posts fails', () => {
      // Arrange
      const filters: Partial<Post> = { author: 'AuthorA' };
      postServiceSpy.getFilteredPosts.and.returnValue(
        throwError(() => new Error('Filter error'))
      );
      spyOn(console, 'error');

      // Act
      component.fetchPosts(filters);

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith(filters);
      expect(postServiceSpy.getPublishedPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual([]);
      expect(console.error).toHaveBeenCalledWith('Failed to load filtered posts:', jasmine.any(Error));
    });
  });

  describe('applyFilters', () => {
    beforeEach(() => {
      // Reset component state before each test
      component.filterCriteria = {};
      component.posts = [];
      postServiceSpy.getFilteredPosts.calls.reset();
      postServiceSpy.getPublishedPosts.calls.reset();
    });

    it('should apply filters and fetch filtered posts', () => {
      // Arrange
      component.filterCriteria = {
        author: 'AuthorA',
        status: 'PUBLISHED',
        createdDate: '2023-01-01',
        lastModifiedDate: '2023-01-02',
        // Assuming other fields can be part of filterCriteria
      };
      const expectedFilters: Partial<Post> = {
        author: 'AuthorA',
        status: 'PUBLISHED',
        createdDate: '2023-01-01',
        lastModifiedDate: '2023-01-02',
      };
      postServiceSpy.getFilteredPosts.and.returnValue(of([mockPosts[0]]));

      // Act
      component.applyFilters();

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith(expectedFilters);
      expect(postServiceSpy.getPublishedPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual([mockPosts[0]]);
    });

    it('should convert createdDate and lastModifiedDate to ISO date strings', () => {
      // Arrange
      component.filterCriteria = {
        createdDate: '2023-01-01',
        lastModifiedDate: '2023-01-02',
      };
      const expectedFilters: Partial<Post> = {
        createdDate: '2023-01-01',
        lastModifiedDate: '2023-01-02',
      };
      postServiceSpy.getFilteredPosts.and.returnValue(of([mockPosts[0]]));

      // Act
      component.applyFilters();

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith(expectedFilters);
    });

    it('should remove empty filter fields before fetching', () => {
      // Arrange
      component.filterCriteria = {
        author: 'AuthorA',
        status: '',
        createdDate: undefined,
      };
      const expectedFilters: Partial<Post> = {
        author: 'AuthorA',
      };
      postServiceSpy.getFilteredPosts.and.returnValue(of([mockPosts[0]]));

      // Act
      component.applyFilters();

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith(expectedFilters);
      expect(postServiceSpy.getPublishedPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual([mockPosts[0]]);
    });

    it('should fetch published posts if no valid filters are provided', () => {
      // Arrange
      component.filterCriteria = {
        author: '',
        status: undefined,
        createdDate: '',
      };
      postServiceSpy.getPublishedPosts.and.returnValue(of(mockPosts));

      // Act
      component.applyFilters();

      // Assert
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(postServiceSpy.getFilteredPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual(mockPosts);
    });

    it('should handle error when fetching filtered posts fails', () => {
      // Arrange
      component.filterCriteria = {
        author: 'AuthorA',
      };
      postServiceSpy.getFilteredPosts.and.returnValue(
        throwError(() => new Error('Filter error'))
      );
      spyOn(console, 'error');

      // Act
      component.applyFilters();

      // Assert
      expect(postServiceSpy.getFilteredPosts).toHaveBeenCalledWith({ author: 'AuthorA' });
      expect(postServiceSpy.getPublishedPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual([]);
      expect(console.error).toHaveBeenCalledWith('Failed to load filtered posts:', jasmine.any(Error));
    });
  });

  describe('clearFilters', () => {
    beforeEach(() => {
      // Reset component state before each test
      component.filterCriteria = {
        author: 'AuthorA',
        status: 'PUBLISHED',
      };
      component.posts = [];
      postServiceSpy.getFilteredPosts.calls.reset();
      postServiceSpy.getPublishedPosts.calls.reset();
    });

    it('should clear filterCriteria and fetch published posts', () => {
      // Arrange
      postServiceSpy.getPublishedPosts.and.returnValue(of(mockPosts));

      // Act
      component.clearFilters();

      // Assert
      expect(component.filterCriteria).toEqual({});
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(postServiceSpy.getFilteredPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual(mockPosts);
    });

    it('should handle error when fetching published posts fails after clearing filters', () => {
      // Arrange
      postServiceSpy.getPublishedPosts.and.returnValue(
        throwError(() => new Error('Network error'))
      );
      spyOn(console, 'error');

      // Act
      component.clearFilters();

      // Assert
      expect(component.filterCriteria).toEqual({});
      expect(postServiceSpy.getPublishedPosts).toHaveBeenCalled();
      expect(postServiceSpy.getFilteredPosts).not.toHaveBeenCalled();
      expect(component.posts).toEqual([]);
      expect(console.error).toHaveBeenCalledWith('Failed to load posts:', jasmine.any(Error));
    });
  });

  describe('Navigation Methods', () => {
    beforeEach(() => {
      // Reset router spies before each test
      routerSpy.navigate.calls.reset();
    });

    it('should navigate to create-post when navigateToCreatePost is called', () => {
      // Act
      component.navigateToCreatePost();

      // Assert
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/create-post']);
    });

    it('should navigate to edit-post with postId when editPost is called', () => {
      // Arrange
      const postId = 1;

      // Act
      component.editPost(postId);

      // Assert
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/edit-post', postId]);
    });

    it('should navigate to comments with postId when viewComments is called', () => {
      // Arrange
      const postId = 2;

      // Act
      component.viewComments(postId);

      // Assert
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/comments', postId]);
    });
  });
});
