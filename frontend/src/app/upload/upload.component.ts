import { Component, inject } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.css']
})
export class UploadComponent {
  private http = inject(HttpClient);
  private fb   = inject(FormBuilder);

  form = this.fb.group({
    title:       ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.required, Validators.maxLength(500)]]
  });

  selectedFile: File | null = null;
  previewUrl:   string | null = null;
  uploadProgress = 0;

  state: 'idle' | 'uploading' | 'success' | 'error' = 'idle';
  errorMessage = '';
  successPhoto: { title: string; url: string } | null = null;

  // ── File selection ────────────────────────────────────────────────────────

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Please select an image file (JPEG, PNG, GIF, WebP…)';
      this.state = 'error';
      return;
    }

    this.selectedFile = file;
    this.state        = 'idle';
    this.errorMessage = '';

    const reader = new FileReader();
    reader.onload = () => (this.previewUrl = reader.result as string);
    reader.readAsDataURL(file);
  }

  // ── Submit ────────────────────────────────────────────────────────────────

  onSubmit(): void {
    if (this.form.invalid || !this.selectedFile) return;

    this.state          = 'uploading';
    this.uploadProgress = 0;

    const formData = new FormData();
    formData.append('file',        this.selectedFile);
    formData.append('title',       this.form.value.title!);
    formData.append('description', this.form.value.description!);

    this.http
      .post<{ title: string; url: string }>('/api/gallery/upload', formData, {
        reportProgress: true,
        observe: 'events'
      })
      .subscribe({
        next: (event) => {
          if (event.type === HttpEventType.UploadProgress && event.total) {
            this.uploadProgress = Math.round((event.loaded / event.total) * 100);
          } else if (event.type === HttpEventType.Response) {
            this.state        = 'success';
            this.successPhoto = event.body!;
            this.resetForm();
          }
        },
        error: (err) => {
          this.state        = 'error';
          this.errorMessage = err.error?.message ?? err.message ?? 'Upload failed. Please try again.';
        }
      });
  }

  uploadAnother(): void {
    this.state        = 'idle';
    this.successPhoto = null;
  }

  private resetForm(): void {
    this.form.reset();
    this.selectedFile   = null;
    this.previewUrl     = null;
    this.uploadProgress = 0;
  }
}
