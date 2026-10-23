import { Injectable } from '@angular/core';
import * as THREE from 'three';
import { geoEquirectangular, geoPath } from 'd3-geo';
import { feature, mesh } from 'topojson-client';

export interface GlobeCachedAssets {
  worldData: any;
  satDay: THREE.Texture | null;
  satBump: THREE.Texture | null;
  satWater: THREE.Texture | null;
  globeTexture: THREE.CanvasTexture | null;
  specularTexture: THREE.CanvasTexture;
}

@Injectable({
  providedIn: 'root'
})
export class GloboTexturaService {
  private cachedAssets: GlobeCachedAssets | null = null;
  private pendingPromise: Promise<GlobeCachedAssets> | null = null;

  async getAssets(): Promise<GlobeCachedAssets> {
    if (this.cachedAssets) {
      return this.cachedAssets;
    }
    if (this.pendingPromise) {
      return this.pendingPromise;
    }
    this.pendingPromise = this.buildAssets();
    try {
      this.cachedAssets = await this.pendingPromise;
      return this.cachedAssets;
    } finally {
      this.pendingPromise = null;
    }
  }

  private loadTextureWithFallback(localUrl: string, cdnUrl: string): Promise<THREE.Texture | null> {
    const loadOne = (url: string) =>
      new Promise<THREE.Texture | null>(resolve => {
        const loader = new THREE.TextureLoader();
        loader.setCrossOrigin('anonymous');
        loader.load(
          url,
          tex => resolve(tex),
          undefined,
          () => resolve(null)
        );
      });

    return Promise.race([
      (async () => {
        const localTex = await loadOne(localUrl);
        if (localTex) return localTex;
        return loadOne(cdnUrl);
      })(),
      new Promise<THREE.Texture | null>(res => setTimeout(() => res(null), 20000))
    ]);
  }

