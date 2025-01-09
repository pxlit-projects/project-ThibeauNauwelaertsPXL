export interface Review {
    id: number;
    postId: number;
    author: string;
    status: string;
    submittedAt?: string;
    reviewer?: string;
    reviewedAt?: string;
    remarks?: string;
  }