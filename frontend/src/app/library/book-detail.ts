import { Component, inject } from '@angular/core';
import { LibraryService } from '../core/library.service';

@Component({
  selector: 'app-book-detail-panel',
  templateUrl: './book-detail.html',
})
export class BookDetailPanel {
  protected readonly library = inject(LibraryService);
}
