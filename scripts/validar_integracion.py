from pathlib import Path
import re, sys

ROOT = Path(__file__).resolve().parents[1]
checks = []

def check(name, condition, detail=''):
    checks.append((name, bool(condition), detail))

def text(rel):
    return (ROOT / rel).read_text(encoding='utf-8')

required = [
    'backend/pom.xml',
    'backend/src/main/java/com/SkyWay/mobile/controller/MobileAuthController.java',
    'backend/src/main/java/com/SkyWay/mobile/controller/MobilePassengerController.java',
    'backend/src/main/java/com/SkyWay/mobile/security/MobileSecurityConfig.java',
    'backend/src/main/java/com/SkyWay/mobile/service/MobileAuthService.java',
    'backend/src/main/java/com/SkyWay/mobile/service/MobileReadService.java',
    'backend/src/main/java/com/SkyWay/mobile/service/MobileOperationsService.java',
    'backend/src/main/java/com/SkyWay/modules/reserva/domain/repository/MobileReservaQueries.java',
    'backend/src/main/java/com/SkyWay/modules/reserva/domain/repository/MobileReservaQueriesImpl.java',
    'backend/src/main/resources/application-mobile.properties',
    'mobile/src/app/core/api.service.ts',
    'mobile/src/environments/environment.ts',
    'mobile/proxy.conf.json',
]
for rel in required:
    check(f'Existe {rel}', (ROOT / rel).is_file())

pom = text('backend/pom.xml')
check('OAuth2 Resource Server agregado', 'spring-boot-starter-oauth2-resource-server' in pom)
check('Validation starter agregado', 'spring-boot-starter-validation' in pom)

websec = text('backend/src/main/java/com/SkyWay/config/security/SecurityConfig.java')
mobsec = text('backend/src/main/java/com/SkyWay/mobile/security/MobileSecurityConfig.java')
check('Cadena móvil tiene prioridad 1', '@Order(1)' in mobsec)
check('Cadena web tiene prioridad 2', '@Order(2)' in websec)
check('Cadena móvil limitada a /api/v1/mobile/**', '/api/v1/mobile/**' in mobsec)
check('API móvil stateless', 'SessionCreationPolicy.STATELESS' in mobsec)
check('Login móvil público y resto protegido', '/api/v1/mobile/auth/login' in mobsec and 'SCOPE_mobile' in mobsec)

repo = text('backend/src/main/java/com/SkyWay/modules/reserva/domain/repository/ReservaRepository.java')
check('ReservaRepository integra fragmento móvil', 'MobileReservaQueries' in repo)
notif = text('backend/src/main/java/com/SkyWay/modules/notificacion/domain/repository/NotificacionRepository.java')
check('Repositorio de notificaciones soporta móvil', 'findByUsuario_RutOrderByFechaDescIdNotificacionDesc' in notif)

env = text('mobile/src/environments/environment.ts')
api = text('mobile/src/app/core/api.service.ts')
controller = text('backend/src/main/java/com/SkyWay/mobile/controller/MobilePassengerController.java')
check('Ionic apunta a /api/v1/mobile', "apiUrl: '/api/v1/mobile'" in env)
for endpoint in ['/inicio', '/reservas', '/notificaciones']:
    check(f'Contrato contiene {endpoint}', endpoint in api and endpoint in controller)

# Comprobación simple de paquetes/rutas para los Java nuevos.
for p in (ROOT/'backend/src/main/java/com/SkyWay/mobile').rglob('*.java'):
    first = p.read_text(encoding='utf-8')
    m = re.search(r'^package\s+([\w.]+);', first, re.M)
    expected = '.'.join(p.relative_to(ROOT/'backend/src/main/java').parent.parts)
    check(f'Package coherente: {p.name}', bool(m and m.group(1) == expected), f'esperado={expected}')

failed = [c for c in checks if not c[1]]
for name, ok, detail in checks:
    print(('OK   ' if ok else 'FAIL ') + name + (f' ({detail})' if detail else ''))
print(f'\nResultado: {len(checks)-len(failed)}/{len(checks)} comprobaciones OK.')
sys.exit(1 if failed else 0)
