import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LibraryService } from '../core/library.service';

@Component({
  selector: 'app-add-book-form',
  imports: [FormsModule],
  template: `
    <form class="card add-form" (ngSubmit)="submit()">
      <div class="section-heading">
        <div>
          <p class="section-eyebrow">GROW THE COLLECTION</p>
          <h2>Add a book</h2>
        </div>
        <span class="section-mark add-mark" aria-hidden="true">+</span>
      </div>
      <div class="row add-row">
        <input name="title" [(ngModel)]="title" maxlength="150" placeholder="Book title" autocomplete="off" />
        <button class="btn primary" type="submit" [disabled]="!title.trim()">Add book</button>
      </div>
    </form>
  `,
})
export class AddBookForm {
  private readonly library = inject(LibraryService);

  protected title = '';

  protected async submit(): Promise<void> {
    if (await this.library.add(this.title.trim())) {
      this.title = '';
    }
  }
}
