// import { ComponentFixture, TestBed } from '@angular/core/testing';
// import { ReviewsComponent } from './reviews.component'; // Adjust to your file path
// import { ReviewService } from '../../../shared/services/review.service';
// import { ReviewNotificationService } from '../../../shared/services/review-notification.service';
// import { AuthService } from '../../../shared/services/auth.service';
// import { Router } from '@angular/router';
// import { of, throwError, Subject } from 'rxjs';
// import { NO_ERRORS_SCHEMA } from '@angular/core';
// import { NotificationMessage } from '../../../shared/models/notification-message.model';
// import { Review } from '../../../shared/models/review.model';

// const mockReviews: Review[] = [
//   {
//     id: 10,
//     postId: 123,
//     author: 'UserA',
//     status: 'PENDING',
//     reviewer: 'randomReviewer',
//     remarks: 'good content',
//     submittedAt: '2023-01-01T10:00:00Z',
//     reviewedAt: '2023-01-01T10:00:00Z',
//   },
//   {
//     id: 20,
//     postId: 123,
//     author: 'UserB',
//     status: 'PENDING',
//     reviewer: 'randomDude',
//     remarks: 'bad content',
//     submittedAt: '2023-01-02T12:00:00Z',
//     reviewedAt: '2023-01-02T12:00:00Z',
//   },
// ];


// describe('ReviewsComponent', () => {
//   let component: ReviewsComponent;
//   let fixture: ComponentFixture<ReviewsComponent>;

//   // Spies (mocks)
//   let reviewServiceSpy: jasmine.SpyObj<ReviewService>;
//   let reviewNotificationSpy: jasmine.SpyObj<ReviewNotificationService>;
//   let authServiceSpy: jasmine.SpyObj<AuthService>;
//   let routerSpy: jasmine.SpyObj<Router>;

//   // We’ll use a Subject to simulate notifications from ReviewNotificationService
//   let reviewNotificationSubject: Subject<NotificationMessage>;

//   beforeEach(async () => {
//     reviewServiceSpy = jasmine.createSpyObj('ReviewService', ['getAllReviews']);
//     reviewNotificationSpy = jasmine.createSpyObj('ReviewNotificationService', ['getReviewUpdate']);
//     authServiceSpy = jasmine.createSpyObj('AuthService', ['getUsername']);
//     routerSpy = jasmine.createSpyObj('Router', ['navigate']);

//     // The Subject will let us push notifications to the component
//     reviewNotificationSubject = new Subject<NotificationMessage>();
//     // Make getReviewUpdate() return an observable from the subject
//     reviewNotificationSpy.getReviewUpdate.and.returnValue(reviewNotificationSubject.asObservable());

//     await TestBed.configureTestingModule({
//       // Because ReviewsComponent is standalone, just import it directly:
//       imports: [ReviewsComponent],
//       providers: [
//         { provide: ReviewService, useValue: reviewServiceSpy },
//         { provide: ReviewNotificationService, useValue: reviewNotificationSpy },
//         { provide: AuthService, useValue: authServiceSpy },
//         { provide: Router, useValue: routerSpy },
//       ],
//       // NO_ERRORS_SCHEMA is optional. It allows ignoring unknown HTML attributes/components
//       schemas: [NO_ERRORS_SCHEMA],
//     }).compileComponents();

//     fixture = TestBed.createComponent(ReviewsComponent);
//     component = fixture.componentInstance;
//   });

//   it('should create', () => {
//     // By default, we haven't called fixture.detectChanges() yet
//     expect(component).toBeTruthy();
//   });

//   describe('ngOnInit', () => {
//     it('should load reviews and subscribe to notifications', () => {
//       // Arrange
//       reviewServiceSpy.getAllReviews.and.returnValue(of(mockReviews));
//       // Act
//       fixture.detectChanges(); // triggers ngOnInit

//       // Assert
//       expect(component.loading).toBeFalse();
//       expect(component.reviews.length).toBe(2);
//       expect(reviewServiceSpy.getAllReviews).toHaveBeenCalled();
//       // We also check that the component has subscribed to reviewNotificationService
//       expect(reviewNotificationSpy.getReviewUpdate).toHaveBeenCalled();
//     });

//     it('should handle error when loading reviews fails', () => {
//       // Arrange
//       reviewServiceSpy.getAllReviews.and.returnValue(
//         throwError(() => new Error('Failed to load reviews'))
//       );
//       spyOn(console, 'error');

//       // Act
//       fixture.detectChanges(); // triggers ngOnInit

//       // Assert
//       expect(component.loading).toBeFalse();
//       expect(component.reviews.length).toBe(0);
//       expect(component.errorMessage).toBe('Failed to load reviews.');
//       expect(console.error).toHaveBeenCalledWith('Error loading reviews', jasmine.any(Error));
//     });
//   });

//   describe('#handleReviewNotification', () => {
//     beforeEach(() => {
//       // Arrange: The component has loaded some reviews
//       reviewServiceSpy.getAllReviews.and.returnValue(of(mockReviews));
//       fixture.detectChanges();
//     });

//     it('should update the local review if it exists', () => {
//       // Suppose we receive a notification that the review with ID 10 is APPROVED
//       const notification: NotificationMessage = {
//         postId: 10,
//         status: 'approved',
//         reviewer: 'testUser',
//         remarks: '',
//       };

//       // Act: push the notification
//       reviewNotificationSubject.next(notification);

//       // Assert
//       const updated = component.reviews.find((r) => r.reviewId === 10);
//       expect(updated?.status).toBe('APPROVED'); // note uppercase
//       expect(updated?.reviewer).toBe('testUser');
//       expect(updated?.remarks).toBe('None'); // because remarks was empty string, so defaults to 'None'
//     });

//     it('should remove the review from list if REJECTED', () => {
//       // Suppose we receive a notification that the review with ID 20 is REJECTED
//       const notification: NotificationMessage = {
//         postId: 20,
//         status: 'rejected',
//         reviewer: 'rejectUser',
//         remarks: 'Bad content',
//       };

//       // Act
//       reviewNotificationSubject.next(notification);

//       // Assert
//       // The component should have removed the review with ID 20
//       expect(component.reviews.find((r) => r.reviewId === 20)).toBeUndefined();
//     });
//   });

//   describe('#removeReviewFromList', () => {
//     beforeEach(() => {
//       // load initial reviews
//       reviewServiceSpy.getAllReviews.and.returnValue(of(mockReviews));
//       fixture.detectChanges();
//     });

//     it('should remove review by ID from the local array', () => {
//       // Act
//       component.removeReviewFromList(10);

//       // Assert
//       expect(component.reviews.length).toBe(1);
//     });
//   });
// });
