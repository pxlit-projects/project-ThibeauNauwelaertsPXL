export interface Comment {
    id: number;
    postId: number;
    author: string;
    content: string;
    createdAt?: string; // Optional field for frontend convenience
  }