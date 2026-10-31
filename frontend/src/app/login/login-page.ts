import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/errors';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule],
  templateUrl: './login-page.html',
})
export class LoginPage {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected name = '';
  protected readonly error = signal('');
  protected readonly busy = signal(false);

  constructor() {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/']);
    }
  }

  protected async submit(): Promise<void> {
    const name = this.name.trim();
    if (!name) {
      this.error.set('Please enter your name.');
      return;
    }
    this.busy.set(true);
    this.error.set('');
    try {
      await this.auth.login(name);
      await this.router.navigate(['/']);
    } catch (error) {
      this.error.set(errorMessage(error));
    } finally {
      this.busy.set(false);
    }
  }
}
