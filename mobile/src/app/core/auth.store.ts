import { Injectable } from '@angular/core';
import { Perfil, LoginResponse } from './models';

const SESSION_CACHE_KEY = 'skyway_session_cache';
const BACKGROUND_SINCE_KEY = 'skyway_bg_since';
const BACKGROUND_SESSION_MS = 5 * 60 * 1000;

interface CachedSession {
  token: string;
  expiresAt: number;
  perfil: Perfil | null;
}

/**
 * Almacén de sesión con soporte de caché temporal en segundo plano:
 * - Mientras el usuario está dentro de la app, la sesión permanece activa sin temporizador de inactividad.
 * - Si el usuario sale con Home o cambia de app, el caché se conserva hasta por 5 minutos máximo.
 * - Si el usuario confirma "¿Salir realmente de la aplicación?" -> "Sí", `clear()` borra la sesión en el acto.
 */
@Injectable({ providedIn: 'root' })
export class AuthStore {
  private token: string | null = null;
  private expiresAt = 0;
  perfil: Perfil | null = null;

  constructor() {
    this.restaurarDesdeCacheSiValido();
  }

  private restaurarDesdeCacheSiValido(): void {
    try {
      const raw = localStorage.getItem(SESSION_CACHE_KEY);
      if (!raw) return;

      // Verificar si estuvo fuera de la app por más de 5 minutos
      const bgSince = Number(localStorage.getItem(BACKGROUND_SINCE_KEY) || 0);
      if (bgSince > 0 && Date.now() - bgSince >= BACKGROUND_SESSION_MS) {
        this.clear();
        return;
      }

      const parsed = JSON.parse(raw) as Partial<CachedSession>;
      if (
        typeof parsed.token === 'string' &&
        parsed.token &&
        typeof parsed.expiresAt === 'number' &&
        Date.now() < parsed.expiresAt
      ) {
        this.token = parsed.token;
        this.expiresAt = parsed.expiresAt;
        this.perfil = parsed.perfil ?? null;
      } else {
        this.clear();
      }
    } catch {
      this.clear();
    }
  }

  private guardarEnCache(): void {
    try {
      if (!this.token) {
        localStorage.removeItem(SESSION_CACHE_KEY);
        return;
      }
      const data: CachedSession = {
        token: this.token,
        expiresAt: this.expiresAt,
        perfil: this.perfil
      };
      localStorage.setItem(SESSION_CACHE_KEY, JSON.stringify(data));
    } catch {
      // Ignorar si el almacenamiento no está disponible
    }
  }

  set(data: LoginResponse) {
    this.token = data.accessToken;
    this.expiresAt = Date.now() + data.expiresIn * 1000;
    this.perfil = data.perfil;
    try {
      localStorage.removeItem(BACKGROUND_SINCE_KEY);
    } catch {
      // Ignorar
    }
    this.guardarEnCache();
  }

  setPerfil(perfil: Perfil) {
    this.perfil = perfil;
    this.guardarEnCache();
  }

  get accessToken() {
    if (this.token && Date.now() >= this.expiresAt) {
      this.clear();
    }
    return this.token;
  }

  get authenticated() {
    return this.accessToken !== null;
  }

  clear() {
    this.token = null;
    this.expiresAt = 0;
    this.perfil = null;
    try {
      localStorage.removeItem(SESSION_CACHE_KEY);
      localStorage.removeItem(BACKGROUND_SINCE_KEY);
    } catch {
      // Ignorar
    }
  }
}
