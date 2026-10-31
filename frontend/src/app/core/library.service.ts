import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { errorMessage } from './errors';
import { ActionResponse, Book, BookDetail, Feedback, Status } from './models';

@Injectable({ providedIn: 'root' })
export class LibraryService {
  private readonly http = inject(HttpClient);

  readonly books = signal<Book[]>([]);
  readonly status = signal<Status>({ borrowed: [], waitlists: [] });
  readonly feedback = signal<Feedback | null>(null);
  readonly detail = signal<BookDetail | null>(null);

  async refresh(): Promise<void> {
    try {
      const [books, status] = await Promise.all([
        firstValueFrom(this.http.get<Book[]>('/api/books')),
        firstValueFrom(this.http.get<Status>('/api/status')),
      ]);
      this.books.set(books);
      this.status.set(status);
      await this.reloadDetail();
    } catch (error) {
      this.feedback.set({ text: errorMessage(error), kind: 'error' });
    }
  }

  add(title: string): Promise<boolean> {
    return this.perform('/api/books', title);
  }

  borrow(title: string): Promise<boolean> {
    return this.perform('/api/borrow', title);
  }

  giveBack(title: string): Promise<boolean> {
    return this.perform('/api/return', title);
  }

  joinWaitlist(title: string): Promise<boolean> {
    return this.perform('/api/waitlist', title);
  }

  async inspect(title: string): Promise<void> {
    try {
      const params = { title };
      this.detail.set(await firstValueFrom(this.http.get<BookDetail>('/api/books/detail', { params })));
    } catch (error) {
      this.feedback.set({ text: errorMessage(error), kind: 'error' });
    }
  }

  closeDetail(): void {
    this.detail.set(null);
  }

  clearFeedback(): void {
    this.feedback.set(null);
  }

  reset(): void {
    this.books.set([]);
    this.status.set({ borrowed: [], waitlists: [] });
    this.feedback.set(null);
    this.detail.set(null);
  }

  private async reloadDetail(): Promise<void> {
    const current = this.detail();
    if (current) {
      await this.inspect(current.title);
    }
  }

  private async perform(url: string, title: string): Promise<boolean> {
    let succeeded = false;
    try {
      const response = await firstValueFrom(this.http.post<ActionResponse>(url, { title }));
      this.feedback.set({ text: response.message, kind: 'success' });
      succeeded = true;
    } catch (error) {
      this.feedback.set({ text: errorMessage(error), kind: 'error' });
    }
    await this.refresh();
    return succeeded;
  }
}
