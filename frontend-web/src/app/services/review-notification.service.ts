import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { NotificationMessage } from '../models/notification-message.model'; // Import model

@Injectable({
  providedIn: 'root',
})
export class ReviewNotificationService {
  private reviewUpdateSubject: Subject<NotificationMessage> = new Subject<NotificationMessage>();

  // Send review updates to subscribers
  getReviewUpdate() {
    return this.reviewUpdateSubject.asObservable();
  }

  // Receive and emit updates (e.g., when a review is approved/rejected)
  receiveReviewUpdate(notification: NotificationMessage): void {
    this.reviewUpdateSubject.next(notification);
  }
}
