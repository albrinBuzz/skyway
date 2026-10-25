import { Component, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ToastController } from '@ionic/angular';
import { firstValueFrom } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { AuthStore } from '../../core/auth.store';
import { Perfil } from '../../core/models';
import { errorMessage } from '../../core/ui';
import { addIcons } from 'ionicons';
import {
  alertCircle,
  alertCircleOutline,
  calendarOutline,
  cameraOutline,
  cardOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkOutline,
  chevronDownOutline,
  chevronUpOutline,
  closeCircleOutline,
  cloudUploadOutline,
  eyeOffOutline,
  eyeOutline,
  fingerPrintOutline,
  globeOutline,
  happyOutline,
  imageOutline,
  informationCircleOutline,
  keyOutline,
  lockClosedOutline,
  logOutOutline,
  mailOutline,
  personCircleOutline,
  personOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  trashOutline
} from 'ionicons/icons';

// Registrar íconos utilizados en la pantalla de Perfil
addIcons({
  alertCircle,
  alertCircleOutline,
  calendarOutline,
  cameraOutline,
  cardOutline,
  checkmarkCircle,
  checkmarkCircleOutline,
  checkmarkOutline,
  chevronDownOutline,
  chevronUpOutline,
  closeCircleOutline,
  cloudUploadOutline,
  eyeOffOutline,
  eyeOutline,
  fingerPrintOutline,
  globeOutline,
  happyOutline,
  imageOutline,
  informationCircleOutline,
  keyOutline,
  lockClosedOutline,
  logOutOutline,
  mailOutline,
  personCircleOutline,
  personOutline,
  refreshOutline,
  shieldCheckmarkOutline,
  sparklesOutline,
  trashOutline
});

export interface CharacterAvatar {
  id: string;
  name: string;
  badge: string;
  description: string;
  category: 'perros' | 'gatos' | 'personas';
  bgGradient: string;
  svg: string;
  safeSvg?: SafeHtml;
}

// Allowlist explícita de tipos MIME permitidos para avatar (rechaza SVG y archivos arbitrarios)
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'] as const;
const MAX_UPLOAD_BYTES = 5 * 1024 * 1024; // 5 MB límite para prevenir DoS / memoria

