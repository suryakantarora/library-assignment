import { Component, inject, input } from '@angular/core';
import { LibraryService } from '../core/library.service';

@Component({
  selector: 'app-book-list',
  templateUrl: './book-list.html',
})
export class BookList {
  protected readonly library = inject(LibraryService);

  readonly admin = input(false);
}
