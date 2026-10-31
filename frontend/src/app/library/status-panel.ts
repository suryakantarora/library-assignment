import { Component, inject } from '@angular/core';
import { LibraryService } from '../core/library.service';

@Component({
  selector: 'app-status-panel',
  templateUrl: './status-panel.html',
})
export class StatusPanel {
  protected readonly library = inject(LibraryService);
}