function isSafeImageDataUrl(url: unknown): url is string {
  if (typeof url !== 'string') return false;
  // Debe ser estrictamente un data URL en base64 de imagen rasterizada (jpeg, png, webp)
  // Previene que localStorage alterado inyecte javascript:, data:text/html, etc.
  return /^data:image\/(?:jpeg|png|webp);base64,[A-Za-z0-9+/=]+$/.test(url.trim());
}

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
  private readonly sanitizer = inject(DomSanitizer);
  readonly auth = inject(AuthService);

  perfil: Perfil | null = null;
  busy = false;
  savingEmail = false;
  savingPassword = false;
  error = '';

  // Mensajes de éxito y error locales para cada formulario
  emailSuccess = '';
  emailError = '';
  passwordSuccess = '';
  passwordError = '';

  // Visibilidad de contraseñas para facilitar el ingreso a adultos mayores
  verEmailClave = false;
  verPassActual = false;
  verPassNueva = false;
  verPassConfirmar = false;

  // Estado del selector de avatar y personalización de foto
  selectedAvatarType: 'character' | 'custom' | 'initials' = 'character';
  selectedCharacterId = 'perro-piloto';
  customPhotoUrl: string | null = null;
  avatarCategory: 'todos' | 'perros' | 'gatos' | 'personas' | 'mifoto' = 'todos';
  showAvatarSelector = false;
  avatarSuccess = '';

  // 12 personajes ilustrados y animados con temática de viaje SkyWay
  readonly characterAvatars: CharacterAvatar[] = [
    // --- 1. Perros ---
    {
      id: 'perro-piloto',
      name: 'Toby el Piloto',
      badge: 'Capitán Canino',
      description: 'Golden retriever sonriente con gafas aviador y gorra de capitán',
      category: 'perros',
      bgGradient: 'linear-gradient(135deg, #38bdf8 0%, #0284c7 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-toby" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#38bdf8"/>
      <stop offset="100%" stop-color="#0284c7"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-toby)"/>
  <path d="M14 74 Q22 66 30 74 Q36 68 44 74 Q52 66 60 74" fill="none" stroke="#ffffff" stroke-width="3" opacity="0.4" stroke-linecap="round"/>
  <g class="anim-sway">
    <path d="M24 38 C18 50 20 66 28 68 C34 68 36 54 32 40 Z" fill="#d97706"/>
    <path d="M76 38 C82 50 80 66 72 68 C66 68 64 54 68 40 Z" fill="#d97706"/>
  </g>
  <ellipse cx="50" cy="56" rx="27" ry="24" fill="#f59e0b"/>
  <ellipse cx="50" cy="65" rx="15" ry="11" fill="#fef3c7"/>
  <path d="M46 59 Q50 56 54 59 Q50 64 46 59 Z" fill="#0f172a"/>
  <path d="M47 67 Q50 70 53 67" stroke="#b45309" stroke-width="1.8" fill="none" stroke-linecap="round"/>
  <path d="M48 68 Q50 76 52 68 Z" fill="#f43f5e" class="anim-bob"/>
  <circle cx="36" cy="62" r="3.5" fill="#f97316" opacity="0.35"/>
  <circle cx="64" cy="62" r="3.5" fill="#f97316" opacity="0.35"/>
  <g class="anim-gleam">
    <rect x="29" y="44" width="18" height="13" rx="5" fill="#0f172a"/>
    <rect x="53" y="44" width="18" height="13" rx="5" fill="#0f172a"/>
    <line x1="47" y1="48" x2="53" y2="48" stroke="#facc15" stroke-width="2.5"/>
    <line x1="32" y1="46" x2="43" y2="54" stroke="#ffffff" stroke-width="1.6" opacity="0.75" stroke-linecap="round"/>
    <line x1="56" y1="46" x2="67" y2="54" stroke="#ffffff" stroke-width="1.6" opacity="0.75" stroke-linecap="round"/>
  </g>
  <g class="anim-bob">
    <path d="M28 35 C28 18 72 18 72 35 Z" fill="#0f172a"/>
    <path d="M24 35 Q50 39 76 35 Q74 41 26 41 Z" fill="#1e293b"/>
    <line x1="28" y1="35" x2="72" y2="35" stroke="#facc15" stroke-width="2.5"/>
    <circle cx="50" cy="27" r="4" fill="#facc15"/>
    <path d="M42 27 Q50 23 58 27" stroke="#facc15" stroke-width="2" fill="none"/>
  </g>
</svg>`
    },
    {
      id: 'perro-husky',
      name: 'Max el Husky',
      badge: 'Viajero Polar',
      description: 'Siberian Husky de ojos azules brillantes con bufanda de viaje',
      category: 'perros',
      bgGradient: 'linear-gradient(135deg, #2dd4bf 0%, #0f766e 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-husky" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#2dd4bf"/>
      <stop offset="100%" stop-color="#0f766e"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-husky)"/>
  <circle cx="22" cy="25" r="1.5" fill="#ffffff" opacity="0.7" class="anim-pulse"/>
  <circle cx="80" cy="30" r="2" fill="#ffffff" opacity="0.8" class="anim-pulse"/>
  <circle cx="76" cy="72" r="1.5" fill="#ffffff" opacity="0.6"/>
  <polygon points="26,42 34,16 46,34" fill="#475569"/>
  <polygon points="30,38 35,22 42,34" fill="#fda4af"/>
  <polygon points="74,42 66,16 54,34" fill="#475569"/>
  <polygon points="70,38 65,22 58,34" fill="#fda4af"/>
  <ellipse cx="50" cy="55" rx="27" ry="24" fill="#475569"/>
  <path d="M33 46 C33 70 67 70 67 46 C61 53 56 46 50 50 C44 46 39 53 33 46 Z" fill="#ffffff"/>
  <polygon points="50,34 46,47 54,47" fill="#ffffff"/>
  <g class="anim-blink">
    <ellipse cx="40" cy="47" rx="4.5" ry="5.5" fill="#38bdf8"/>
    <ellipse cx="40" cy="47" rx="2.5" ry="3.5" fill="#0369a1"/>
    <circle cx="39" cy="45" r="1.5" fill="#ffffff"/>
    <ellipse cx="60" cy="47" rx="4.5" ry="5.5" fill="#38bdf8"/>
    <ellipse cx="60" cy="47" rx="2.5" ry="3.5" fill="#0369a1"/>
    <circle cx="59" cy="45" r="1.5" fill="#ffffff"/>
  </g>
  <path d="M46 58 Q50 55 54 58 Q50 63 46 58 Z" fill="#0f172a"/>
  <path d="M46 63 Q50 66 54 63" stroke="#475569" stroke-width="1.8" fill="none" stroke-linecap="round"/>
  <g class="anim-sway">
    <path d="M28 72 C38 84 62 84 72 72 C76 82 66 88 50 88 C34 88 24 82 28 72 Z" fill="#f97316"/>
    <path d="M36 75 L40 85 M46 77 L50 87 M56 77 L60 87 M64 74 L68 83" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round"/>
  </g>
</svg>`
    },
    {
      id: 'perro-pug',
      name: 'Rocco el Pug',
      badge: 'Turista Relax',
      description: 'Pug tierno con audífonos listo para disfrutar sus vacaciones',
      category: 'perros',
      bgGradient: 'linear-gradient(135deg, #f59e0b 0%, #b45309 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-pug" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#f59e0b"/>
      <stop offset="100%" stop-color="#b45309"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-pug)"/>
  <g class="anim-sway">
    <path d="M22 36 C18 48 30 54 34 46 Z" fill="#78350f"/>
    <path d="M78 36 C82 48 70 54 66 46 Z" fill="#78350f"/>
  </g>
  <ellipse cx="50" cy="54" rx="28" ry="24" fill="#fef3c7"/>
  <path d="M40 33 Q50 37 60 33" stroke="#b45309" stroke-width="2" fill="none" stroke-linecap="round"/>
  <path d="M43 38 Q50 42 57 38" stroke="#b45309" stroke-width="2" fill="none" stroke-linecap="round"/>
  <g class="anim-blink">
    <circle cx="36" cy="46" r="6.5" fill="#1c1917"/>
    <circle cx="35" cy="44" r="2.2" fill="#ffffff"/>
    <circle cx="64" cy="46" r="6.5" fill="#1c1917"/>
    <circle cx="63" cy="44" r="2.2" fill="#ffffff"/>
  </g>
  <ellipse cx="50" cy="62" rx="16" ry="12" fill="#451a03"/>
  <path d="M46 56 Q50 54 54 56 Q50 60 46 56 Z" fill="#0f172a"/>
  <path d="M52 66 Q57 73 54 76 Q49 75 51 66 Z" fill="#fb7185" class="anim-bob"/>
  <g class="anim-bob">
    <path d="M22 50 C18 16 82 16 78 50" stroke="#0284c7" stroke-width="4.5" fill="none"/>
    <ellipse cx="21" cy="50" rx="5.5" ry="9" fill="#38bdf8"/>
    <ellipse cx="79" cy="50" rx="5.5" ry="9" fill="#38bdf8"/>
  </g>
