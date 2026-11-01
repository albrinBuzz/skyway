import { Component, OnInit, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { addIcons } from 'ionicons';
import {
  airplane,
  alertCircle,
  arrowBackOutline,
  arrowForwardOutline,
  calendarOutline,
  callOutline,
  cardOutline,
  checkmarkCircle,
  chevronDownOutline,
  closeCircle,
  closeOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  personAddOutline,
  personOutline,
  searchOutline,
  shieldCheckmarkOutline
} from 'ionicons/icons';
import { AuthService } from '../../core/auth.service';
import { AuthStore } from '../../core/auth.store';
import { RegistroRequest } from '../../core/models';
import { PAISES_TELEFONO, PAIS_POR_DEFECTO_ISO2, PaisTelefono } from '../../core/paises-telefono';
import { errorMessage } from '../../core/ui';

addIcons({
  airplane,
  alertCircle,
  arrowBackOutline,
  arrowForwardOutline,
  calendarOutline,
  callOutline,
  cardOutline,
  checkmarkCircle,
  chevronDownOutline,
  closeCircle,
  closeOutline,
  eyeOffOutline,
  eyeOutline,
  globeOutline,
  informationCircleOutline,
  lockClosedOutline,
  mailOutline,
  personAddOutline,
  personOutline,
  searchOutline,
  shieldCheckmarkOutline
});

const NOMBRE_REGEX = /^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s]{2,60}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_SEGURA_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/;