  private async buildAssets(): Promise<GlobeCachedAssets> {
    let worldData: any = null;

    // 1. Cargar TopoJSON de alta resolución (50m) local o desde CDN
    for (const url of [
      'assets/geo/countries-50m.json',
      'https://cdn.jsdelivr.net/npm/world-atlas@2/countries-50m.json',
      'https://unpkg.com/world-atlas@2/countries-50m.json',
      'assets/geo/countries-110m.json',
      'https://cdn.jsdelivr.net/npm/world-atlas@2/countries-110m.json'
    ]) {
      try {
        const r = await fetch(url);
        if (r.ok) {
          worldData = await r.json();
          break;
        }
      } catch {
        // continuar
      }
    }

    if (!worldData) {
      throw new Error('No se pudieron cargar los datos geográficos del mapa.');
    }

    // 2. Cargar texturas satelitales reales NASA Blue Marble + Topografía + Agua (Real Life)
    const CDN_IMG = 'https://unpkg.com/three-globe@2.31.0/example/img/';
    const [satDay, satBump, satWater] = await Promise.all([
      this.loadTextureWithFallback('assets/geo/earth-blue-marble.jpg', CDN_IMG + 'earth-blue-marble.jpg'),
      this.loadTextureWithFallback('assets/geo/earth-topology.png', CDN_IMG + 'earth-topology.png'),
      this.loadTextureWithFallback('assets/geo/earth-water.png', CDN_IMG + 'earth-water.png')
    ]);

    const sat = !!satDay;
    const land = feature(worldData, worldData.objects.land) as any;

    // 3. Canvas especular de respaldo (océano brillante, tierra mate) en 1024x512 como el original
    const specCanvas = document.createElement('canvas');
    specCanvas.width = 1024;
    specCanvas.height = 512;
    const sc = specCanvas.getContext('2d');
    if (!sc) throw new Error('No se pudo inicializar canvas especular');

    const sproj = geoEquirectangular()
      .scale(1024 / (2 * Math.PI))
      .translate([512, 256]);
    const spath = geoPath(sproj, sc);

    const specGrad = sc.createLinearGradient(0, 0, 0, 512);
    specGrad.addColorStop(0, '#3a4a5e');
    specGrad.addColorStop(0.5, '#485870');
    specGrad.addColorStop(1, '#3a4a5e');
    sc.fillStyle = specGrad;
    sc.fillRect(0, 0, 1024, 512);

    sc.beginPath();
    spath(land);
    sc.fillStyle = '#050505';
    sc.fill();

    const specularTexture = new THREE.CanvasTexture(specCanvas);

    // Si las texturas satelitales NASA están listas, retornarlas directamente sin degradar
    if (sat) {
      return {
        worldData,
        satDay,
        satBump,
        satWater,
        globeTexture: null,
        specularTexture
      };
    }

    // 4. Fallback procedural de alta resolución (2048x1024) idéntico al HTML original
    const TEX_W = 2048;
    const TEX_H = 1024;

    const maskCanvas = document.createElement('canvas');
    maskCanvas.width = TEX_W;
    maskCanvas.height = TEX_H;
    const mc = maskCanvas.getContext('2d');
    if (!mc) throw new Error('No se pudo inicializar canvas 2D');

    mc.fillStyle = '#000000';
    mc.fillRect(0, 0, TEX_W, TEX_H);

    const proj = geoEquirectangular()
      .scale(TEX_W / (2 * Math.PI))
      .translate([TEX_W / 2, TEX_H / 2]);

    const pathGen = geoPath(proj, mc);
    mc.beginPath();
    pathGen(land);
    mc.fillStyle = '#ffffff';
    mc.fill();

    const maskData = mc.getImageData(0, 0, TEX_W, TEX_H).data;

    const B = {
      rainforest: [8, 58, 14],
      tropicalWet: [18, 78, 18],
      tropicalDry: [42, 95, 26],
      savanna: [118, 98, 32],
      desert: [205, 165, 72],
      semiArid: [158, 138, 72],
      atacama: [138, 108, 50],
      outback: [192, 102, 42],
      outbackSpin: [175, 132, 62],
      medScrub: [88, 118, 38],
      temperate: [52, 105, 28],
      temperEuro: [60, 118, 34],
      temperEAsia: [48, 95, 28],
      boreal: [28, 60, 18],
      tundra: [90, 108, 75],
      tundraGrey: [105, 118, 95],
      polarEdge: [185, 205, 225],
      polarIce: [228, 238, 248],
      patSteppe: [90, 100, 78],
      patForest: [50, 82, 36],
      indiaGreen: [46, 108, 26],
      chinaLoess: [145, 130, 65],
      tibetan: [115, 118, 72],
      australGreen: [68, 105, 38]
    };

    const smooth = (t: number) => {
      t = Math.max(0, Math.min(1, t));
      return t * t * (3 - 2 * t);
    };
    const smoothstep = (e0: number, e1: number, x: number) => smooth((x - e0) / (e1 - e0));
    const lerp3 = (a: number[], b: number[], t: number) => {
      t = Math.max(0, Math.min(1, t));
      return [
        (a[0] + (b[0] - a[0]) * t) | 0,
        (a[1] + (b[1] - a[1]) * t) | 0,
        (a[2] + (b[2] - a[2]) * t) | 0
      ];
    };
    const latBand = (lat: number, low: number, high: number, edge: number) => {
      if (lat < low - edge || lat > high + edge) return 0;
      return Math.min(smoothstep(low - edge, low, lat), 1 - smoothstep(high, high + edge, lat));
    };
    const lonBand = (lon: number, low: number, high: number, edge: number) => {
      if (lon < low - edge || lon > high + edge) return 0;
      return Math.min(smoothstep(low - edge, low, lon), 1 - smoothstep(high, high + edge, lon));
    };
    const box = (lat: number, lon: number, latLo: number, latHi: number, lonLo: number, lonHi: number, edge: number) =>
      Math.min(latBand(lat, latLo, latHi, edge), lonBand(lon, lonLo, lonHi, edge));

    const blend = (pairs: [number[], number][]) => {
      let r = 0, g = 0, b = 0, total = 0;
      for (const [c, w] of pairs) {
        if (w <= 0) continue;
        r += c[0] * w;
        g += c[1] * w;
        b += c[2] * w;
        total += w;
      }
      if (total === 0) return [0, 0, 0];
      return [(r / total) | 0, (g / total) | 0, (b / total) | 0];
    };

    const satelliteColorFull = (lat: number, lon: number) => {
      const a = Math.abs(lat);
      if (a > 60) {
        const tIce = smoothstep(62, 80, a);
        const tEdge = smoothstep(58, 65, a);
        const base = lerp3(B.tundraGrey, B.polarEdge, tEdge);
        const greenland = box(lat, lon, 62, 85, -55, -14, 6);
        const glColor = lerp3(B.polarEdge, B.polarIce, smoothstep(68, 82, lat));
        if (greenland > 0.01) return lerp3(base, glColor, greenland);
        return lerp3(base, B.polarIce, tIce);
      }

      const weights: [number[], number][] = [];
      const wPolar = latBand(a, 55, 62, 5);
      if (wPolar > 0) weights.push([B.tundraGrey, wPolar]);
      const wTundra = box(lat, lon, 48, 60, -180, 180, 6);
      if (wTundra > 0) weights.push([B.tundra, wTundra * 0.5]);
      const wBoreal = latBand(lat, 45, 62, 6);
      if (wBoreal > 0) weights.push([B.boreal, wBoreal]);

      const latTempBase = latBand(lat, 35, 55, 8);
      const wEurope = latTempBase * box(lat, lon, 35, 58, -12, 30, 8);
      if (wEurope > 0) weights.push([B.temperEuro, wEurope * 1.4]);
      const wENAm = latTempBase * box(lat, lon, 35, 52, -100, -60, 8);
      if (wENAm > 0) weights.push([B.temperate, wENAm * 1.3]);
      const wEAsia = latTempBase * box(lat, lon, 32, 52, 100, 145, 8);
      if (wEAsia > 0) weights.push([B.temperEAsia, wEAsia * 1.2]);
      const wSiberia = latTempBase * box(lat, lon, 45, 62, 55, 170, 8);
      if (wSiberia > 0) weights.push([B.boreal, wSiberia * 1.3]);
      if (latTempBase > 0) weights.push([B.temperate, latTempBase * 0.4]);

      const wSahara = box(lat, lon, 15, 33, -18, 42, 7);
      if (wSahara > 0) weights.push([B.desert, wSahara * 2.2]);
      const wArabian = box(lat, lon, 12, 30, 35, 60, 6);
      if (wArabian > 0) weights.push([B.desert, wArabian * 2.0]);
      const wAtacama = box(lat, lon, -32, -14, -76, -67, 5);
      if (wAtacama > 0) weights.push([B.atacama, wAtacama * 2.0]);
      const wOutback = box(lat, lon, -35, -15, 114, 142, 6);
      if (wOutback > 0) weights.push([B.outback, wOutback * 2.4]);
      const wAmazon = box(lat, lon, -12, 8, -80, -46, 7);
      if (wAmazon > 0) weights.push([B.rainforest, wAmazon * 2.5]);
      const wCongo = box(lat, lon, -6, 6, 10, 32, 6);
      if (wCongo > 0) weights.push([B.rainforest, wCongo * 2.3]);
      const wPatSteppe = box(lat, lon, -55, -38, -70, -55, 6);
      if (wPatSteppe > 0) weights.push([B.patSteppe, wPatSteppe * 2.0]);
      const wPatForest = box(lat, lon, -55, -38, -78, -70, 5);
      if (wPatForest > 0) weights.push([B.patForest, wPatForest * 2.0]);

      if (a < 10) weights.push([B.tropicalWet, 0.5]);
      else if (a < 23) weights.push([B.tropicalDry, 0.5]);
      else if (a < 35) weights.push([B.medScrub, 0.4]);
      else if (a < 50) weights.push([B.temperate, 0.4]);
      else if (a < 60) weights.push([B.boreal, 0.4]);
      else weights.push([B.tundraGrey, 0.4]);

      return blend(weights);
    };

    const oceanColor = (lat: number, lon: number) => {
      const a = Math.abs(lat);
      let r = 3, g = 10, b = 32;
      if (a > 65) {
        const t = (a - 65) / 25;
        r += t * 10;
        g += t * 18;
        b += t * 28;
      }
      if (a < 30 && lon > -130 && lon < -80) {
        g -= 1;
        b += 2;
      }
      return [r | 0, g | 0, b | 0];
    };

    const hash = (x: number, y: number) => (Math.sin(x * 127.1 + y * 311.7) * 43758.5453) % 1;
    const smoothN = (x: number, y: number) => {
      const xi = Math.floor(x), yi = Math.floor(y), xf = x - xi, yf = y - yi;
      const a = hash(xi, yi), b = hash(xi + 1, yi), c = hash(xi, yi + 1), d = hash(xi + 1, yi + 1);
      const u = xf * xf * (3 - 2 * xf), v = yf * yf * (3 - 2 * yf);
      return a * (1 - u) * (1 - v) + b * u * (1 - v) + c * (1 - u) * v + d * u * v;
    };
    const fbm = (lon: number, lat: number) => {
      let v = 0, amp = 1, freq = 0.12, total = 0;
      for (let i = 0; i < 3; i++) {
        v += smoothN(lon * freq, lat * freq) * amp;
        total += amp;
        amp *= 0.5;
        freq *= 2.1;
      }
      return v / total;
    };

    const texCanvas = document.createElement('canvas');
    texCanvas.width = TEX_W;
    texCanvas.height = TEX_H;
    const tc = texCanvas.getContext('2d');
    if (!tc) throw new Error('No se pudo inicializar canvas principal');

    const imgData = tc.createImageData(TEX_W, TEX_H);
    const pix = imgData.data;

    for (let py = 0; py < TEX_H; py++) {
      const lat = 90 - (py / TEX_H) * 180;
      for (let px = 0; px < TEX_W; px++) {
        const lon = (px / TEX_W) * 360 - 180;
        const idx = (py * TEX_W + px) * 4;
        const isLand = maskData[idx] > 128;

        let color: number[];
        if (isLand) {
          color = satelliteColorFull(lat, lon);
          const n = (fbm(lon, lat) - 0.5) * 22;
          color = [
            Math.max(0, Math.min(255, color[0] + n * 1.1)),
            Math.max(0, Math.min(255, color[1] + n * 0.95)),
            Math.max(0, Math.min(255, color[2] + n * 0.7))
          ];
        } else {
          color = oceanColor(lat, lon);
          const n = (smoothN(lon * 0.06, lat * 0.06) - 0.5) * 5;
          color = [
            Math.max(0, color[0] + n * 0.8),
            Math.max(0, color[1] + n * 0.9),
            Math.max(0, color[2] + n * 1.2)
          ];
        }

        pix[idx] = color[0];
        pix[idx + 1] = color[1];
        pix[idx + 2] = color[2];
        pix[idx + 3] = 255;
      }
    }

    tc.putImageData(imgData, 0, 0);

    const pathGen2 = geoPath(proj, tc);
    tc.beginPath();
    pathGen2(land);
    tc.strokeStyle = 'rgba(210, 230, 190, 0.32)';
    tc.lineWidth = 1.0;
    tc.stroke();

    if (worldData.objects?.countries) {
      const borders = mesh(worldData, worldData.objects.countries, (a: any, b: any) => a !== b) as any;
      tc.beginPath();
      pathGen2(borders);
      tc.strokeStyle = 'rgba(190, 215, 160, 0.16)';
      tc.lineWidth = 0.5;
      tc.stroke();
    }

    const globeTexture = new THREE.CanvasTexture(texCanvas);
    return {
      worldData,
      satDay: null,
      satBump: null,
      satWater: null,
      globeTexture,
      specularTexture
    };
  }
}
