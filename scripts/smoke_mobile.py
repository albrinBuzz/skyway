"""Pruebas HTTP con escritura, exclusivamente para los datos sintéticos de 03_demo.sql.
Uso: python scripts/smoke_mobile.py --demo [--base http://localhost:8080]
Requiere una demostración recién inicializada. No usar contra datos reales.
"""
import argparse
import concurrent.futures
import json
import urllib.request
import urllib.error

parser = argparse.ArgumentParser()
parser.add_argument('--base', default='http://localhost:8080')
parser.add_argument('--demo', action='store_true', help='Confirma que se usa la base sintética de demostración')
parser.add_argument('--report', help='Archivo JSON de resultados')
args = parser.parse_args()
if not args.demo:
    parser.error('Se requiere --demo: estas pruebas cambian asientos, check-in y notificaciones.')
base = args.base.rstrip('/') + '/api/v1/mobile'
opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
results = []

def request(path, method='GET', token=None, body=None, headers=None):
    h = {'Content-Type': 'application/json', **(headers or {})}
    if token:
        h['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(base + path, method=method, headers=h,
                                 data=None if body is None else json.dumps(body).encode())
    try:
        with opener.open(req, timeout=30) as response:
            raw = response.read().decode()
            return response.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as ex:
        raw = ex.read().decode()
        try: data = json.loads(raw)
        except ValueError: data = raw
        return ex.code, data

def check(name, condition):
    results.append({'prueba': name, 'ok': bool(condition)})
    print(('OK  ' if condition else 'FAIL') + ' ' + name, flush=True)
    if not condition:
        raise AssertionError(name)

def expect(name, code, path, **kw):
    actual, data = request(path, **kw)
    check(name, actual == code)
    return data

def login(email):
    data = expect('Login ' + email, 200, '/auth/login', method='POST',
                  body={'correoElectronico': email, 'contrasena': 'SkyWayDemo2026!'})
    check('No se serializa contraseña', 'contrasena' not in json.dumps(data).lower())
    return data['accessToken']