export function formatearRut(rutRaw: string): string {
  if (!rutRaw) return '';
  const clean = rutRaw.replace(/[^0-9kK]/g, '').toUpperCase();
  if (!clean) return '';
  if (clean.length === 1) return clean;
  const cuerpo = clean.slice(0, -1).slice(0, 8);
  const dv = clean.slice(-1);
  const cuerpoFormateado = cuerpo.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${cuerpoFormateado}-${dv}`;
}

export function validarRutModulo11(rutCompleto: string): boolean {
  if (!rutCompleto) return false;
  const clean = rutCompleto.replace(/[^0-9kK]/g, '').toUpperCase();
  if (clean.length < 8 || clean.length > 9) return false;
  const cuerpo = clean.slice(0, -1);
  const dv = clean.slice(-1);
  if (!/^\d+$/.test(cuerpo)) return false;

  let suma = 0;
  let multiplo = 2;
  for (let i = cuerpo.length - 1; i >= 0; i--) {
    suma += multiplo * parseInt(cuerpo.charAt(i), 10);
    multiplo = multiplo < 7 ? multiplo + 1 : 2;
  }
  const dvEsperadoNum = 11 - (suma % 11);
  let dvEsperado = '';
  if (dvEsperadoNum === 11) dvEsperado = '0';
  else if (dvEsperadoNum === 10) dvEsperado = 'K';
  else dvEsperado = String(dvEsperadoNum);

  return dv === dvEsperado;
}

function nombreValidoValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val) return null;
  const trimmed = String(val).trim();
  if (trimmed.length < 2) return { nombreMuyCorto: true };
  if (!NOMBRE_REGEX.test(trimmed)) return { nombreInvalido: true };
  return null;
}

function correoValidoValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val) return null;
  return EMAIL_REGEX.test(String(val).trim()) ? null : { correoInvalido: true };
}

function fechaNacimientoValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val) return null;
  const partes = String(val).split('-');
  if (partes.length !== 3) return { fechaInvalida: true };
  const anio = parseInt(partes[0], 10);
  const mes = parseInt(partes[1], 10) - 1;
  const dia = parseInt(partes[2], 10);
  const fecha = new Date(anio, mes, dia);
  if (isNaN(fecha.getTime())) return { fechaInvalida: true };
  if (anio < 1920) return { anioMuyAntiguo: true };

  const hoy = new Date();
  let edad = hoy.getFullYear() - anio;
  const diffMes = hoy.getMonth() - mes;
  if (diffMes < 0 || (diffMes === 0 && hoy.getDate() < dia)) {
    edad--;
  }
  return edad >= 18 ? null : { menorDeEdad: true };
}

function contrasenaSeguraValidator(control: AbstractControl): ValidationErrors | null {
  const val = control.value;
  if (!val) return null;
  return PASSWORD_SEGURA_REGEX.test(String(val)) ? null : { contrasenaInsegura: true };
}

function contrasenasCoincidenValidator(group: AbstractControl): ValidationErrors | null {
  const pass = group.get('contrasena')?.value;
  const conf = group.get('confirmarContrasena')?.value;
  if (!pass || !conf) return null;
  return pass === conf ? null : { passwordsMismatch: true };
}

@Component({
  selector: 'app-registro',
  standalone: false,
  templateUrl: './registro.page.html',
  styleUrls: ['./registro.page.scss']
})
export class RegistroPage implements OnInit {
  private fb = inject(FormBuilder).nonNullable;
  private auth = inject(AuthService);
  private store = inject(AuthStore);
  private router = inject(Router);

  busy = false;
  error = '';
  registroExitoso = false;
  mostrarContrasena = false;
  mostrarConfirmar = false;

  readonly paises = PAISES_TELEFONO;
  readonly fechaMaxima = this.calcularFechaMaximaMayorEdad();
  readonly fechaMinima = '1920-01-01';

  modalPaisAbierto = false;
  filtroPais = '';
  paisesFiltrados: PaisTelefono[] = PAISES_TELEFONO;

  form = this.fb.group(
    {
      tipoDocumento: ['RUT', [Validators.required]],
      rut: ['', [Validators.required, (c: AbstractControl) => this.validarDocumento(c)]],
      nombre: ['', [Validators.required, nombreValidoValidator]],
      apellido: ['', [Validators.required, nombreValidoValidator]],
      correoElectronico: ['', [Validators.required, correoValidoValidator, Validators.maxLength(100)]],
      paisTelefono: [PAIS_POR_DEFECTO_ISO2, [Validators.required]],
      telefonoLocal: ['', [Validators.required, (c: AbstractControl) => this.validarTelefonoLocal(c)]],
      fechaNacimiento: ['', [Validators.required, fechaNacimientoValidator]],
      nacionalidad: ['Chilena', [Validators.required, Validators.maxLength(50)]],
      contrasena: ['', [Validators.required, contrasenaSeguraValidator]],
      confirmarContrasena: ['', [Validators.required]]
    },
    { validators: [contrasenasCoincidenValidator] }
  );

  ngOnInit(): void {
    this.form.controls.tipoDocumento.valueChanges.subscribe(() => {
      this.form.controls.rut.updateValueAndValidity();
    });
    this.form.controls.paisTelefono.valueChanges.subscribe(() => {
      this.form.controls.telefonoLocal.updateValueAndValidity();
    });
  }

  get paisSeleccionado(): PaisTelefono {
    const cod = this.form.controls.paisTelefono.value;
    return this.paises.find((p) => p.iso2 === cod) || this.paises.find((p) => p.iso2 === 'CL') || this.paises[0];
  }

  abrirModalPais(): void {
    this.filtroPais = '';
    this.paisesFiltrados = this.paises;
    this.modalPaisAbierto = true;
  }

  cerrarModalPais(): void {
    this.modalPaisAbierto = false;
  }

  filtrarPaises(event: any): void {
    const raw = (event?.target?.value || '').trim();
    this.filtroPais = raw;
    if (!raw) {
      this.paisesFiltrados = this.paises;
      return;
    }
    const cleanTerm = raw.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    const cleanDigits = raw.replace(/\D/g, '');

    this.paisesFiltrados = this.paises.filter((p) => {
      const nombreNorm = p.nombre.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
      const iso2Norm = p.iso2.toLowerCase();
      const prefijoDigits = p.prefijo.replace(/\D/g, '');

      return (
        nombreNorm.includes(cleanTerm) ||
        iso2Norm.includes(cleanTerm) ||
        (cleanDigits.length > 0 && prefijoDigits.includes(cleanDigits)) ||
        p.prefijo.includes(raw)
      );
    });
  }

  seleccionarPais(pais: PaisTelefono): void {
    this.form.controls.paisTelefono.setValue(pais.iso2);
    this.modalPaisAbierto = false;
    this.form.controls.telefonoLocal.updateValueAndValidity();
  }

  trackByIso2(_index: number, item: PaisTelefono): string {
    return item.iso2;
  }

  validarDocumento(control: AbstractControl): ValidationErrors | null {
    const val = control.value;
    if (!val) return null;
    const clean = String(val).trim();
    const tipo = this.form ? this.form.controls.tipoDocumento.value : 'RUT';
    if (tipo === 'RUT') {
      return validarRutModulo11(clean) ? null : { rutInvalido: true };
    }
    return /^[a-zA-Z0-9-]{6,20}$/.test(clean) ? null : { docInvalido: true };
  }

  validarTelefonoLocal(control: AbstractControl): ValidationErrors | null {
    const val = control.value;
    if (!val) return null;
    const digitos = String(val).replace(/\D/g, '');
    const iso2 = this.form ? this.form.controls.paisTelefono.value : 'CL';

    if (iso2 === 'CL') {
      if (digitos.length !== 9) return { telefonoChileLargo: true };
      if (!digitos.startsWith('9')) return { telefonoChileFormato: true };
    } else {
      const p = this.paises.find((x) => x.iso2 === iso2);
      const min = p ? p.minDigitos : 4;
      const max = p ? p.maxDigitos : 15;
      if (digitos.length < min || digitos.length > max) {
        return { telefonoGeneralLargo: { min, max } };
      }
    }
    return null;
  }

  onDocumentoInput(event: any): void {
    const val = event?.target?.value || '';
    if (this.form.controls.tipoDocumento.value === 'RUT') {
      const formatted = formatearRut(val);
      if (formatted !== val) {
        this.form.controls.rut.setValue(formatted, { emitEvent: false });
      }
    }
  }

  onTelefonoInput(event: any): void {
    const val = event?.target?.value || '';
    // Filtrar solo dígitos y espacios permitidos
    const sanitized = val.replace(/[^\d\s]/g, '');
    if (sanitized !== val) {
      this.form.controls.telefonoLocal.setValue(sanitized, { emitEvent: false });
    }
  }

  private calcularFechaMaximaMayorEdad(): string {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  }

  ionViewWillEnter(): void {
    if (this.store.authenticated) {
      void this.router.navigateByUrl('/inicio', { replaceUrl: true });
    }
  }

  toggleMostrarContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  toggleMostrarConfirmar(): void {
    this.mostrarConfirmar = !this.mostrarConfirmar;
  }

  async registrar(): Promise<void> {
    if (this.form.invalid || this.busy) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.busy = true;
    this.error = '';

    const pais = this.paisSeleccionado;
    const digitosTel = raw.telefonoLocal.replace(/\D/g, '');
    const telefonoCompleto = `${pais.prefijo}${digitosTel}`;

    const req: RegistroRequest = {
      rut: raw.rut.trim(),
      nombre: raw.nombre.trim(),
      apellido: raw.apellido.trim(),
      correoElectronico: raw.correoElectronico.trim().toLowerCase(),
      telefono: telefonoCompleto,
      fechaNacimiento: raw.fechaNacimiento,
      tipoDocumento: raw.tipoDocumento || 'RUT',
      documentoIdentidad: raw.rut.trim(),
      nacionalidad: raw.nacionalidad.trim() || 'Chilena',
      contrasena: raw.contrasena
    };

    try {
      await firstValueFrom(this.auth.registro(req));
      this.registroExitoso = true;
      setTimeout(() => {
        void this.router.navigateByUrl('/inicio', { replaceUrl: true });
      }, 1200);
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }
}
