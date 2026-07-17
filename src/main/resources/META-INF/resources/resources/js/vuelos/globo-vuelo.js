// Se ejecuta UNA vez, cuando carga la página (fuera del sidebar)
async function initGlobo() {
    // ... todo el bootstrap de three.js, texturas, océano, atmósfera ...
    // en vez de hardcodear AIRPORTS, deja los grupos vacíos:
    window.markersGroup = new THREE.Group(); scene.add(markersGroup);
    window.routeGroup = new THREE.Group(); scene.add(routeGroup);
    window.planeObjects = [];
    window._globoReady = true;
    // guarda referencias globales: scene, camera, renderer, latLonToVec3, greatCirclePoints, R
}

function rowExpansion(dataTable) {
    var $this = dataTable;
    $this.tbody.children('tr').css('cursor', 'pointer')
    $this.tbody.off('click.datatable-expansion', '> tr')
        .on('click.datatable-expansion', '> tr', null, function() {
            if (typeof $this.collapseAllRows === 'function') {
                $this.collapseAllRows();
            }
            $this.toggleExpansion($(this).find('div.ui-row-toggler'));
        });
}

// Se ejecuta CADA VEZ que el usuario abre "Ver paradas"
function actualizarRutaGlobo(aeropuertos) {
    if (!window._globoReady) {
        // si el globo aún no cargó, reintenta cuando esté listo
        window.addEventListener('globo-listo', () => actualizarRutaGlobo(aeropuertos), {once:true});
        return;
    }
    // limpiar marcadores y rutas anteriores
    while (markersGroup.children.length) markersGroup.remove(markersGroup.children[0]);
    while (routeGroup.children.length) routeGroup.remove(routeGroup.children[0]);
    planeObjects.length = 0;

    const colorPorTipo = { origen: 0xff4d4d, escala: 0xffd440, destino: 0x3ddc84 };

    aeropuertos.forEach(apt => {
        const color = colorPorTipo[apt.tipo] || 0xffd440;
        const pos = latLonToVec3(apt.latitud, apt.longitud, R);
        const dotR = apt.tipo === 'escala' ? 0.017 : 0.022;
        const dot = new THREE.Mesh(new THREE.SphereGeometry(dotR,20,20), new THREE.MeshBasicMaterial({color}));
        dot.position.copy(pos);
        markersGroup.add(dot);
        // (rings, spike -> igual que tu mock, usando `color` en vez del hardcode)
    });

    // rutas: un segmento por cada par consecutivo, N-1 segmentos para N aeropuertos
    for (let i = 0; i < aeropuertos.length - 1; i++) {
        const fr = aeropuertos[i], to = aeropuertos[i+1];
        const pts = greatCirclePoints(fr.latitud, fr.longitud, to.latitud, to.longitud, 200, 0.2);
        const curve = new THREE.CatmullRomCurve3(pts);
        routeGroup.add(new THREE.Mesh(new THREE.TubeGeometry(curve,200,0.0048,8,false),
            new THREE.MeshBasicMaterial({color:0xff9020, transparent:true, opacity:0.92})));
        // (glow tubes y avión animado igual que tu mock)
    }
}