</svg>`
    },

    // --- 2. Gatos ---
    {
      id: 'gato-capitan',
      name: 'Michi Capitán',
      badge: 'Comandante Felino',
      description: 'Gato atigrado con gorra de piloto y alas doradas',
      category: 'gatos',
      bgGradient: 'linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-michicap" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#1e3a8a"/>
      <stop offset="100%" stop-color="#0f172a"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-michicap)"/>
  <path d="M16 66 Q50 40 84 66" stroke="#38bdf8" stroke-width="1.5" stroke-dasharray="3,3" fill="none" opacity="0.6"/>
  <polygon points="26,42 32,18 44,34" fill="#f97316"/>
  <polygon points="29,38 33,24 40,34" fill="#fed7aa"/>
  <polygon points="74,42 68,18 56,34" fill="#f97316"/>
  <polygon points="71,38 67,24 60,34" fill="#fed7aa"/>
  <ellipse cx="50" cy="55" rx="27" ry="24" fill="#f97316"/>
  <path d="M46 36 L50 42 L54 36" stroke="#c2410c" stroke-width="2.2" stroke-linecap="round" fill="none"/>
  <path d="M38 38 L42 43" stroke="#c2410c" stroke-width="2" stroke-linecap="round"/>
  <path d="M62 38 L58 43" stroke="#c2410c" stroke-width="2" stroke-linecap="round"/>
  <ellipse cx="42" cy="65" rx="8" ry="6" fill="#fed7aa"/>
  <ellipse cx="58" cy="65" rx="8" ry="6" fill="#fed7aa"/>
  <polygon points="47,59 53,59 50,63" fill="#fb7185"/>
  <path d="M46 64 Q50 67 54 64" stroke="#c2410c" stroke-width="1.8" fill="none" stroke-linecap="round"/>
  <g stroke="#ffffff" stroke-width="1.5" opacity="0.85" stroke-linecap="round">
    <line x1="20" y1="63" x2="36" y2="62"/>
    <line x1="21" y1="68" x2="36" y2="66"/>
    <line x1="80" y1="63" x2="64" y2="62"/>
    <line x1="79" y1="68" x2="64" y2="66"/>
  </g>
  <g class="anim-blink">
    <ellipse cx="38" cy="48" rx="5" ry="6" fill="#10b981"/>
    <ellipse cx="38" cy="48" rx="2" ry="5.5" fill="#064e3b"/>
    <circle cx="36" cy="46" r="1.5" fill="#ffffff"/>
    <ellipse cx="62" cy="48" rx="5" ry="6" fill="#10b981"/>
    <ellipse cx="62" cy="48" rx="2" ry="5.5" fill="#064e3b"/>
    <circle cx="60" cy="46" r="1.5" fill="#ffffff"/>
  </g>
  <g class="anim-bob">
    <path d="M28 32 C28 17 72 17 72 32 Z" fill="#0f172a"/>
    <path d="M24 32 Q50 36 76 32 Q74 38 26 38 Z" fill="#1e293b"/>
    <line x1="28" y1="32" x2="72" y2="32" stroke="#facc15" stroke-width="2.5"/>
    <circle cx="50" cy="24" r="3.5" fill="#facc15"/>
    <path d="M43 24 Q50 20 57 24" stroke="#facc15" stroke-width="2" fill="none"/>
  </g>
</svg>`
    },
    {
      id: 'gato-explorador',
      name: 'Luna Viajera',
      badge: 'Exploradora VIP',
      description: 'Gatita siamesa con boina de viaje y lentes dorados',
      category: 'gatos',
      bgGradient: 'linear-gradient(135deg, #c084fc 0%, #6b21a8 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-luna" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#c084fc"/>
      <stop offset="100%" stop-color="#6b21a8"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-luna)"/>
  <polygon points="26,40 32,16 44,32" fill="#581c87"/>
  <polygon points="74,40 68,16 56,32" fill="#581c87"/>
  <ellipse cx="50" cy="55" rx="27" ry="24" fill="#fdf4ff"/>
  <g class="anim-sway">
    <path d="M30 26 C36 12 76 16 74 30 C64 34 36 34 30 26 Z" fill="#db2777"/>
    <circle cx="54" cy="18" r="2.5" fill="#be185d"/>
  </g>
  <g class="anim-gleam">
    <circle cx="38" cy="49" r="8.5" fill="none" stroke="#facc15" stroke-width="2.2"/>
    <circle cx="62" cy="49" r="8.5" fill="none" stroke="#facc15" stroke-width="2.2"/>
    <line x1="46.5" y1="49" x2="53.5" y2="49" stroke="#facc15" stroke-width="2.2"/>
    <line x1="33" y1="45" x2="40" y2="52" stroke="#ffffff" stroke-width="1.4" opacity="0.75" stroke-linecap="round"/>
    <line x1="57" y1="45" x2="64" y2="52" stroke="#ffffff" stroke-width="1.4" opacity="0.75" stroke-linecap="round"/>
  </g>
  <g class="anim-blink">
    <ellipse cx="38" cy="49" rx="4" ry="5" fill="#7c3aed"/>
    <circle cx="37" cy="47" r="1.5" fill="#ffffff"/>
    <ellipse cx="62" cy="49" rx="4" ry="5" fill="#7c3aed"/>
    <circle cx="61" cy="47" r="1.5" fill="#ffffff"/>
  </g>
  <polygon points="48,60 52,60 50,63" fill="#f472b6"/>
  <path d="M47 64 Q50 67 53 64" stroke="#a21caf" stroke-width="1.6" fill="none" stroke-linecap="round"/>
  <g stroke="#ffffff" stroke-width="1.3" opacity="0.85" stroke-linecap="round">
    <line x1="22" y1="62" x2="35" y2="61"/>
    <line x1="23" y1="67" x2="35" y2="65"/>
    <line x1="78" y1="62" x2="65" y2="61"/>
    <line x1="77" y1="67" x2="65" y2="65"/>
  </g>
