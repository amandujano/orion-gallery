import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface CatPhoto {
  id: number;
  title: string;
  url: string;
  description: string;
}

@Component({
  selector: 'app-gallery',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gallery.component.html',
  styleUrls: ['./gallery.component.css']
})
export class GalleryComponent implements OnInit {
  private http = inject(HttpClient);

  photos: CatPhoto[] = [];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    this.http.get<CatPhoto[]>('/api/gallery/photos').subscribe({
      next: (data) => {
        this.photos = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Could not load photos. Is the backend running?';
        this.loading = false;
        console.error(err);
      }
    });
  }
}
