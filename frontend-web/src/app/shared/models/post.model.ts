export interface Post {
  id: number;
  title: string;
  content: string;
  status: string; 
  remarks?: string; 
  createdDate?: string;
  lastModifiedDate?: string;
  author?: string;
}