</svg>`
    },
    {
      id: 'gato-negro',
      name: 'Salem VIP',
      badge: 'Pasajero Nocturno',
      description: 'Gato negro elegante con corbata michi turquesa y cielo estrellado',
      category: 'gatos',
      bgGradient: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-salem" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#0f172a"/>
      <stop offset="100%" stop-color="#1e1b4b"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-salem)"/>
  <polygon points="22,22 24,26 28,27 24,28 22,32 20,28 16,27 20,26" fill="#fef08a" class="anim-pulse"/>
  <polygon points="80,24 81.5,27 85,28 81.5,29 80,32 78.5,29 75,28 78.5,27" fill="#fef08a" class="anim-pulse"/>
  <circle cx="78" cy="74" r="1.5" fill="#ffffff" opacity="0.8"/>
  <circle cx="20" cy="68" r="1.2" fill="#ffffff" opacity="0.7"/>
  <polygon points="26,42 32,16 44,32" fill="#1e293b"/>
  <polygon points="29,38 33,24 40,32" fill="#334155"/>
  <polygon points="74,42 68,16 56,32" fill="#1e293b"/>
  <polygon points="71,38 67,24 60,32" fill="#334155"/>
  <ellipse cx="50" cy="55" rx="27" ry="24" fill="#1e293b"/>
  <g class="anim-blink">
    <ellipse cx="38" cy="48" rx="6" ry="7" fill="#fbbf24"/>
    <ellipse cx="38" cy="48" rx="2.2" ry="6.2" fill="#090d16"/>
    <circle cx="36" cy="45" r="1.8" fill="#ffffff"/>
    <ellipse cx="62" cy="48" rx="6" ry="7" fill="#fbbf24"/>
    <ellipse cx="62" cy="48" rx="2.2" ry="6.2" fill="#090d16"/>
    <circle cx="60" cy="45" r="1.8" fill="#ffffff"/>
  </g>
  <polygon points="48,59 52,59 50,62" fill="#475569"/>
  <path d="M47 63 Q50 66 53 63" stroke="#64748b" stroke-width="1.6" fill="none" stroke-linecap="round"/>
  <g stroke="#ffffff" stroke-width="1.4" opacity="0.9" stroke-linecap="round">
    <line x1="20" y1="61" x2="36" y2="60"/>
    <line x1="21" y1="66" x2="36" y2="64"/>
    <line x1="80" y1="61" x2="64" y2="60"/>
    <line x1="79" y1="66" x2="64" y2="64"/>
  </g>
  <g class="anim-bob">
    <polygon points="36,76 48,80 36,84" fill="#06b6d4"/>
    <polygon points="64,76 52,80 64,84" fill="#06b6d4"/>
    <circle cx="50" cy="80" r="3.5" fill="#facc15"/>
  </g>
</svg>`
    },

    // --- 3. Personas / Viajeros ---
    {
      id: 'persona-capitana',
      name: 'Capitana Sofía',
      badge: 'Piloto al Mando',
      description: 'Piloto comercial con uniforme, diadema de vuelo y sonrisa',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-sofia" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#2563eb"/>
      <stop offset="100%" stop-color="#1d4ed8"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-sofia)"/>
  <path d="M26 86 Q50 78 74 86 L74 98 L26 98 Z" fill="#ffffff"/>
  <polygon points="50,82 46,92 54,92" fill="#0f172a"/>
  <rect x="25" y="84" width="13" height="6" rx="1.5" fill="#0f172a"/>
  <line x1="27" y1="86" x2="36" y2="86" stroke="#facc15" stroke-width="1.2"/>
  <line x1="27" y1="88" x2="36" y2="88" stroke="#facc15" stroke-width="1.2"/>
  <rect x="62" y="84" width="13" height="6" rx="1.5" fill="#0f172a"/>
  <line x1="64" y1="86" x2="73" y2="86" stroke="#facc15" stroke-width="1.2"/>
  <line x1="64" y1="88" x2="73" y2="88" stroke="#facc15" stroke-width="1.2"/>
  <circle cx="50" cy="30" r="22" fill="#451a03"/>
  <rect x="44" y="68" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="54" rx="20" ry="21" fill="#fed7aa"/>
  <g class="anim-blink">
    <ellipse cx="42" cy="51" rx="2.5" ry="3.2" fill="#1c1917"/>
    <circle cx="41.2" cy="50" r="1" fill="#ffffff"/>
    <ellipse cx="58" cy="51" rx="2.5" ry="3.2" fill="#1c1917"/>
    <circle cx="57.2" cy="50" r="1" fill="#ffffff"/>
  </g>
  <circle cx="36" cy="57" r="3" fill="#f43f5e" opacity="0.3"/>
  <circle cx="64" cy="57" r="3" fill="#f43f5e" opacity="0.3"/>
  <path d="M44 60 Q50 66 56 60" stroke="#be123c" stroke-width="2" fill="none" stroke-linecap="round"/>
  <path d="M28 48 C24 26 76 26 72 48" stroke="#334155" stroke-width="3" fill="none"/>
  <circle cx="28" cy="48" r="4.5" fill="#0f172a"/>
  <circle cx="72" cy="48" r="4.5" fill="#0f172a"/>
  <path d="M28 50 Q32 62 42 63" stroke="#334155" stroke-width="2" fill="none"/>
  <circle cx="42" cy="63" r="2.2" fill="#0f172a"/>
  <g class="anim-bob">
    <path d="M30 35 C30 20 70 20 70 35 Z" fill="#0f172a"/>
    <path d="M26 35 Q50 39 74 35 Q72 40 28 40 Z" fill="#1e293b"/>
    <line x1="30" y1="35" x2="70" y2="35" stroke="#facc15" stroke-width="2"/>
    <circle cx="50" cy="28" r="3.5" fill="#facc15"/>
    <path d="M43 28 Q50 25 57 28" stroke="#facc15" stroke-width="1.8" fill="none"/>
  </g>
