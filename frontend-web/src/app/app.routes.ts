import { Routes } from '@angular/router';
import { LoginComponent } from './core/login/login.component';
import { PostsComponent } from './core/posts/posts.component';
import { AuthGuard } from './guard/auth.guard';
import { CreatePostComponent } from './core/create-post/create-post.component';
import { EditPostComponent } from './core/edit-post/edit-post.component';
import { DraftPostsComponent } from './core/drafts/drafts.component';
import { ReviewsComponent } from './core/reviews/reviews.component'; // Import the ReviewsComponent
import { CommentsListComponent } from './core/comments/comments.component'; // Import the CommentsComponent

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'posts', component: PostsComponent, canActivate: [AuthGuard] }, // Protect with AuthGuard
  { path: 'drafts', component: DraftPostsComponent, canActivate: [AuthGuard] }, // Protect with AuthGuard
  { path: 'create-post', component: CreatePostComponent, canActivate: [AuthGuard] },
  { path: 'edit-post/:id', component: EditPostComponent },  // Route for editing posts
  { path: 'reviews', component: ReviewsComponent, canActivate: [AuthGuard] }, // Add route for ReviewsComponent
  { path: 'comments/:postId', component: CommentsListComponent, canActivate: [AuthGuard] }, // Include postId in the path
  { path: '', redirectTo: '/login', pathMatch: 'full' }, // Default route
  { path: '**', redirectTo: '/login' }, // Redirect unknown routes to login
];
