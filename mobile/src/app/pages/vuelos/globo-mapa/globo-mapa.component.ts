import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  AfterViewInit,
  NgZone,
  inject
} from '@angular/core';
import * as THREE from 'three';
import { geoDistance, geoInterpolate, geoGraticule, geoBounds, geoContains } from 'd3-geo';
import { feature, mesh } from 'topojson-client';
import { GloboTexturaService, GlobeCachedAssets } from '../globo-textura.service';
import { ParadaRuta, RutaVuelo3D } from '../globo.models';

@Component({
  selector: 'app-globo-mapa',
  standalone: false,
  templateUrl: './globo-mapa.component.html',
  styleUrls: ['./globo-mapa.component.scss']
})
export class GloboMapaComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly texturaService = inject(GloboTexturaService);
  private readonly ngZone = inject(NgZone);

  @Input() ruta: RutaVuelo3D | null = null;
  @Output() cerrar = new EventEmitter<void>();

  @ViewChild('canvas3d', { static: false }) canvas3dRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('canvasLabels', { static: false }) canvasLabelsRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('container', { static: false }) containerRef!: ElementRef<HTMLDivElement>;

  cargando = true;
  estadoCarga = 'Cargando datos geográficos...';
  detallesExpandidos = false;

  // Tooltip de país al hacer tap
  tooltipVisible = false;
  tooltipNombre = '';
  tooltipCoords = '';
  tooltipX = 0;
  tooltipY = 0;
  private tooltipTimeout: any = null;

  // Three.js Core
  private renderer: THREE.WebGLRenderer | null = null;
  private scene: THREE.Scene | null = null;
  private camera: THREE.PerspectiveCamera | null = null;
  private animId: number | null = null;
  private globeMesh: THREE.Mesh | null = null;
  private markersGroup: THREE.Group | null = null;
  private routeGroup: THREE.Group | null = null;
  private bordersGroup: THREE.Group | null = null;
  private planeObjects: THREE.Mesh[] = [];

  // Geodatos y hover
  private countryFeatures: Array<{ f: any; b: any }> = [];
  private hlLine: THREE.LineSegments | null = null;
  private hlId: any = null;

  // Estado de cámara y rotación
  private isDrag = false;
  private dragStartX = 0;
  private dragStartY = 0;
  private prevX = 0;
  private prevY = 0;
  private velX = 0;
  private velY = 0;
  private rotX = 0.1;
  private rotY = 0;
  private targetRotX = 0.1;
  private targetRotY = 0;
  private zoom = 2.7;
  private targetZoom = 2.7;
  private isAutoCentering = false;
  private pauseAutoRotateUntil = 0;
  private readonly PAUSE_ROTATION_MS = 30000;

  // Pinch-to-zoom
  private initialPinchDist = 0;
  private initialPinchZoom = 2.7;

  // Parámetros del globo
  private readonly R = 1.0;
  private pulsePhase = 0;
  private lastTime = 0;

  // Listeners para desuscribir limpiamente
  private boundResize = () => this.onResize();
  private boundTouchMove = (e: TouchEvent) => this.onTouchMove(e);
  private boundTouchEnd = (e: TouchEvent) => this.onTouchEnd(e);
  private boundMouseMove = (e: MouseEvent) => this.onMouseMove(e);
  private boundMouseUp = () => this.onMouseUp();
  private boundMouseDown = (e: MouseEvent) => this.onMouseDown(e);
  private boundTouchStart = (e: TouchEvent) => this.onTouchStart(e);
  private boundWheel = (e: WheelEvent) => this.onWheel(e);

  ngOnInit() {
    this.calcularOrientacionInicial();
  }

  ngAfterViewInit() {
    this.inicializarVisualizador();
  }

  ngOnDestroy() {
    this.destruirVisualizador();
  }

  private calcularOrientacionInicial() {
    if (!this.ruta || !this.ruta.paradas.length) {
      this.rotX = 0.1;
      this.rotY = -Math.PI * 0.55;
      this.targetRotX = this.rotX;
      this.targetRotY = this.rotY;
      return;
    }

    // Calcular centro geográfico de las paradas
    const lats = this.ruta.paradas.map(p => p.latitud);
    const lons = this.ruta.paradas.map(p => p.longitud);
    const avgLat = lats.reduce((a, b) => a + b, 0) / lats.length;
    const avgLon = lons.reduce((a, b) => a + b, 0) / lons.length;

    // Fórmula geodésica para centrar hacia la cámara (+Z)
    this.rotX = (avgLat * Math.PI) / 180;
    this.rotY = -((avgLon + 90) * Math.PI) / 180;
    this.targetRotX = this.rotX;
    this.targetRotY = this.rotY;

    // Zoom adaptado a la distancia total
    if (this.ruta.distanciaKm > 10000) {
      this.targetZoom = 3.1;
    } else if (this.ruta.distanciaKm > 4000) {
      this.targetZoom = 2.7;
    } else {
      this.targetZoom = 2.3;
    }
    this.zoom = this.targetZoom;
  }

  centrarRuta() {
    this.isAutoCentering = true;
    this.pausarGiroAutomatico();
    this.calcularOrientacionInicial();
  }

  private pausarGiroAutomatico() {
    this.pauseAutoRotateUntil = performance.now() + this.PAUSE_ROTATION_MS;
  }

  zoomIn() {
    this.targetZoom = Math.max(1.3, this.targetZoom - 0.4);
  }

  zoomOut() {
    this.targetZoom = Math.min(4.4, this.targetZoom + 0.4);
  }

  toggleDetalles() {
    this.detallesExpandidos = !this.detallesExpandidos;
  }

  private async inicializarVisualizador() {
    try {
      this.estadoCarga = 'Cargando datos geográficos...';
      const assets = await this.texturaService.getAssets();

      this.estadoCarga = 'Construyendo globo 3D...';
      await new Promise(r => setTimeout(r, 20));

      this.ngZone.runOutsideAngular(() => {
        this.construirEscena3D(assets);
        this.iniciarAnimacion();
      });

      this.cargando = false;
    } catch (err) {
      console.error('Error inicializando globo 3D:', err);
      this.estadoCarga = 'No se pudo cargar el mapa 3D.';
    }
  }

  private construirEscena3D(assets: GlobeCachedAssets) {
    const canvas = this.canvas3dRef.nativeElement;
    const container = this.containerRef.nativeElement;
    const W = container.clientWidth || window.innerWidth;
    const H = container.clientHeight || window.innerHeight;

    // 1. Renderer en resolución nativa de alta nitidez (idéntico al HTML original)
    this.renderer = new THREE.WebGLRenderer({
      canvas,
      antialias: true,
      alpha: false,
      powerPreference: 'high-performance'
    });
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    this.renderer.setSize(W, H);
    this.renderer.setClearColor(0x060e1f, 1);

    // 2. Escena y Cámara
    this.scene = new THREE.Scene();
    this.camera = new THREE.PerspectiveCamera(42, W / H, 0.01, 100);
    this.camera.position.z = this.zoom;

    // 3. Luces idénticas al diseño original
    this.scene.add(new THREE.AmbientLight(0xccd8f0, 0.5));
    const sun = new THREE.DirectionalLight(0xfff5e8, 1.4);
    sun.position.set(5, 3, 4);
    this.scene.add(sun);
    const fill = new THREE.DirectionalLight(0x1a3388, 0.3);
    fill.position.set(-4, -2, -3);
    this.scene.add(fill);

    // 4. Estrellas de fondo (5000 estrellas como en el original)
    this.crearEstrellas();

    // 5. Malla del Globo (192x192 segmentos + Textura Satelital Real NASA Blue Marble + Topografía)
    const maxAniso = this.renderer.capabilities.getMaxAnisotropy();
    assets.specularTexture.needsUpdate = true;

    const globeGeo = new THREE.SphereGeometry(this.R, 192, 192);
    let globeMat: THREE.MeshPhongMaterial;

    if (assets.satDay) {
      assets.satDay.anisotropy = maxAniso;
      assets.satDay.minFilter = THREE.LinearMipmapLinearFilter;
      assets.satDay.needsUpdate = true;
      if (assets.satBump) assets.satBump.needsUpdate = true;
      if (assets.satWater) assets.satWater.needsUpdate = true;

      globeMat = new THREE.MeshPhongMaterial({
        map: assets.satDay,
        bumpMap: assets.satBump || null,
        bumpScale: 0.014,
        specularMap: assets.satWater || assets.specularTexture,
        specular: new THREE.Color(0x3a5577),
        shininess: 18
      });
    } else {
      if (assets.globeTexture) {
        assets.globeTexture.anisotropy = maxAniso;
        assets.globeTexture.needsUpdate = true;
      }
      globeMat = new THREE.MeshPhongMaterial({
        map: assets.globeTexture,
        specularMap: assets.specularTexture,
        specular: new THREE.Color(0x1a3355),
        shininess: 22
      });
    }

    this.globeMesh = new THREE.Mesh(globeGeo, globeMat);
    this.scene.add(this.globeMesh);

    // 6. Efectos de Atmósfera
    this.crearAtmosfera();

    // 7. Fronteras y costas vectoriales de alta resolución (50m)
    this.crearFronterasYCostas(assets.worldData);

    // 8. Marcadores de aeropuertos de la ruta
    this.crearMarcadoresAeropuertos();

    // 9. Arcos de vuelo y aviones animados
    this.crearRutasYAviones();

    // 10. Configurar eventos interactivos
    this.registrarEventos();
    this.onResize();
  }

  private crearEstrellas() {
    if (!this.scene) return;
    const N = 5000;
    const pos = new Float32Array(N * 3);
    for (let i = 0; i < N * 3; i++) {
      pos[i] = (Math.random() - 0.5) * 80;
    }
    const g = new THREE.BufferGeometry();
    g.setAttribute('position', new THREE.BufferAttribute(pos, 3));
    const sizes = new Float32Array(N);
    for (let i = 0; i < N; i++) {
      sizes[i] = Math.random() < 0.05 ? 0.08 : 0.035;
    }
    g.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
    const stars = new THREE.Points(
      g,
      new THREE.PointsMaterial({
        color: 0xffffff,
        size: 0.038,
        transparent: true,
        opacity: 0.75
      })
    );
    this.scene.add(stars);
  }

  private crearAtmosfera() {
    if (!this.scene) return;
    // Capas de brillo atmosférico idénticas al original
    this.scene.add(
      new THREE.Mesh(
        new THREE.SphereGeometry(this.R * 1.011, 64, 64),
        new THREE.MeshPhongMaterial({
          color: 0x2255cc,
          transparent: true,
          opacity: 0.05,
          side: THREE.FrontSide,
          depthWrite: false
        })
      )
    );

    this.scene.add(
      new THREE.Mesh(
        new THREE.SphereGeometry(this.R * 1.09, 64, 64),
        new THREE.MeshPhongMaterial({
          color: 0x1a44bb,
          transparent: true,
          opacity: 0.09,
          side: THREE.BackSide,
          depthWrite: false
        })
      )
    );

    this.scene.add(
      new THREE.Mesh(
        new THREE.SphereGeometry(this.R * 1.18, 48, 48),
        new THREE.MeshPhongMaterial({
          color: 0x0a2266,
          transparent: true,
          opacity: 0.04,
          side: THREE.BackSide,
          depthWrite: false
        })
      )
    );

    // Borde atmosférico Fresnel (96x96)
    this.scene.add(
      new THREE.Mesh(
        new THREE.SphereGeometry(this.R * 1.025, 96, 96),
        new THREE.ShaderMaterial({
          vertexShader:
            'varying vec3 vN; void main(){ vN = normalize(normalMatrix * normal); gl_Position = projectionMatrix * modelViewMatrix * vec4(position,1.0); }',
          fragmentShader:
            'varying vec3 vN; void main(){ float i = pow(0.68 - dot(vN, vec3(0.0,0.0,1.0)), 3.2); gl_FragColor = vec4(0.32,0.62,1.0,1.0) * i; }',
          blending: THREE.AdditiveBlending,
          side: THREE.BackSide,
          transparent: true,
          depthWrite: false
        })
      )
    );
  }

  private crearFronterasYCostas(worldData: any) {
    if (!this.scene || !worldData) return;
    this.bordersGroup = new THREE.Group();
    this.scene.add(this.bordersGroup);

    const lineMat = (color: number, opacity: number) =>
      new THREE.LineBasicMaterial({
        color,
        transparent: true,
        opacity,
        depthWrite: false
      });

    // Rejilla de coordenadas esféricas cada 15° (como el original)
    const graticule = geoGraticule().step([15, 15])() as any;
    this.bordersGroup.add(
      new THREE.LineSegments(
        this.linesToGeometry(graticule.coordinates, this.R * 1.001),
        lineMat(0x6fa8ff, 0.1)
      )
    );

    if (worldData.objects?.countries) {
      // Costas vectoriales nítidas
      const coastMesh = mesh(worldData, worldData.objects.countries, (a: any, b: any) => a === b) as any;
      if (coastMesh?.coordinates) {
        this.bordersGroup.add(
          new THREE.LineSegments(
            this.linesToGeometry(coastMesh.coordinates, this.R * 1.0016),
            lineMat(0xaad7ff, 0.55)
          )
        );
      }

      // Fronteras internacionales nítidas
      const borderMesh = mesh(worldData, worldData.objects.countries, (a: any, b: any) => a !== b) as any;
      if (borderMesh?.coordinates) {
        this.bordersGroup.add(
          new THREE.LineSegments(
            this.linesToGeometry(borderMesh.coordinates, this.R * 1.0018),
            lineMat(0xffffff, 0.75)
          )
        );
      }

      const countries = feature(worldData, worldData.objects.countries) as any;
      if (countries?.features) {
        this.countryFeatures = countries.features.map((f: any) => ({
          f,
          b: geoBounds(f)
        }));
      }
    }
  }

  private linesToGeometry(lines: number[][][], r: number): THREE.BufferGeometry {
    const pos: number[] = [];
    for (const line of lines) {
      for (let i = 0; i < line.length - 1; i++) {
        const a = line[i];
        const b = line[i + 1];
        const distDeg = (geoDistance(a as any, b as any) * 180) / Math.PI;
        const steps = Math.max(1, Math.ceil(distDeg / 0.75)); // Densidad exacta del HTML original (<=0.75°)
        const interp = geoInterpolate(a as any, b as any);

        let prev = this.latLonToVec3(a[1], a[0], r);
        for (let k = 1; k <= steps; k++) {
          const p = k === steps ? b : interp(k / steps);
          const cur = this.latLonToVec3(p[1], p[0], r);
          pos.push(prev.x, prev.y, prev.z, cur.x, cur.y, cur.z);
          prev = cur;
        }
      }
    }
    const g = new THREE.BufferGeometry();
    g.setAttribute('position', new THREE.Float32BufferAttribute(pos, 3));
    return g;
  }

  private crearMarcadoresAeropuertos() {
    if (!this.scene || !this.ruta) return;
    this.markersGroup = new THREE.Group();
    this.scene.add(this.markersGroup);

    for (const apt of this.ruta.paradas) {
      const colorNum = parseInt(apt.colorHex.replace('#', '0x'), 16);
      const pos = this.latLonToVec3(apt.latitud, apt.longitud, this.R);
      const dotR = apt.tipo === 'escala' ? 0.016 : 0.022;

      // Punto central
      const dot = new THREE.Mesh(
        new THREE.SphereGeometry(dotR, 16, 16),
        new THREE.MeshBasicMaterial({ color: colorNum })
      );
      dot.position.copy(pos);
      this.markersGroup.add(dot);

      // Anillo pulsante interior
      const ring1 = new THREE.Mesh(
        new THREE.RingGeometry(dotR * 1.8, dotR * 2.5, 32),
        new THREE.MeshBasicMaterial({
          color: colorNum,
          transparent: true,
          opacity: 0.65,
          side: THREE.DoubleSide
        })
      );
      ring1.position.copy(pos);
      ring1.lookAt(new THREE.Vector3(0, 0, 0));
      ring1.userData = { isPulse: true, pOff: Math.random() * Math.PI * 2 };
      this.markersGroup.add(ring1);

      // Anillo pulsante exterior
      const ring2 = new THREE.Mesh(
        new THREE.RingGeometry(dotR * 3.2, dotR * 3.8, 32),
        new THREE.MeshBasicMaterial({
          color: colorNum,
          transparent: true,
          opacity: 0.25,
          side: THREE.DoubleSide
        })
      );
      ring2.position.copy(pos);
      ring2.lookAt(new THREE.Vector3(0, 0, 0));
      ring2.userData = { isPulse: true, pOff: Math.PI };
      this.markersGroup.add(ring2);

      // Aguja vertical de localización
      const spikeGeo = new THREE.BufferGeometry().setFromPoints([
        pos.clone().multiplyScalar(1.0),
        pos.clone().multiplyScalar(1.0 + dotR * 3.2)
      ]);
      this.markersGroup.add(
        new THREE.Line(
          spikeGeo,
          new THREE.LineBasicMaterial({
            color: colorNum,
            transparent: true,
            opacity: 0.6
          })
        )
      );
    }
  }

  private crearRutasYAviones() {
    if (!this.scene || !this.ruta) return;
    this.routeGroup = new THREE.Group();
    this.scene.add(this.routeGroup);
    this.planeObjects = [];

    // Trazar cada tramo de la ruta
    this.ruta.tramos.forEach((tramo, index) => {
      const fr = tramo.origen;
      const to = tramo.destino;
      const lift = 0.22;
      const pts = this.greatCirclePoints(fr.latitud, fr.longitud, to.latitud, to.longitud, 120, lift);
      const curve = new THREE.CatmullRomCurve3(pts);

      // Tubo central principal (naranja brillante #ff9020)
      this.routeGroup!.add(
        new THREE.Mesh(
          new THREE.TubeGeometry(curve, 120, 0.0045, 6, false),
          new THREE.MeshBasicMaterial({
            color: 0xff9020,
            transparent: true,
            opacity: 0.95
          })
        )
      );

      // Tubo de brillo / resplandor
      this.routeGroup!.add(
        new THREE.Mesh(
          new THREE.TubeGeometry(curve, 120, 0.011, 6, false),
          new THREE.MeshBasicMaterial({
            color: 0xff6600,
            transparent: true,
            opacity: 0.22
          })
        )
      );

      // Resplandor exterior difuso
      this.routeGroup!.add(
        new THREE.Mesh(
          new THREE.TubeGeometry(curve, 120, 0.019, 6, false),
          new THREE.MeshBasicMaterial({
            color: 0xff4400,
            transparent: true,
            opacity: 0.07
          })
        )
      );

      // Avión animado en movimiento (punto blanco-dorado brillante)
      const airplane = new THREE.Mesh(
        new THREE.SphereGeometry(0.014, 12, 12),
        new THREE.MeshBasicMaterial({ color: 0xfff2a0 })
      );
      airplane.userData = {
        curve,
        t: (index * 0.45) % 1.0,
        speed: 0.0011 + Math.random() * 0.0003
      };
      this.routeGroup!.add(airplane);
      this.planeObjects.push(airplane);

      // Estela del avión
      const trail = new THREE.Mesh(
        new THREE.SphereGeometry(0.009, 8, 8),
        new THREE.MeshBasicMaterial({
          color: 0xff9020,
          transparent: true,
          opacity: 0.55
        })
      );
      trail.userData = {
        curve,
        t: (index * 0.45 - 0.02 + 1.0) % 1.0,
        speed: airplane.userData['speed']
      };
      this.routeGroup!.add(trail);
      this.planeObjects.push(trail);
    });
  }

  private latLonToVec3(lat: number, lon: number, r: number): THREE.Vector3 {
    const phi = ((90 - lat) * Math.PI) / 180;
    const theta = ((lon + 180) * Math.PI) / 180;
    return new THREE.Vector3(
      -r * Math.sin(phi) * Math.cos(theta),
      r * Math.cos(phi),
      r * Math.sin(phi) * Math.sin(theta)
    );
  }

  private greatCirclePoints(
    lat1: number,
    lon1: number,
    lat2: number,
    lon2: number,
    n: number,
    lift: number
  ): THREE.Vector3[] {
    const v1 = this.latLonToVec3(lat1, lon1, 1);
    const v2 = this.latLonToVec3(lat2, lon2, 1);
    const pts: THREE.Vector3[] = [];

    for (let i = 0; i <= n; i++) {
      const t = i / n;
      const v = new THREE.Vector3().lerpVectors(v1, v2, t).normalize();
      const h = lift * 4 * t * (1 - t);
      pts.push(v.clone().multiplyScalar(this.R * (1.015 + h)));
    }
    return pts;
  }

  private registrarEventos() {
    const canvas = this.canvas3dRef.nativeElement;

    // Mouse para desktop / browser preview
    canvas.addEventListener('mousedown', this.boundMouseDown);
    window.addEventListener('mousemove', this.boundMouseMove);
    window.addEventListener('mouseup', this.boundMouseUp);

    // Touch para dispositivos móviles
    canvas.addEventListener('touchstart', this.boundTouchStart, { passive: true });
    window.addEventListener('touchmove', this.boundTouchMove, { passive: true });
    window.addEventListener('touchend', this.boundTouchEnd);

    // Zoom con rueda de mouse
    canvas.addEventListener('wheel', this.boundWheel, { passive: false });
    window.addEventListener('resize', this.boundResize);
  }

  private onMouseDown(e: MouseEvent) {
    this.isDrag = true;
    this.isAutoCentering = false;
    this.pausarGiroAutomatico();
    this.dragStartX = e.clientX;
    this.dragStartY = e.clientY;
    this.prevX = e.clientX;
    this.prevY = e.clientY;
    this.velX = 0;
    this.velY = 0;
  }

  private onTouchStart(e: TouchEvent) {
    this.pausarGiroAutomatico();
    if (e.touches.length === 1) {
      this.isDrag = true;
      this.isAutoCentering = false;
      this.dragStartX = e.touches[0].clientX;
      this.dragStartY = e.touches[0].clientY;
      this.prevX = e.touches[0].clientX;
      this.prevY = e.touches[0].clientY;
      this.velX = 0;
      this.velY = 0;
    } else if (e.touches.length === 2) {
      this.isDrag = false;
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      this.initialPinchDist = Math.hypot(dx, dy);
      this.initialPinchZoom = this.targetZoom;
    }
  }

  private onWheel(e: WheelEvent) {
    e.preventDefault();
    this.pausarGiroAutomatico();
    this.targetZoom = Math.max(1.3, Math.min(4.4, this.targetZoom + e.deltaY * 0.0025));
  }

  private onMouseMove(e: MouseEvent) {
    if (!this.isDrag) return;
    this.pausarGiroAutomatico();
    const k = 0.0055 * Math.min(1, (this.zoom - 1) / 1.5);
    this.velY = (e.clientX - this.prevX) * k;
    this.velX = (e.clientY - this.prevY) * k;
    this.rotY += this.velY;
    this.rotX = Math.max(-1.4, Math.min(1.4, this.rotX + this.velX));
    this.prevX = e.clientX;
    this.prevY = e.clientY;
  }

  private onMouseUp() {
    if (this.isDrag) {
      this.pausarGiroAutomatico();
      const dx = Math.abs(this.prevX - this.dragStartX);
      const dy = Math.abs(this.prevY - this.dragStartY);
      if (dx < 6 && dy < 6) {
        this.detectarPaisTap(this.prevX, this.prevY);
      }
    }
    this.isDrag = false;
  }

  private onTouchMove(e: TouchEvent) {
    if (e.touches.length === 1 && this.isDrag) {
      this.pausarGiroAutomatico();
      const k = 0.0055 * Math.min(1, (this.zoom - 1) / 1.5);
      this.velY = (e.touches[0].clientX - this.prevX) * k;
      this.velX = (e.touches[0].clientY - this.prevY) * k;
      this.rotY += this.velY;
      this.rotX = Math.max(-1.4, Math.min(1.4, this.rotX + this.velX));
      this.prevX = e.touches[0].clientX;
      this.prevY = e.touches[0].clientY;
    } else if (e.touches.length === 2 && this.initialPinchDist > 0) {
      this.pausarGiroAutomatico();
      const dx = e.touches[0].clientX - e.touches[1].clientX;
      const dy = e.touches[0].clientY - e.touches[1].clientY;
      const currentDist = Math.hypot(dx, dy);
      const scale = this.initialPinchDist / Math.max(1, currentDist);
      this.targetZoom = Math.max(1.3, Math.min(4.4, this.initialPinchZoom * scale));
    }
  }

  private onTouchEnd(e: TouchEvent) {
    if (this.isDrag) {
      this.pausarGiroAutomatico();
      const dx = Math.abs(this.prevX - this.dragStartX);
      const dy = Math.abs(this.prevY - this.dragStartY);
      if (dx < 6 && dy < 6) {
        // Tap simple: identificar país tocado
        this.detectarPaisTap(this.prevX, this.prevY);
      }
    }
    this.isDrag = false;
    this.initialPinchDist = 0;
  }

  private detectarPaisTap(clientX: number, clientY: number) {
    if (!this.camera || !this.globeMesh) return;
    const canvas = this.canvas3dRef.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const x = ((clientX - rect.left) / rect.width) * 2 - 1;
    const y = -((clientY - rect.top) / rect.height) * 2 + 1;

    const raycaster = new THREE.Raycaster();
    raycaster.setFromCamera(new THREE.Vector2(x, y), this.camera);
    const hit = raycaster.intersectObject(this.globeMesh)[0];

    if (hit) {
      const p = this.globeMesh.worldToLocal(hit.point.clone()).normalize();
      const lat = 90 - (Math.acos(Math.max(-1, Math.min(1, p.y))) * 180) / Math.PI;
      let lon = (Math.atan2(p.z, -p.x) * 180) / Math.PI - 180;
      if (lon < -180) lon += 360;

      const f = this.findCountry(lon, lat);
      this.setHighlight(f);
      if (f) {
        const nombre = f.properties?.name || 'País';
        const latStr = `${Math.abs(lat).toFixed(2)}°${lat >= 0 ? 'N' : 'S'}`;
        const lonStr = `${Math.abs(lon).toFixed(2)}°${lon >= 0 ? 'E' : 'O'}`;

        this.ngZone.run(() => {
          this.tooltipNombre = nombre;
          this.tooltipCoords = `${latStr} · ${lonStr}`;
          this.tooltipX = Math.min(window.innerWidth - 170, Math.max(12, clientX - 80));
          this.tooltipY = Math.max(70, clientY - 60);
          this.tooltipVisible = true;

          clearTimeout(this.tooltipTimeout);
          this.tooltipTimeout = setTimeout(() => {
            this.tooltipVisible = false;
          }, 3500);
        });
        return;
      }
    }

    this.setHighlight(null);
    this.ngZone.run(() => {
      this.tooltipVisible = false;
    });
  }

  private setHighlight(f: any) {
    if (!this.bordersGroup) return;
    const id = f ? (f.id ?? f.properties?.name) : null;
    if (id === this.hlId) return;
    this.hlId = id;
    if (this.hlLine) {
      this.bordersGroup.remove(this.hlLine);
      this.hlLine.geometry.dispose();
      this.hlLine = null;
    }
    if (!f?.geometry) return;
    const rings = f.geometry.type === 'Polygon' ? f.geometry.coordinates : f.geometry.coordinates.flat();
    this.hlLine = new THREE.LineSegments(
      this.linesToGeometry(rings, this.R * 1.0024),
      new THREE.LineBasicMaterial({
        color: 0xffd27a,
        transparent: true,
        opacity: 1.0,
        depthWrite: false
      })
    );
    this.bordersGroup.add(this.hlLine);
  }

  private findCountry(lon: number, lat: number) {
    for (const { f, b } of this.countryFeatures) {
      if (lat < b[0][1] || lat > b[1][1]) continue;
      const inLon =
        b[0][0] <= b[1][0]
          ? lon >= b[0][0] && lon <= b[1][0]
          : lon >= b[0][0] || lon <= b[1][0];
      if (!inLon) continue;
      if (geoContains(f, [lon, lat])) return f;
    }
    return null;
  }

  private onResize() {
    if (!this.containerRef || !this.camera || !this.renderer) return;
    const container = this.containerRef.nativeElement;
    const W = container.clientWidth || window.innerWidth;
    const H = container.clientHeight || window.innerHeight;

    this.camera.aspect = W / H;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(W, H);

    if (this.canvasLabelsRef) {
      const lc = this.canvasLabelsRef.nativeElement;
      lc.width = W;
      lc.height = H;
    }
  }

  private iniciarAnimacion() {
    const loop = (t: number) => {
      this.animId = requestAnimationFrame(loop);
      this.renderFrame(t);
    };
    this.animId = requestAnimationFrame(loop);
  }

  private renderFrame(t: number) {
    if (!this.renderer || !this.scene || !this.camera) return;

    const dt = Math.min((t - this.lastTime) / 1000, 0.05);
    this.lastTime = t;
    this.pulsePhase += dt * 2.2;

    // Inercia o auto-centrado
    if (this.isAutoCentering) {
      this.rotX += (this.targetRotX - this.rotX) * 0.08;
      this.rotY += (this.targetRotY - this.rotY) * 0.08;
      if (Math.abs(this.targetRotX - this.rotX) < 0.001 && Math.abs(this.targetRotY - this.rotY) < 0.001) {
        this.isAutoCentering = false;
      }
    } else if (!this.isDrag) {
      this.velY *= 0.85;
      this.velX *= 0.85;
      if (Math.abs(this.velY) < 0.0002 && Math.abs(this.velX) < 0.0002) {
        this.velY = 0;
        this.velX = 0;
        // Reanudar giro automático solo después de 30 segundos sin interacción
        if (performance.now() >= this.pauseAutoRotateUntil) {
          this.rotY += 0.0005;
        }
      } else {
        this.rotY += this.velY;
        this.rotX = Math.max(-1.4, Math.min(1.4, this.rotX + this.velX));
      }
    }

    // Suavizado del zoom
    this.zoom += (this.targetZoom - this.zoom) * 0.12;
    this.camera.position.z = this.zoom;

    // Aplicar rotaciones
    const rotGroups = [this.globeMesh, this.markersGroup, this.routeGroup, this.bordersGroup];
    for (const g of rotGroups) {
      if (g) {
        g.rotation.x = this.rotX;
        g.rotation.y = this.rotY;
      }
    }

    // Pulsación de anillos
    if (this.markersGroup) {
      for (const obj of this.markersGroup.children) {
        const u = obj.userData as Record<string, any>;
        if (u['isPulse']) {
          const phase = this.pulsePhase + (u['pOff'] || 0);
          const mat = (obj as THREE.Mesh).material as THREE.MeshBasicMaterial;
          mat.opacity = 0.2 + 0.35 * Math.sin(phase);
          const s = 1.0 + 0.16 * Math.sin(phase * 0.8);
          obj.scale.set(s, s, s);
        }
      }
    }

    // Animación de aviones por la curva
    for (const pl of this.planeObjects) {
      const u = pl.userData as Record<string, any>;
      u['t'] += u['speed'];
      if (u['t'] > 1) u['t'] = 0;
      if (u['t'] < 0) u['t'] = 1;
      pl.position.copy(u['curve'].getPoint(u['t']));
    }

    // Render WebGL
    this.renderer.render(this.scene, this.camera);

    // Dibujar etiquetas 2D en canvas de superposición
    this.dibujarEtiquetas2D();
  }

  private dibujarEtiquetas2D() {
    if (!this.canvasLabelsRef || !this.camera || !this.ruta) return;
    const canvas = this.canvasLabelsRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const W2 = canvas.width;
    const H2 = canvas.height;
    const camDir = new THREE.Vector3(0, 0, 1);

    for (const apt of this.ruta.paradas) {
      // Posición 3D del aeropuerto
      const worldPos = this.latLonToVec3(apt.latitud, apt.longitud, this.R);
      const v = worldPos.clone();
      v.applyEuler(new THREE.Euler(this.rotX, this.rotY, 0, 'XYZ'));

      // Ocultar etiquetas en la parte posterior de la Tierra (Backface culling)
      if (v.dot(camDir) < 0.08) continue;

      // Proyectar a pantalla
      const projected = v.clone().project(this.camera);
      const sx = (projected.x * 0.5 + 0.5) * W2;
      const sy = (-projected.y * 0.5 + 0.5) * H2;

      const dotR = apt.tipo === 'escala' ? 0.016 : 0.022;
      const dotPx = dotR * W2 * 0.18 * (2.6 / this.zoom);
      const offsetX = dotPx + 8;
      const offsetY = 3;

      ctx.save();
      ctx.shadowColor = 'rgba(0, 0, 0, 0.9)';
      ctx.shadowBlur = 6;

      // Código IATA
      ctx.font = 'bold 12px "Space Mono", monospace, system-ui';
      ctx.fillStyle = apt.colorHex;
      ctx.fillText(apt.codigoIata, sx + offsetX, sy + offsetY);

      // Ciudad / Nombre
      ctx.font = '500 10px system-ui, -apple-system, sans-serif';
      ctx.fillStyle = 'rgba(255, 255, 255, 0.85)';
      ctx.fillText(apt.ciudad || apt.nombreAeropuerto, sx + offsetX, sy + offsetY + 13);

      ctx.restore();
    }
  }

  private destruirVisualizador() {
    if (this.animId !== null) {
      cancelAnimationFrame(this.animId);
      this.animId = null;
    }

    clearTimeout(this.tooltipTimeout);

    if (this.canvas3dRef?.nativeElement) {
      const c = this.canvas3dRef.nativeElement;
      c.removeEventListener('mousedown', this.boundMouseDown);
      c.removeEventListener('touchstart', this.boundTouchStart);
      c.removeEventListener('wheel', this.boundWheel);
    }

    window.removeEventListener('resize', this.boundResize);
    window.removeEventListener('touchmove', this.boundTouchMove);
    window.removeEventListener('touchend', this.boundTouchEnd);
    window.removeEventListener('mousemove', this.boundMouseMove);
    window.removeEventListener('mouseup', this.boundMouseUp);

    // Disponer Three.js
    if (this.scene) {
      this.scene.traverse((obj: any) => {
        if (obj.geometry) obj.geometry.dispose();
        if (obj.material) {
          if (Array.isArray(obj.material)) {
            obj.material.forEach((m: any) => m.dispose());
          } else {
            obj.material.dispose();
          }
        }
      });
      this.scene.clear();
      this.scene = null;
    }

    if (this.renderer) {
      this.renderer.dispose();
      this.renderer.forceContextLoss();
      const gl = this.renderer.domElement.getContext('webgl2') || this.renderer.domElement.getContext('webgl');
      if (gl) {
        const loseExt = gl.getExtension('WEBGL_lose_context');
        if (loseExt) loseExt.loseContext();
      }
      this.renderer = null;
    }

    this.planeObjects = [];
    this.countryFeatures = [];
    this.globeMesh = null;
    this.markersGroup = null;
    this.routeGroup = null;
    this.bordersGroup = null;
    this.camera = null;
  }
}