</svg>`
    },
    {
      id: 'persona-comandante',
      name: 'Comandante Mateo',
      badge: 'Primer Oficial',
      description: 'Piloto con gafas aviador, uniforme de vuelo y gorra de capitán',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-mateo" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#1e293b"/>
      <stop offset="100%" stop-color="#0f172a"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-mateo)"/>
  <path d="M24 86 Q50 78 76 86 L76 98 L24 98 Z" fill="#ffffff"/>
  <polygon points="50,82 46,94 54,94" fill="#0f172a"/>
  <rect x="23" y="84" width="14" height="6" rx="1.5" fill="#0f172a"/>
  <line x1="25" y1="85.5" x2="35" y2="85.5" stroke="#facc15" stroke-width="1"/>
  <line x1="25" y1="87.5" x2="35" y2="87.5" stroke="#facc15" stroke-width="1"/>
  <rect x="63" y="84" width="14" height="6" rx="1.5" fill="#0f172a"/>
  <line x1="65" y1="85.5" x2="75" y2="85.5" stroke="#facc15" stroke-width="1"/>
  <line x1="65" y1="87.5" x2="75" y2="87.5" stroke="#facc15" stroke-width="1"/>
  <rect x="44" y="68" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="54" rx="20" ry="21" fill="#fed7aa"/>
  <path d="M36 55 C36 67 64 67 64 55 C64 69 36 69 36 55 Z" fill="#9a3412" opacity="0.18"/>
  <path d="M44 63 Q50 67 56 63" stroke="#9a3412" stroke-width="2" fill="none" stroke-linecap="round"/>
  <g class="anim-gleam">
    <rect x="32" y="45" width="16" height="12" rx="4" fill="#0f172a"/>
    <rect x="52" y="45" width="16" height="12" rx="4" fill="#0f172a"/>
    <line x1="48" y1="48" x2="52" y2="48" stroke="#facc15" stroke-width="2"/>
    <line x1="35" y1="47" x2="44" y2="54" stroke="#ffffff" stroke-width="1.5" opacity="0.8" stroke-linecap="round"/>
    <line x1="55" y1="47" x2="64" y2="54" stroke="#ffffff" stroke-width="1.5" opacity="0.8" stroke-linecap="round"/>
  </g>
  <g class="anim-bob">
    <path d="M28 34 C28 18 72 18 72 34 Z" fill="#0f172a"/>
    <path d="M24 34 Q50 38 76 34 Q74 40 26 40 Z" fill="#1e293b"/>
    <line x1="28" y1="34" x2="72" y2="34" stroke="#facc15" stroke-width="2"/>
    <circle cx="50" cy="26" r="3.5" fill="#facc15"/>
    <path d="M42 26 Q50 22 58 26" stroke="#facc15" stroke-width="1.8" fill="none"/>
  </g>
</svg>`
    },
    {
      id: 'persona-abuelita',
      name: 'Doña Carmen',
      badge: 'Viajera Experta',
      description: 'Abuelita viajera alegre con sombrero de sol, lentes y perlas',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #fb923c 0%, #ea580c 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-carmen" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#fb923c"/>
      <stop offset="100%" stop-color="#ea580c"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-carmen)"/>
  <path d="M26 86 Q50 78 74 86 L74 98 L26 98 Z" fill="#0d9488"/>
  <circle cx="44" cy="90" r="2" fill="#fef08a"/>
  <circle cx="56" cy="92" r="2" fill="#fef08a"/>
  <g fill="#e2e8f0">
    <circle cx="30" cy="46" r="10"/>
    <circle cx="70" cy="46" r="10"/>
    <circle cx="34" cy="34" r="9"/>
    <circle cx="66" cy="34" r="9"/>
    <circle cx="50" cy="28" r="10"/>
  </g>
  <rect x="44" y="68" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="55" rx="20" ry="20" fill="#fed7aa"/>
  <circle cx="36" cy="59" r="4" fill="#fb7185" opacity="0.4"/>
  <circle cx="64" cy="59" r="4" fill="#fb7185" opacity="0.4"/>
  <path d="M44 63 Q50 68 56 63" stroke="#be123c" stroke-width="2" fill="none" stroke-linecap="round"/>
  <g class="anim-gleam">
    <circle cx="41" cy="51" r="7.5" fill="rgba(244,63,94,0.15)" stroke="#e11d48" stroke-width="2"/>
    <circle cx="59" cy="51" r="7.5" fill="rgba(244,63,94,0.15)" stroke="#e11d48" stroke-width="2"/>
    <line x1="48.5" y1="51" x2="51.5" y2="51" stroke="#e11d48" stroke-width="2"/>
  </g>
  <g class="anim-blink">
    <circle cx="41" cy="51" r="2.2" fill="#1c1917"/>
    <circle cx="59" cy="51" r="2.2" fill="#1c1917"/>
  </g>
  <circle cx="28" cy="56" r="3.2" fill="#ffffff" stroke="#cbd5e1" stroke-width="0.8"/>
  <circle cx="72" cy="56" r="3.2" fill="#ffffff" stroke="#cbd5e1" stroke-width="0.8"/>
  <g class="anim-sway">
    <ellipse cx="50" cy="30" rx="34" ry="11" fill="#fef08a"/>
    <ellipse cx="50" cy="26" rx="19" ry="9" fill="#fde047"/>
    <path d="M31 29 Q50 33 69 29" stroke="#0d9488" stroke-width="3.5" fill="none" stroke-linecap="round"/>
  </g>
