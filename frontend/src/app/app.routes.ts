import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LibraryPage } from './library/library-page';
import { LoginPage } from './login/login-page';

export const routes: Routes = [
  { path: 'login', component: LoginPage },
  { path: '', component: LibraryPage, canActivate: [authGuard] },
  { path: '**', redirectTo: '' },
];
