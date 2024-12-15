import { Routes } from '@angular/router';
import { LoginComponent } from './core/login/login.component';
import { PostsComponent } from './core/post/posts/posts.component';
import { AuthGuard } from './guard/auth.guard';
import { CreatePostComponent } from './core/post/create-post/create-post.component';
import { EditPostComponent } from './core/post/edit-post/edit-post.component';
import { DraftPostsComponent } from './core/post/drafts/drafts.component';
import { ReviewsComponent } from './core/review/reviews/reviews.component'; // Import the ReviewsComponent
import { CommentsComponent } from './core/comment/comments-overview/comments-overview.component'; // Import the CommentsComponent

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'posts', component: PostsComponent, canActivate: [AuthGuard] }, // Protect with AuthGuard
  { path: 'drafts', component: DraftPostsComponent, canActivate: [AuthGuard] }, // Protect with AuthGuard
  { path: 'create-post', component: CreatePostComponent, canActivate: [AuthGuard] },
  { path: 'edit-post/:id', component: EditPostComponent },  // Route for editing posts
  { path: 'reviews', component: ReviewsComponent, canActivate: [AuthGuard] }, // Add route for ReviewsComponent
  { path: 'comments/:postId', component: CommentsComponent, canActivate: [AuthGuard] }, // Include postId in the path
  { path: '', redirectTo: '/login', pathMatch: 'full' }, // Default route
  { path: '**', redirectTo: '/login' }, // Redirect unknown routes to login
];