</svg>`
    },
    {
      id: 'persona-abuelito',
      name: 'Don Roberto',
      badge: 'Aventurero Senior',
      description: 'Caballero amable con boina inglesa, barba blanca y binoculares',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #0f766e 0%, #115e59 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-roberto" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#0f766e"/>
      <stop offset="100%" stop-color="#115e59"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-roberto)"/>
  <path d="M26 86 Q50 78 74 86 L74 98 L26 98 Z" fill="#b45309"/>
  <path d="M44 86 L50 98 L56 86" stroke="#fef3c7" stroke-width="2" fill="none"/>
  <path d="M36 82 Q50 92 64 82" stroke="#0f172a" stroke-width="2.5" fill="none"/>
  <rect x="44" y="66" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="52" rx="19" ry="20" fill="#fed7aa"/>
  <path d="M35 56 C35 74 65 74 65 56 C61 62 55 60 50 63 C45 60 39 62 35 56 Z" fill="#f8fafc"/>
  <path d="M42 58 Q50 64 58 58" stroke="#cbd5e1" stroke-width="2" fill="none" stroke-linecap="round"/>
  <g class="anim-blink">
    <ellipse cx="42" cy="47" rx="2.2" ry="2.8" fill="#1c1917"/>
    <ellipse cx="58" cy="47" rx="2.2" ry="2.8" fill="#1c1917"/>
  </g>
  <path d="M37 45 L35 48 M63 45 L65 48" stroke="#b45309" stroke-width="1.2" stroke-linecap="round"/>
  <g class="anim-bob">
    <path d="M24 35 C26 19 74 19 76 35 C80 37 72 41 50 40 C28 41 20 37 24 35 Z" fill="#78350f"/>
    <circle cx="50" cy="24" r="2.5" fill="#92400e"/>
    <path d="M30 36 Q50 38 70 36" stroke="#92400e" stroke-width="2" fill="none"/>
  </g>
</svg>`
    },
    {
      id: 'persona-aventurero',
      name: 'Leo Mochilero',
      badge: 'Trotamundos',
      description: 'Joven aventurero con gorro de lana, abrigo outdoor y energía de viaje',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #f97316 0%, #e11d48 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-leo" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#f97316"/>
      <stop offset="100%" stop-color="#e11d48"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-leo)"/>
  <polygon points="12,86 36,54 56,86" fill="#7f1d1d" opacity="0.3"/>
  <polygon points="44,86 68,48 92,86" fill="#7f1d1d" opacity="0.4"/>
  <path d="M24 86 Q50 78 76 86 L76 98 L24 98 Z" fill="#0284c7"/>
  <line x1="50" y1="80" x2="50" y2="98" stroke="#38bdf8" stroke-width="2.5"/>
  <circle cx="64" cy="88" r="3" fill="#facc15"/>
  <rect x="44" y="68" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="55" rx="19" ry="20" fill="#fed7aa"/>
  <path d="M30 46 Q28 58 33 54 M70 46 Q72 58 67 54" stroke="#451a03" stroke-width="4" stroke-linecap="round"/>
  <g class="anim-blink">
    <ellipse cx="42" cy="52" rx="2.5" ry="3.2" fill="#1c1917"/>
    <circle cx="41.2" cy="51" r="1" fill="#ffffff"/>
    <ellipse cx="58" cy="52" rx="2.5" ry="3.2" fill="#1c1917"/>
    <circle cx="57.2" cy="51" r="1" fill="#ffffff"/>
  </g>
  <path d="M43 61 Q50 67 57 61" stroke="#9a3412" stroke-width="2.2" fill="none" stroke-linecap="round"/>
  <g class="anim-bob">
    <path d="M28 38 C28 18 72 18 72 38 Z" fill="#f59e0b"/>
    <rect x="26" y="36" width="48" height="7" rx="3" fill="#d97706"/>
    <line x1="34" y1="37" x2="34" y2="42" stroke="#b45309" stroke-width="1.5"/>
    <line x1="42" y1="37" x2="42" y2="42" stroke="#b45309" stroke-width="1.5"/>
    <line x1="50" y1="37" x2="50" y2="42" stroke="#b45309" stroke-width="1.5"/>
    <line x1="58" y1="37" x2="58" y2="42" stroke="#b45309" stroke-width="1.5"/>
    <line x1="66" y1="37" x2="66" y2="42" stroke="#b45309" stroke-width="1.5"/>
  </g>
</svg>`
    },
    {
      id: 'persona-ejecutiva',
      name: 'Valentina VIP',
      badge: 'Socio Diamante',
      description: 'Viajera frecuente ejecutiva con lentes oscuros y blazer elegante',
      category: 'personas',
      bgGradient: 'linear-gradient(135deg, #701a75 0%, #3b0764 100%)',
      svg: `<svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg" class="svg-avatar">
  <defs>
    <linearGradient id="bg-valentina" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#701a75"/>
      <stop offset="100%" stop-color="#3b0764"/>
    </linearGradient>
  </defs>
  <circle cx="50" cy="50" r="48" fill="url(#bg-valentina)"/>
  <path d="M24 86 Q50 78 76 86 L76 98 L24 98 Z" fill="#831843"/>
  <polygon points="50,84 42,98 58,98" fill="#0f172a"/>
  <circle cx="50" cy="80" r="2.5" fill="#facc15"/>
  <path d="M30 36 C30 18 70 18 70 36 C74 54 72 68 68 68 C66 52 66 38 50 36 C34 38 34 52 32 68 C28 68 26 54 30 36 Z" fill="#18181b"/>
  <rect x="44" y="68" width="12" height="12" fill="#fed7aa"/>
  <ellipse cx="50" cy="54" rx="19" ry="20" fill="#fed7aa"/>
  <path d="M44 63 Q50 67 56 63" stroke="#be123c" stroke-width="2.2" fill="none" stroke-linecap="round"/>
  <g class="anim-gleam">
    <polygon points="29,45 48,45 45,55 33,55" fill="#09090b"/>
    <polygon points="52,45 71,45 67,55 55,55" fill="#09090b"/>
    <line x1="48" y1="47" x2="52" y2="47" stroke="#facc15" stroke-width="2"/>
    <line x1="33" y1="47" x2="42" y2="53" stroke="#ffffff" stroke-width="1.3" opacity="0.8" stroke-linecap="round"/>
    <line x1="56" y1="47" x2="65" y2="53" stroke="#ffffff" stroke-width="1.3" opacity="0.8" stroke-linecap="round"/>
  </g>
  <circle cx="28" cy="56" r="3.5" fill="none" stroke="#facc15" stroke-width="1.8"/>
  <circle cx="72" cy="56" r="3.5" fill="none" stroke="#facc15" stroke-width="1.8"/>
