import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReviewService } from '../../../shared/services/review.service';
import { AuthService } from '../../../shared/services/auth.service';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RejectRequest } from '../../../shared/models/reject-request.model';
import { ReviewActionsComponent } from './reviews-actions.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ReviewActionsComponent', () => {
  let component: ReviewActionsComponent;
  let fixture: ComponentFixture<ReviewActionsComponent>;

  let reviewServiceSpy: jasmine.SpyObj<ReviewService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    // Create spy objects for the services
    reviewServiceSpy = jasmine.createSpyObj('ReviewService', [
      'approveReview',
      'rejectReview',
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);

    await TestBed.configureTestingModule({
      imports: [ReviewActionsComponent],
      providers: [
        { provide: ReviewService, useValue: reviewServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewActionsComponent);
    component = fixture.componentInstance;
    // Set an initial reviewId for testing
    component.reviewId = 42; 
  });

  it('should create', () => {
    fixture.detectChanges(); // triggers ngOnInit if present
    expect(component).toBeTruthy();
  });

  describe('#approve()', () => {
    it('should handle error if approveReview fails', () => {
      // Arrange
      authServiceSpy.getUsername.and.returnValue('testuser');
      reviewServiceSpy.approveReview.and.returnValue(
        throwError(() => new Error('Approve error'))
      );
      spyOn(console, 'error');

      // Act
      component.approve();

      // Assert
      expect(reviewServiceSpy.approveReview).toHaveBeenCalledWith(42, 'testuser');
      expect(console.error).toHaveBeenCalledWith('Error approving review 42:', jasmine.any(Error));
    });
  });

  describe('#reject()', () => {
    let originalPrompt: (message?: string) => string | null;

    beforeAll(() => {
      // Save a reference to the original prompt
      originalPrompt = window.prompt;
    });

    afterAll(() => {
      // Restore the original prompt after all tests
      window.prompt = originalPrompt;
    });

    it('should do nothing if the user cancels prompt (no remarks)', () => {
      // Arrange
      // Mock prompt to return null
      window.prompt = jasmine.createSpy().and.returnValue(null);
      spyOn(component.reviewRejected, 'emit');

      // Act
      component.reject();

      // Assert
      expect(reviewServiceSpy.rejectReview).not.toHaveBeenCalled();
      expect(component.reviewRejected.emit).not.toHaveBeenCalled();
    });

    it('should handle error if rejectReview fails', () => {
      // Arrange
      window.prompt = jasmine.createSpy().and.returnValue('Some remarks');
      authServiceSpy.getUsername.and.returnValue('testuser');
      reviewServiceSpy.rejectReview.and.returnValue(
        throwError(() => new Error('Reject error'))
      );
      spyOn(console, 'error');

      // Act
      component.reject();

      // Assert
      const expectedRequest: RejectRequest = { reviewer: 'testuser', remarks: 'Some remarks' };
      expect(reviewServiceSpy.rejectReview).toHaveBeenCalledWith(42, expectedRequest);
      expect(console.error).toHaveBeenCalledWith('Error rejecting review 42:', jasmine.any(Error));
    });
  });
});
