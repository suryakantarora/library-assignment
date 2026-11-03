import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LibraryService } from './library.service';

describe('LibraryService', () => {
  let library: LibraryService;
  let http: HttpTestingController;

  const books = [{ title: '1984', available: false, borrowedByMe: true, waitlistPosition: null }];
  const status = { borrowed: ['1984'], waitlists: [] };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    library = TestBed.inject(LibraryService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    vi.useRealTimers();
  });

  function flushRefresh(): void {
    http.expectOne('/api/books').flush(books);
    http.expectOne('/api/status').flush(status);
  }

  it('loads books and status on refresh', async () => {
    const pending = library.refresh();
    flushRefresh();
    await pending;

    expect(library.books()).toEqual(books);
    expect(library.status()).toEqual(status);
  });

  async function flushRefreshAfterAction(): Promise<void> {
    await vi.waitFor(() => http.expectOne('/api/books').flush(books));
    http.expectOne('/api/status').flush(status);
  }

  it('shows the server message and refreshes after a successful action', async () => {
    const pending = library.borrow('1984');
    const request = http.expectOne('/api/borrow');
    expect(request.request.body).toEqual({ title: '1984' });
    request.flush({ message: 'You borrowed "1984".' });
    await flushRefreshAfterAction();

    expect(await pending).toBe(true);
    expect(library.feedback()).toEqual({ text: 'You borrowed "1984".', kind: 'success' });
    expect(library.books()).toEqual(books);
  });

  it('shows the server error and reports failure', async () => {
    const pending = library.borrow('1984');
    http.expectOne('/api/borrow').flush(
      { message: 'Sorry, "1984" is currently not available.' },
      { status: 409, statusText: 'Conflict' },
    );
    await flushRefreshAfterAction();

    expect(await pending).toBe(false);
    expect(library.feedback()).toEqual({ text: 'Sorry, "1984" is currently not available.', kind: 'error' });
  });

  it('clears action feedback after five seconds', async () => {
    vi.useFakeTimers();
    const pending = library.borrow('1984');
    http.expectOne('/api/borrow').flush({ message: 'You borrowed "1984".' });
    await Promise.resolve();
    flushRefresh();
    await pending;

    await vi.advanceTimersByTimeAsync(4999);
    expect(library.feedback()).not.toBeNull();

    await vi.advanceTimersByTimeAsync(1);
    expect(library.feedback()).toBeNull();
  });
});