</svg>`
    }
  ];

  readonly emailForm = this.fb.nonNullable.group({
    correoElectronico: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    contrasenaActual: ['', [Validators.required, Validators.maxLength(72)]]
  });

  readonly passwordForm = this.fb.nonNullable.group({
    contrasenaActual: ['', [Validators.required, Validators.maxLength(72)]],
    nuevaContrasena: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    confirmarContrasena: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]]
  });

  constructor() {
    // Sanitizar cada SVG para permitir renderizado seguro con [innerHTML]
    for (const char of this.characterAvatars) {
      char.safeSvg = this.sanitizer.bypassSecurityTrustHtml(char.svg);
    }
  }

  get iniciales(): string {
    const nom = this.perfil?.usuario?.nombre?.trim() ?? '';
    const ape = this.perfil?.usuario?.apellido?.trim() ?? '';
    const inicial1 = nom ? nom[0] : '';
    const inicial2 = ape ? ape[0] : '';
    return `${inicial1}${inicial2}`.toUpperCase() || 'P';
  }

  get avatarStorageKey(): string {
    const rut = this.perfil?.usuario?.rut?.trim();
    return rut ? `skyway_avatar_${rut}` : 'skyway_avatar_default';
  }

  get currentCharacter(): CharacterAvatar | undefined {
    return this.characterAvatars.find(c => c.id === this.selectedCharacterId);
  }

  get currentAvatarLabel(): string {
    if (this.selectedAvatarType === 'character') {
      return this.currentCharacter?.name ?? 'Avatar animado';
    }
    if (this.selectedAvatarType === 'custom') {
      return 'Foto personalizada';
    }
    return `Iniciales (${this.iniciales})`;
  }

  get filteredAvatars(): CharacterAvatar[] {
    if (this.avatarCategory === 'todos') {
      return this.characterAvatars;
    }
    return this.characterAvatars.filter(c => c.category === this.avatarCategory);
  }

  ionViewWillEnter() {
    void this.load();
    this.loadSavedAvatar();
  }

  async load() {
    this.busy = true;
    this.error = '';
    try {
      this.perfil = await firstValueFrom(this.api.me());
      this.store.setPerfil(this.perfil);
      this.emailForm.patchValue({ correoElectronico: this.perfil.usuario.correoElectronico ?? '' });
      this.loadSavedAvatar();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
    }
  }

  // Carga el avatar persistido en localStorage para el usuario actual
  loadSavedAvatar() {
    const key = this.avatarStorageKey;
    let raw = localStorage.getItem(key);
    if (!raw && key !== 'skyway_avatar_default') {
      raw = localStorage.getItem('skyway_avatar_default');
    }

    if (!raw) {
      this.selectedAvatarType = 'character';
      this.selectedCharacterId = 'perro-piloto';
      return;
    }

    const validIds = this.characterAvatars.map(c => c.id);

    try {
      const parsed = JSON.parse(raw);
      if (parsed && typeof parsed === 'object') {
        const type = parsed.type === 'custom' || parsed.type === 'initials' ? parsed.type : 'character';
        const id = validIds.includes(parsed.id) ? parsed.id : 'perro-piloto';
        const data = isSafeImageDataUrl(parsed.data) ? parsed.data : null;

        this.selectedAvatarType = (type === 'custom' && !data) ? 'character' : type;
        this.selectedCharacterId = id;
        this.customPhotoUrl = data;
        return;
      }
    } catch {
      // Si no es JSON, evaluar formato plano
    }

    if (isSafeImageDataUrl(raw)) {
      this.selectedAvatarType = 'custom';
      this.customPhotoUrl = raw;
    } else if (raw === 'initials') {
      this.selectedAvatarType = 'initials';
    } else {
      this.selectedAvatarType = 'character';
      this.selectedCharacterId = validIds.includes(raw) ? raw : 'perro-piloto';
    }
  }

  // Guarda el estado del avatar en localStorage
  private saveAvatarState(type: 'character' | 'custom' | 'initials', id?: string, customData?: string) {
    this.selectedAvatarType = type;
    if (id) {
      this.selectedCharacterId = id;
    }
    if (customData !== undefined) {
      this.customPhotoUrl = isSafeImageDataUrl(customData) ? customData : null;
    }

    const payload = JSON.stringify({
      type: this.selectedAvatarType,
      id: this.selectedCharacterId,
      data: this.customPhotoUrl
    });

    const key = this.avatarStorageKey;
    try {
      localStorage.setItem(key, payload);
      if (key !== 'skyway_avatar_default') {
        localStorage.setItem('skyway_avatar_default', payload);
      }
    } catch (err) {
      console.warn('No se pudo guardar el avatar en localStorage:', err);
    }
  }

  toggleAvatarSelector() {
    this.showAvatarSelector = !this.showAvatarSelector;
    this.avatarSuccess = '';
  }

  setAvatarCategory(cat: 'todos' | 'perros' | 'gatos' | 'personas' | 'mifoto') {
    this.avatarCategory = cat;
  }

  async selectCharacterAvatar(id: string) {
    this.saveAvatarState('character', id);
    const char = this.currentCharacter;
    this.avatarSuccess = `¡Excelente elección! Ahora tu avatar es ${char?.name || 'tu personaje animado'}.`;
    const toast = await this.toasts.create({
      message: `¡Avatar actualizado: ${char?.name || 'Guardado'}!`,
      duration: 2200,
      color: 'success',
      icon: 'checkmark-circle'
    });
    await toast.present();
  }

  async selectCustomPhoto(dataUrl: string) {
    if (!isSafeImageDataUrl(dataUrl)) {
      void this.toasts.create({
        message: 'La imagen seleccionada no tiene un formato seguro.',
        duration: 3000,
        color: 'danger',
        icon: 'alert-circle-outline'
      }).then(t => t.present());
      return;
    }
    this.saveAvatarState('custom', undefined, dataUrl);
    this.avatarSuccess = '¡Tu foto personalizada ha sido guardada y está activa!';
    const toast = await this.toasts.create({
      message: '¡Foto personalizada guardada con éxito!',
      duration: 2200,
      color: 'success',
      icon: 'checkmark-circle'
    });
    await toast.present();
  }

  async selectInitialsAvatar() {
    this.saveAvatarState('initials');
    this.avatarSuccess = `Avatar actualizado para mostrar tus iniciales (${this.iniciales}).`;
    const toast = await this.toasts.create({
      message: '¡Ahora usando tus iniciales!',
      duration: 2200,
      color: 'success',
      icon: 'checkmark-circle'
    });
    await toast.present();
  }

  // Manejador para carga y compresión en canvas de foto desde celular o PC
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    // 1. Validar tipo MIME contra allowlist explícita (rechazar SVG y archivos arbitrarios)
    const fileMime = (file.type || '').toLowerCase();
    if (!ALLOWED_MIME_TYPES.includes(fileMime as any)) {
      void this.toasts.create({
        message: 'Por favor selecciona un archivo de imagen válido (JPG, PNG, WebP o GIF). Los archivos SVG no están permitidos.',
        duration: 3200,
        color: 'warning',
        icon: 'alert-circle-outline'
      }).then(t => t.present());
      input.value = '';
      return;
    }

    // 2. Validar tamaño máximo del archivo para prevenir DoS / consumo excesivo de memoria
    if (file.size > MAX_UPLOAD_BYTES) {
      void this.toasts.create({
        message: 'La imagen seleccionada es demasiado pesada (máx 5 MB). Por favor selecciona una imagen más liviana.',
        duration: 3200,
        color: 'warning',
        icon: 'alert-circle-outline'
      }).then(t => t.present());
      input.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onerror = () => {
      void this.toasts.create({
        message: 'No fue posible leer la imagen seleccionada.',
        duration: 3000,
        color: 'danger',
        icon: 'alert-circle-outline'
      }).then(t => t.present());
    };
    reader.onload = () => {
      const img = new Image();
      img.onerror = () => {
        void this.toasts.create({
          message: 'El archivo seleccionado no es una imagen válida o está dañado.',
          duration: 3000,
          color: 'danger',
          icon: 'alert-circle-outline'
        }).then(t => t.present());
      };
      img.onload = () => {
        // Redimensionar mediante Canvas a max 256x256 para almacenamiento eficiente y fluido
        const maxDim = 256;
        let width = img.width;
        let height = img.height;

        if (width > height) {
          if (width > maxDim) {
            height = Math.round((height * maxDim) / width);
            width = maxDim;
          }
        } else {
          if (height > maxDim) {
            width = Math.round((width * maxDim) / height);
            height = maxDim;
          }
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0, width, height);
          const dataUrl = canvas.toDataURL('image/jpeg', 0.88);
          if (isSafeImageDataUrl(dataUrl)) {
            void this.selectCustomPhoto(dataUrl);
          }
        }
      };
      img.src = reader.result as string;
    };
    reader.readAsDataURL(file);

    // Resetear valor para permitir seleccionar la misma foto nuevamente si se desea
    input.value = '';
  }

  async guardarCorreo() {
    this.emailError = '';
    this.emailSuccess = '';
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
      this.emailSuccess = 'Correo electrónico actualizado correctamente.';
      const toast = await this.toasts.create({ message: 'Correo actualizado con éxito.', duration: 2500, color: 'success' });
      await toast.present();
    } catch (e) {
      this.emailError = errorMessage(e);
    } finally {
      this.savingEmail = false;
    }
  }

  async guardarContrasena() {
    this.passwordError = '';
    this.passwordSuccess = '';
    if (this.passwordForm.invalid || this.savingPassword) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { contrasenaActual, nuevaContrasena, confirmarContrasena } = this.passwordForm.getRawValue();
    if (nuevaContrasena !== confirmarContrasena) {
      this.passwordError = 'Las contraseñas nuevas no coinciden.';
      return;
    }
    if (contrasenaActual === nuevaContrasena) {
      this.passwordError = 'La nueva contraseña debe ser distinta de la actual.';
      return;
    }

    this.savingPassword = true;
    this.error = '';
    try {
      await firstValueFrom(this.api.cambiarContrasena(contrasenaActual, nuevaContrasena));
      this.passwordForm.reset();
      this.passwordSuccess = 'Tu contraseña ha sido actualizada con éxito.';
      const toast = await this.toasts.create({ message: 'Contraseña actualizada con éxito.', duration: 2500, color: 'success' });
      await toast.present();
    } catch (e) {
      this.passwordError = errorMessage(e);
    } finally {
      this.savingPassword = false;
    }
  }
}
