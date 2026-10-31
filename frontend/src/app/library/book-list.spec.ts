import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LibraryService } from '../core/library.service';
import { BookList } from './book-list';

describe('BookList', () => {
  let fixture: ComponentFixture<BookList>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [BookList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(BookList);
  });

  function render(): HTMLElement {
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows an empty message when there are no books', () => {
    expect(render().textContent).toContain('No books are registered');
  });

  it('offers the right action for each book state', () => {
    TestBed.inject(LibraryService).books.set([
      { title: 'Dune', available: true, borrowedByMe: false, waitlistPosition: null },
      { title: '1984', available: false, borrowedByMe: true, waitlistPosition: null },
      { title: 'Emma', available: false, borrowedByMe: false, waitlistPosition: null },
      { title: 'Ulysses', available: false, borrowedByMe: false, waitlistPosition: 2 },
    ]);

    const rows = Array.from(render().querySelectorAll('tbody tr')).map((row) => row.textContent?.replace(/\s+/g, ' ').trim());

    expect(rows[0]).toContain('Borrow');
    expect(rows[1]).toContain('Return');
    expect(rows[2]).toContain('Join waitlist');
    expect(rows[3]).toContain('Waitlist position 2');
  });

  it('hides borrow and return actions for admin', () => {
    TestBed.inject(LibraryService).books.set([
      { title: 'Dune', available: true, borrowedByMe: false, waitlistPosition: null },
    ]);
    fixture.componentRef.setInput('admin', true);

    expect(render().querySelector('.btn')).toBeNull();
  });
});
