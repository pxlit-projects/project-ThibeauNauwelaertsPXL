import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { PostsComponent } from './posts/posts.component';
import { AuthGuard } from './login/auth.guard';
import { CreatePostComponent } from './create-post/create-post.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'posts', component: PostsComponent, canActivate: [AuthGuard] }, // Protect with AuthGuard
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'create-post', component: CreatePostComponent, canActivate: [AuthGuard] },
];
