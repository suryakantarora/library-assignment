import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LibraryService } from '../core/library.service';
import { BookDetailPanel } from './book-detail';

describe('BookDetailPanel', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookDetailPanel],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  function render(): string {
    const fixture = TestBed.createComponent(BookDetailPanel);
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent?.replace(/\s+/g, ' ') ?? '';
  }

  it('renders nothing until a book is selected', () => {
    expect(render().trim()).toBe('');
  });

  it('shows waitlist position and hides borrower when unknown', () => {
    TestBed.inject(LibraryService).detail.set({
      title: '1984',
      available: false,
      borrowedByMe: false,
      borrowedBy: null,
      waitlistSize: 2,
      waitlistPosition: 2,
    });

    const text = render();

    expect(text).toContain('1984');
    expect(text).toContain('Currently borrowed');
    expect(text).toContain('People waiting2');
    expect(text).toContain('Your position2');
    expect(text).not.toContain('Borrowed by');
  });

  it('shows the borrower to admin', () => {
    TestBed.inject(LibraryService).detail.set({
      title: '1984',
      available: false,
      borrowedByMe: false,
      borrowedBy: 'Alice',
      waitlistSize: 0,
      waitlistPosition: null,
    });

    expect(render()).toContain('Borrowed byAlice');
  });
});
