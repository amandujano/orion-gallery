import { Routes } from '@angular/router';
import { GalleryComponent } from './gallery/gallery.component';
import { UploadComponent } from './upload/upload.component';

export const routes: Routes = [
  { path: '', redirectTo: 'gallery', pathMatch: 'full' },
  { path: 'gallery', component: GalleryComponent },
  { path: 'upload', component: UploadComponent },
  { path: '**', redirectTo: 'gallery' }
];
