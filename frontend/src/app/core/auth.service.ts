import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ActionResponse, LoginResponse, Session } from './models';

const STORAGE_KEY = 'library.session';

function readStoredSession(): Session | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly session = signal<Session | null>(readStoredSession());
  readonly welcome = signal<string[]>([]);
  readonly farewell = signal('');
  readonly isLoggedIn = computed(() => this.session() !== null);

  token(): string | null {
    return this.session()?.token ?? null;
  }

  async login(name: string): Promise<void> {
    const response = await firstValueFrom(this.http.post<LoginResponse>('/api/auth/login', { name }));
    const session = { token: response.token, name: response.name, admin: response.admin };
    this.store(session);
    this.session.set(session);
    this.farewell.set('');
    this.welcome.set(this.welcomeLines(response));
  }

  async logout(): Promise<void> {
    let farewell = `Goodbye, ${this.session()?.name}!`;
    try {
      farewell = (await firstValueFrom(this.http.post<ActionResponse>('/api/auth/logout', {}))).message;
    } catch {
    }
    await this.expire();
    this.farewell.set(farewell);
  }

  async expire(): Promise<void> {
    this.session.set(null);
    this.welcome.set([]);
    this.store(null);
    await this.router.navigate(['/login']);
  }

  dismissWelcome(): void {
    this.welcome.set([]);
  }

  private welcomeLines(response: LoginResponse): string[] {
    const lines = [`Hello, ${response.name}!`];
    if (response.admin) {
      return [...lines, 'You have access to library management.', ...response.notifications];
    }
    const { borrowed, waitlists } = response.status;
    lines.push(...response.notifications);
    if (borrowed.length) {
      lines.push('Your borrowed books:', ...borrowed.map((title, i) => `${i + 1}. ${title}`));
    } else {
      lines.push("You don't have any books borrowed yet.");
    }
    if (waitlists.length) {
      lines.push('Your wait lists:', ...waitlists.map((w, i) => `${i + 1}. ${w.title} - position ${w.position}`));
    }
    return lines;
  }

  private store(session: Session | null): void {
    try {
      if (session) {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      } else {
        sessionStorage.removeItem(STORAGE_KEY);
      }
    } catch {
    }
  }
}