try:
    expect('Sin JWT devuelve JSON 401', 401, '/reservas')
    expect('Login inválido 401', 401, '/auth/login', method='POST', body={'correoElectronico':'pasajero@skyway.demo','contrasena':'incorrecta'})
    expect('Usuario sin fila Pasajero rechazado',401,'/auth/login',method='POST',body={'correoElectronico':'personal@skyway.demo','contrasena':'SkyWayDemo2026!'})
    expect('Validación del correo',400,'/auth/login',method='POST',body={'correoElectronico':'no-es-correo','contrasena':'algo'})
    token = login('pasajero@skyway.demo')
    other = login('camila@skyway.demo')
    parts = token.split('.'); parts[2] = ('A' if parts[2][0] != 'A' else 'B') + parts[2][1:]
    expect('Firma JWT alterada',401,'/auth/me',token='.'.join(parts))
    expect('Perfil del pasajero',200,'/auth/me',token=token)
    home = expect('Inicio y próximo vuelo',200,'/inicio',token=token)
    check('Próximo vuelo real',home['proximoVuelo']['vuelo']['idVuelo']==1)
    active = expect('Reservas activas',200,'/reservas?grupo=activas',token=token)
    history = expect('Historial',200,'/reservas?grupo=historial',token=token)
    check('Activas e historial disjuntos',set(x['idReserva'] for x in active['contenido']).isdisjoint(x['idReserva'] for x in history['contenido']))
    check('Reserva finalizada y cancelada en historial',{3,4}.issubset(x['idReserva'] for x in history['contenido']))
    expect('Grupo inválido',400,'/reservas?grupo=cualquiera',token=token)
    expect('Límite de paginación',400,'/reservas?tamano=101',token=token)
    expect('Reserva ajena oculta',404,'/reservas/2',token=token)
    expect('Reserva inexistente',404,'/reservas/999999',token=token)
    detail = expect('Detalle de reserva',200,'/reservas/1',token=token)
    check('Demo disponible sin check-in previo',detail['checkin'] is None)
    check('Dos vuelos de conexión',len(detail['itinerarios'][0]['vuelos'])==2)
    bags = expect('Equipaje del pasajero',200,'/reservas/1/equipajes',token=token)
    check('Equipaje de acompañante no expuesto',len(bags)==1 and float(bags[0]['peso'])==18.5)
    member = expect('Acompañante ve reserva compartida',200,'/reservas/1',token=other)
    check('Acompañante no es titular',not member['titular'])
    expect('Vuelo fuera de la reserva',404,'/reservas/1/vuelos/999',token=token)
    seatmap = expect('Mapa de asientos V3',200,'/reservas/1/vuelos/1/asientos',token=token)
    check('Solo asiento propio seleccionado',[s['idAsiento'] for s in seatmap['asientos'] if s['estado']=='seleccionado']==[1])
    check('Asiento del acompañante ocupado',next(s for s in seatmap['asientos'] if s['idAsiento']==2)['estado']=='ocupado')
    def change(t, reservation, seat):
        return request(f'/reservas/{reservation}/vuelos/1/asiento',method='PUT',token=t,body={'idAsiento':seat})
    check('Asiento ocupado rechazado',change(token,1,2)[0]==409)
    check('Asiento de otro avión rechazado',change(token,1,100)[0]==404)
    check('Cambio de clase rechazado',change(token,1,25)[0]==409)
    check('Tarifa Básica no permite cambio',change(token,5,8)[0]==409)
    check('Cambio válido',change(token,1,4)[0]==200)
    second = expect('Segundo vuelo conserva su asiento',200,'/reservas/1/vuelos/2',token=token)
    check('Cambio acotado al vuelo',second['asiento']['idAsiento']==1)
    check('PUT repetido idempotente',change(token,1,4)[0]==200)
    check('Otra reserva no ocupa asiento tomado',change(other,2,4)[0]==409)
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        a=pool.submit(change,token,1,5); b=pool.submit(change,other,2,5)
        statuses=sorted([a.result()[0],b.result()[0]])
    check('Dos solicitudes por el mismo asiento: 200 y 409',statuses==[200,409])
    expect('Pase antes de check-in rechazado',409,'/reservas/1/vuelos/1/pase-abordar',token=token)
    expect('Acompañante no registra check-in de toda la reserva',403,'/reservas/1/checkin',method='POST',body={},token=other)
    expect('Check-in reserva cancelada rechazado',409,'/reservas/4/checkin',method='POST',body={},token=token)
    expect('Check-in vuelo pasado rechazado',409,'/reservas/3/checkin',method='POST',body={},token=token)
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        futures=[pool.submit(request,'/reservas/1/checkin',method='POST',body={},token=token) for _ in range(2)]
        checks=[f.result() for f in futures]
    check('Check-in simultáneo devuelve un único registro',all(s==200 for s,d in checks) and checks[0][1]['idCheckin']==checks[1][1]['idCheckin'])
    again=expect('Check-in repetido',200,'/reservas/1/checkin',method='POST',body={},token=token)
    check('ID de check-in estable',again['idCheckin']==checks[0][1]['idCheckin'])
    check('Cambio después del check-in rechazado',change(token,1,6)[0]==409)
    boarding=expect('Pase de abordar',200,'/reservas/1/vuelos/1/pase-abordar',token=token)
    check('QR identificado como simulación',boarding['simulacion'] and boarding['qrContenido'].startswith('SKYWAY:SIMULACION:'))
    expect('Pase propio del acompañante',200,'/reservas/1/vuelos/1/pase-abordar',token=other)
    expect('Notificaciones propias',200,'/notificaciones',token=token)
    expect('Notificación ajena protegida',404,'/notificaciones/3/leida',method='PATCH',body={},token=token)
    note=expect('Marcar leída',200,'/notificaciones/1/leida',method='PATCH',body={},token=token)
    check('Estado leída persistido',note['leido'])
    expect('Marcar leída es idempotente',200,'/notificaciones/1/leida',method='PATCH',body={},token=token)
    expect('CORS origen no autorizado',403,'/auth/login',method='OPTIONS',headers={'Origin':'https://no-autorizado.example','Access-Control-Request-Method':'POST'})
    expect('CORS Ionic autorizado',200,'/auth/login',method='OPTIONS',headers={'Origin':'http://localhost:8100','Access-Control-Request-Method':'POST'})
    print(f'\n{len(results)} comprobaciones satisfactorias.',flush=True)
finally:
    if args.report:
        with open(args.report,'w',encoding='utf-8') as f:
            json.dump(results,f,indent=2,ensure_ascii=False)
