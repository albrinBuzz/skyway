import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AuthStore } from '../../core/auth.store';
import { Perfil } from '../../core/models';
import { errorMessage } from '../../core/ui';

@Component({
  selector: 'app-perfil',
  standalone: false,
  templateUrl: './perfil.page.html',
  styleUrls: ['./perfil.page.scss']
})
export class PerfilPage {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly store = inject(AuthStore);
  private readonly toasts = inject(ToastController);
  readonly auth = inject(AuthService);

  perfil: Perfil | null = null;
  busy = false;
  savingEmail = false;
  savingPassword = false;
  error = '';

  readonly emailForm = this.fb.nonNullable.group({
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    contrasenaActual: ['', [Validators.required, Validators.maxLength(72)]]
  });

  readonly passwordForm = this.fb.nonNullable.group({
    contrasenaActual: ['', [Validators.required, Validators.maxLength(72)]],
    nuevaContrasena: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    confirmarContrasena: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
  });

  ionViewWillEnter() {
    void this.load();
  }

  async load() {
    this.busy = true;
    this.error = '';
    try {
      this.perfil = await firstValueFrom(this.api.me());
      this.store.setPerfil(this.perfil);
      this.emailForm.patchValue({ correoElectronico: this.perfil.usuario.correoElectronico ?? '' });
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }

  async guardarCorreo() {
    if (this.emailForm.invalid || this.savingEmail) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.savingEmail = true;
    this.error = '';
    const { correoElectronico, contrasenaActual } = this.emailForm.getRawValue();
    try {
      this.perfil = await firstValueFrom(this.api.actualizarCorreo(correoElectronico, contrasenaActual));
      this.store.setPerfil(this.perfil);
      this.emailForm.patchValue({ contrasenaActual: '' });
      this.emailForm.markAsPristine();
      const toast = await this.toasts.create({ message: 'Correo actualizado.', duration: 2200, color: 'success' });
      await toast.present();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.savingEmail = false;
    }
  }

  async guardarContrasena() {
    if (this.passwordForm.invalid || this.savingPassword) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { contrasenaActual, nuevaContrasena, confirmarContrasena } = this.passwordForm.getRawValue();
    if (nuevaContrasena !== confirmarContrasena) {
      this.error = 'Las contraseñas nuevas no coinciden.';
      return;
    }
    if (contrasenaActual === nuevaContrasena) {
      this.error = 'La nueva contraseña debe ser distinta de la actual.';
      return;
    }

    this.savingPassword = true;
    this.error = '';
    try {
      await firstValueFrom(this.api.cambiarContrasena(contrasenaActual, nuevaContrasena));
      this.passwordForm.reset();
      const toast = await this.toasts.create({ message: 'Contraseña actualizada.', duration: 2200, color: 'success' });
      await toast.present();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.savingPassword = false;
    }
  }
}
