import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../core/auth.service';
import { LibraryService } from '../core/library.service';
import { AddBookForm } from './add-book-form';
import { BookDetailPanel } from './book-detail';
import { BookList } from './book-list';
import { StatusPanel } from './status-panel';

@Component({
  selector: 'app-library-page',
  imports: [AddBookForm, BookDetailPanel, BookList, StatusPanel],
  templateUrl: './library-page.html',
})
export class LibraryPage implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly library = inject(LibraryService);

  ngOnInit(): void {
    this.library.clearFeedback();
    this.library.refresh();
  }

  protected async logout(): Promise<void> {
    await this.auth.logout();
    this.library.reset();
  }
}
