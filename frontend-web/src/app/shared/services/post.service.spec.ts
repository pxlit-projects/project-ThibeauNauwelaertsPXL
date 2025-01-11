import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { PostService } from './post.service';
import { Post } from '../models/post.model';

describe('PostService', () => {
  let service: PostService;
  let httpMock: HttpTestingController;

  const dummyPosts: Post[] = [
    { id: 1, title: 'title', content: 'Post 1 content', author: 'Author 1', status: 'PUBLISHED' },
    { id: 2, title: 'title', content: 'Post 2 content', author: 'Author 2', status: 'PUBLISHED' },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PostService],
    });

    // Inject the service and the mock controller
    service = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Ensure that no unmatched requests are outstanding
    httpMock.verify();
  });

  describe('#getPublishedPosts', () => {
    it('should fetch published posts (success)', () => {
      service.getPublishedPosts().subscribe((posts) => {
        expect(posts).toEqual(dummyPosts);
      });

      const req = httpMock.expectOne('http://localhost:8083/post/posts/published');
      expect(req.request.method).toBe('GET');

      // Simulate a successful response
      req.flush(dummyPosts);
    });

    it('should handle error (e.g., 500) when fetching published posts', () => {
      const errorMessage = 'Failed to fetch published posts.';
      service.getPublishedPosts().subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne('http://localhost:8083/post/posts/published');
      req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getDraftPosts', () => {
    it('should handle error when fetching draft posts', () => {
      const errorMessage = 'Failed to fetch draft posts.';
      service.getDraftPosts().subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(
        'http://localhost:8083/post/posts/drafts?'
      );
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#createPost', () => {
    it('should create a post (success)', () => {
      const newPost: Post = { id: 3, title: 'title', content: 'New post', author: 'Jane Doe', status: 'DRAFT' };

      service.createPost(newPost).subscribe((post) => {
        expect(post).toEqual(newPost);
      });

      const req = httpMock.expectOne('http://localhost:8083/post/posts');
      expect(req.request.method).toBe('POST');
      // Validate the request body if needed
      expect(req.request.body).toEqual(newPost);

      req.flush(newPost);
    });

    it('should handle error on createPost', () => {
      const newPost: Post = { id: 3, title: 'title', content: 'New post', author: 'Jane Doe', status: 'DRAFT' };
      const errorMessage = 'Failed to create the post.';

      service.createPost(newPost).subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne('http://localhost:8083/post/posts');
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getPostById', () => {
    it('should fetch a single post by ID (success)', () => {
      const postId = 1;

      service.getPostById(postId).subscribe((post) => {
        expect(post).toEqual(dummyPosts[0]);
      });

      const req = httpMock.expectOne(`http://localhost:8083/post/posts/${postId}`);
      expect(req.request.method).toBe('GET');

      req.flush(dummyPosts[0]);
    });

    it('should handle error when fetching a post by ID', () => {
      const postId = 999;
      const errorMessage = `Failed to fetch post with ID ${postId}.`;

      service.getPostById(postId).subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(`http://localhost:8083/post/posts/${postId}`);
      req.flush({}, { status: 404, statusText: 'Not Found' });
    });
  });

  describe('#updatePost', () => {
    it('should update an existing post (success)', () => {
      const updatedPost: Post = { id: 1, title: 'title', content: 'Updated', author: 'Author 1', status: 'DRAFT' };

      service.updatePost(updatedPost.id, updatedPost).subscribe((post) => {
        expect(post).toEqual(updatedPost);
      });

      const req = httpMock.expectOne(`http://localhost:8083/post/posts/${updatedPost.id}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatedPost);

      req.flush(updatedPost);
    });

    it('should handle error on updatePost', () => {
      const updatedPost: Post = { id: 1, title: 'title', content: 'Updated', author: 'Author 1', status: 'DRAFT' };
      const errorMessage = `Failed to update post with ID ${updatedPost.id}.`;

      service.updatePost(updatedPost.id, updatedPost).subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(`http://localhost:8083/post/posts/${updatedPost.id}`);
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getFilteredPosts', () => {
    it('should fetch filtered posts (success)', () => {
      const filters = {
        content: 'RxJS',
        lastModifiedDate: '2025-01-01'
      };

      service.getFilteredPosts(filters).subscribe((posts) => {
        expect(posts).toEqual(dummyPosts);
      });

      // Check the final URL with query params
      // lastModifiedDate is converted to ISO date (yyyy-MM-dd)
      const expectedDate = new Date('2025-01-01').toISOString().split('T')[0];
      const expectedUrl = `http://localhost:8083/post/posts/filtered?content=RxJS&lastModifiedDate=${expectedDate}`;

      const req = httpMock.expectOne(expectedUrl);
      expect(req.request.method).toBe('GET');
      req.flush(dummyPosts);
    });

    it('should handle error when fetching filtered posts', () => {
      const filters = { content: 'RxJS' };
      const errorMessage = 'Failed to fetch filtered posts.';

      service.getFilteredPosts(filters).subscribe({
        next: () => fail('expected an error, not data'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(
        `http://localhost:8083/post/posts/filtered?content=RxJS`
      );
      req.flush({}, { status: 500, statusText: 'Server Error' });
    });
  });
});
