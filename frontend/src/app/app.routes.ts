import { Routes } from '@angular/router';
import { GalleryComponent } from './gallery/gallery.component';

export const routes: Routes = [
  { path: '', redirectTo: 'gallery', pathMatch: 'full' },
  { path: 'gallery', component: GalleryComponent },
  { path: '**', redirectTo: 'gallery' }
];
