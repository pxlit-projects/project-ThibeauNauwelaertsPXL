import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ReviewService } from './review.service';
import { Review } from '../models/review.model';
import { ReviewRequest } from '../models/ReviewRequest.model';
import { RejectRequest } from '../models/reject-request.model';

describe('ReviewService', () => {
  let service: ReviewService;
  let httpMock: HttpTestingController;

  // Corrected dummy data matching the Review interface
  const dummyReviews: Review[] = [
    {
      id: 1,
      postId: 101,
      author: 'Author 1',
      status: 'PENDING',
      submittedAt: '2025-01-01T10:00:00Z',
      remarks: '',
    },
    {
      id: 2,
      postId: 102,
      author: 'Author 2',
      status: 'APPROVED',
      submittedAt: '2025-01-03T12:00:00Z',
      reviewer: 'Reviewer 1',
      reviewedAt: '2025-01-04T15:30:00Z',
      remarks: 'Well done!',
    },
  ];

  const dummyResponseMessage = 'Operation successful';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ReviewService],
    });

    service = TestBed.inject(ReviewService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Ensure that no unmatched requests are outstanding
    httpMock.verify();
  });

  describe('#submitPostForReview', () => {
    it('should submit a post for review successfully', () => {
      const reviewRequest: ReviewRequest = {
        postId: 1,
        author: 'Author 1',
      };

      service.submitPostForReview(reviewRequest).subscribe((response) => {
        expect(response).toBe(dummyResponseMessage);
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews/submit');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(reviewRequest);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      expect(req.request.headers.get('X-User-Role')).toBe('editor');

      req.flush(dummyResponseMessage);
    });

    it('should handle error when submitting a post for review', () => {
      const reviewRequest: ReviewRequest = {
        postId: 1,
        author: 'Author 1',
      };
      const errorMessage = 'Failed to submit post for review.';

      service.submitPostForReview(reviewRequest).subscribe({
        next: () => fail('Expected an error, not a response'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews/submit');
      expect(req.request.method).toBe('POST');

      req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getAllReviews', () => {
    it('should fetch all reviews successfully', () => {
      service.getAllReviews().subscribe((reviews) => {
        expect(reviews.length).toBe(2);
        expect(reviews).toEqual(dummyReviews);
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      expect(req.request.headers.get('X-User-Role')).toBe('editor');

      req.flush(dummyReviews);
    });

    it('should handle error when fetching all reviews', () => {
      const errorMessage = 'Failed to fetch reviews';

      service.getAllReviews().subscribe({
        next: () => fail('Expected an error, not reviews'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews');
      expect(req.request.method).toBe('GET');

      req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#approveReview', () => {
    it('should approve a review successfully', () => {
      const reviewId = 1;
      const reviewer = 'Test Reviewer';

      service.approveReview(reviewId, reviewer).subscribe((response) => {
        expect(response).toBe(dummyResponseMessage);
      });

      const req = httpMock.expectOne(
        `http://localhost:8083/review/reviews/${reviewId}/approve?reviewer=${encodeURIComponent(
          reviewer
        )}`
      );
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toBeNull();
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      expect(req.request.headers.get('X-User-Role')).toBe('editor');

      req.flush(dummyResponseMessage);
    });

    it('should handle error when approving a review', () => {
      const reviewId = 1;
      const reviewer = 'Test Reviewer';
      const errorMessage = `Failed to approve review with ID ${reviewId}.`;

      service.approveReview(reviewId, reviewer).subscribe({
        next: () => fail('Expected an error, not a response'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(
        `http://localhost:8083/review/reviews/${reviewId}/approve?reviewer=${encodeURIComponent(
          reviewer
        )}`
      );
      expect(req.request.method).toBe('PUT');

      req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#rejectReview', () => {
    it('should reject a review successfully', () => {
      const reviewId = 2;
      const rejectRequest: RejectRequest = {
        reviewer: 'Test Reviewer',
        remarks: 'Inappropriate content',
      };

      service.rejectReview(reviewId, rejectRequest).subscribe((response) => {
        expect(response).toBe(dummyResponseMessage);
      });

      const req = httpMock.expectOne(`http://localhost:8083/review/reviews/${reviewId}/reject`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(rejectRequest);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      expect(req.request.headers.get('X-User-Role')).toBe('editor');

      req.flush(dummyResponseMessage);
    });

    it('should handle error when rejecting a review', () => {
      const reviewId = 2;
      const rejectRequest: RejectRequest = {
        reviewer: 'Test Reviewer',
        remarks: 'Inappropriate content',
      };
      const errorMessage = `Failed to reject review with ID ${reviewId}.`;

      service.rejectReview(reviewId, rejectRequest).subscribe({
        next: () => fail('Expected an error, not a response'),
        error: (error) => {
          expect(error.message).toBe(errorMessage);
        },
      });

      const req = httpMock.expectOne(`http://localhost:8083/review/reviews/${reviewId}/reject`);
      expect(req.request.method).toBe('PUT');

      req.flush({ message: 'Server Error' }, { status: 500, statusText: 'Server Error' });
    });
  });

  describe('#getAllReviews - Additional Test Cases', () => {
    it('should handle empty reviews array', () => {
      service.getAllReviews().subscribe((reviews) => {
        expect(reviews.length).toBe(0);
        expect(reviews).toEqual([]);
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews');
      expect(req.request.method).toBe('GET');

      req.flush([]);
    });
  });

  describe('#submitPostForReview - Additional Test Cases', () => {
    it('should handle missing fields in ReviewRequest', () => {
      // Assuming ReviewRequest requires postId and author, but title and content are optional
      const incompleteReviewRequest: ReviewRequest = {
        postId: 3,
        author: 'Author 3',
        // title and content are omitted
      };

      service.submitPostForReview(incompleteReviewRequest).subscribe((response) => {
        expect(response).toBe(dummyResponseMessage);
      });

      const req = httpMock.expectOne('http://localhost:8083/review/reviews/submit');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(incompleteReviewRequest);

      req.flush(dummyResponseMessage);
    });
  });
});
