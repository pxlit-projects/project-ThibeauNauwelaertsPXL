// src/app/models/notification-message.model.ts

export class NotificationMessage {
    postId: number;
    status: string;
    reviewer: string;
    remarks?: string;
  
    constructor(postId: number, status: string, reviewer: string, remarks?: string) {
      this.postId = postId;
      this.status = status;
      this.reviewer = reviewer;
      this.remarks = remarks;
    }
  }
  