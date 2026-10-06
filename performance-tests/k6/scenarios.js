/**
 * Pruebas de rendimiento — Posgrados API
 * k6 v2.2.0
 *
 * 7 escenarios secuenciales.  Cada uno empieza cuando termina el anterior
 * (startTime escalonado).  Ninguno toca AWS SES, S3 ni Wompi.
 *
 * Umbrales por defecto (sin NFR documentados):
 *   p(95) < 3 000 ms  |  error_rate < 1 %  |  checks > 99 %
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

// ── Configuración ─────────────────────────────────────────────────────────────
const BASE  = 'http://localhost:8080/posgrados-project';
const HDR   = { 'Content-Type': 'application/json' };
// Cohorte 1 existe y tiene aspirantes sembrados
const COHORTE_ID = 1;

// ── Duraciones de escenario ───────────────────────────────────────────────────
// Cada escenario: 30 s rampa, 60 s meseta, 30 s bajada = 120 s → buffer 3 min
const STAGES = [
  { duration: '30s', target: 10 },
  { duration: '60s', target: 50 },
  { duration: '30s', target: 0  },
];

// ── Opciones y escenarios ─────────────────────────────────────────────────────
export const options = {
  scenarios: {
    s01_login: {
      exec:      'scenarioLogin',
      executor:  'ramping-vus',
      startTime: '0s',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s02_catalogo_public: {
      exec:      'scenarioCatalogoPublic',
      executor:  'ramping-vus',
      startTime: '3m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s03_cohortes_listall: {
      exec:      'scenarioCohorteListall',
      executor:  'ramping-vus',
      startTime: '6m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s04_programas_listall: {
      exec:      'scenarioProgramasListall',
      executor:  'ramping-vus',
      startTime: '9m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s05_aspirantes_validar: {
      exec:      'scenarioAspirantesValidar',
      executor:  'ramping-vus',
      startTime: '12m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s06_cohortes_director: {
      exec:      'scenarioCohorteDirector',
      executor:  'ramping-vus',
      startTime: '15m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
    s07_ranking: {
      exec:      'scenarioRanking',
      executor:  'ramping-vus',
      startTime: '18m',
      stages:    STAGES,
      gracefulRampDown: '10s',
    },
  },

  // Umbrales por escenario (tag automático de k6)
  thresholds: {
    // S01 — login
    'http_req_duration{scenario:s01_login}':    ['p(95)<3000'],
    'http_req_failed{scenario:s01_login}':      ['rate<0.01'],
    'checks{scenario:s01_login}':               ['rate>0.99'],
    // S02 — catálogo público
    'http_req_duration{scenario:s02_catalogo_public}':  ['p(95)<3000'],
    'http_req_failed{scenario:s02_catalogo_public}':    ['rate<0.01'],
    'checks{scenario:s02_catalogo_public}':             ['rate>0.99'],
    // S03 — cohortes listall
    'http_req_duration{scenario:s03_cohortes_listall}': ['p(95)<3000'],
    'http_req_failed{scenario:s03_cohortes_listall}':   ['rate<0.01'],
    'checks{scenario:s03_cohortes_listall}':            ['rate>0.99'],
    // S04 — programas listall
    'http_req_duration{scenario:s04_programas_listall}':['p(95)<3000'],
    'http_req_failed{scenario:s04_programas_listall}':  ['rate<0.01'],
    'checks{scenario:s04_programas_listall}':           ['rate>0.99'],
    // S05 — aspirantes a validar
    'http_req_duration{scenario:s05_aspirantes_validar}':['p(95)<3000'],
    'http_req_failed{scenario:s05_aspirantes_validar}':  ['rate<0.01'],
    'checks{scenario:s05_aspirantes_validar}':           ['rate>0.99'],
    // S06 — cohortes director
    'http_req_duration{scenario:s06_cohortes_director}': ['p(95)<3000'],
    'http_req_failed{scenario:s06_cohortes_director}':   ['rate<0.01'],
    'checks{scenario:s06_cohortes_director}':            ['rate>0.99'],
    // S07 — ranking
    'http_req_duration{scenario:s07_ranking}':  ['p(95)<3000'],
    'http_req_failed{scenario:s07_ranking}':    ['rate<0.01'],
    'checks{scenario:s07_ranking}':             ['rate>0.99'],
  },
};

// ── Setup: obtiene tokens para los tres roles ─────────────────────────────────
function doLogin(username, password, requestedRole) {
  const res = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ username, password, requestedRole }),
    { headers: HDR }
  );
  if (res.status !== 200) {
    console.error(`[setup] Login falló para ${username}: HTTP ${res.status} — ${res.body}`);
    return null;
  }
  const body = JSON.parse(res.body);
  return body.accessToken || null;
}

export function setup() {
  const superadminToken = doLogin('superadmin',  'admin123',      'SUPER_ADMINISTRADOR');
  const directorToken   = doLogin('director1',   'director123',   'DIRECTOR_DE_PROGRAMA');
  const posgradosToken  = doLogin('posgrados1',  'posgrados123',  'POSGRADOS');

  if (!superadminToken) throw new Error('No se pudo obtener token de superadmin en setup()');
  if (!directorToken)   throw new Error('No se pudo obtener token de director en setup()');
  if (!posgradosToken)  throw new Error('No se pudo obtener token de posgrados en setup()');

  return { superadminToken, directorToken, posgradosToken };
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function authHdr(token) {
  return { headers: { ...HDR, Authorization: `Bearer ${token}` } };
}

// ── S01: Login ────────────────────────────────────────────────────────────────
// Mide throughput y latencia del endpoint de autenticación.
// No reutiliza token: cada petición ES un login nuevo.
export function scenarioLogin() {
  const res = http.post(
    `${BASE}/auth/login`,
    JSON.stringify({ username: 'aspirante01', password: 'aspirante123', requestedRole: 'ASPIRANTE' }),
    { headers: HDR }
  );
  check(res, {
    'login: status 200':         (r) => r.status === 200,
    'login: tiene accessToken':  (r) => {
      try { return !!JSON.parse(r.body).accessToken; } catch { return false; }
    },
  });
  sleep(0.5);
}

// ── S02: Catálogo público de inscripción ─────────────────────────────────────
// Simula el frontend cargando los datos del formulario de inscripción.
// No requiere autenticación.
export function scenarioCatalogoPublic() {
  const endpoints = [
    `${BASE}/api/application/case/inscripciones/programas`,
    `${BASE}/api/application/case/inscripciones/generos`,
    `${BASE}/api/application/case/inscripciones/tipos-documento`,
    `${BASE}/api/application/case/inscripciones/estados-civiles`,
  ];
  const url = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(url);
  check(res, {
    'catalogo-public: status 200':   (r) => r.status === 200,
    'catalogo-public: body es array': (r) => {
      try { return Array.isArray(JSON.parse(r.body)); } catch { return false; }
    },
  });
  sleep(0.3);
}

// ── S03: Cohortes listall (POSGRADOS) ─────────────────────────────────────────
// Endpoint sin paginación — riesgo si el volumen de cohortes crece.
export function scenarioCohorteListall(data) {
  const res = http.get(
    `${BASE}/api/dev/endpoint/cohortes/listall`,
    authHdr(data.posgradosToken)
  );
  check(res, {
    'cohortes-listall: status 200':      (r) => r.status === 200,
    'cohortes-listall: body es array':   (r) => {
      try { return Array.isArray(JSON.parse(r.body)); } catch { return false; }
    },
    'cohortes-listall: al menos 1 item': (r) => {
      try { return JSON.parse(r.body).length >= 1; } catch { return false; }
    },
  });
  sleep(0.3);
}

// ── S04: Programas listall (POSGRADOS) ────────────────────────────────────────
export function scenarioProgramasListall(data) {
  const res = http.get(
    `${BASE}/api/dev/endpoint/programa/listall`,
    authHdr(data.posgradosToken)
  );
  check(res, {
    'programas-listall: status 200':    (r) => r.status === 200,
    'programas-listall: body es array': (r) => {
      try { return Array.isArray(JSON.parse(r.body)); } catch { return false; }
    },
    'programas-listall: al menos 1':    (r) => {
      try { return JSON.parse(r.body).length >= 1; } catch { return false; }
    },
  });
  sleep(0.3);
}

// ── S05: Aspirantes a validar por cohorte (DIRECTOR) ─────────────────────────
// Listado sin paginación: 400 aspirantes por cohorte en el seed.
// Escenario de mayor riesgo de N+1 y memoria.
export function scenarioAspirantesValidar(data) {
  const res = http.get(
    `${BASE}/api/application/case/director-programa/cohorte/${COHORTE_ID}/aspirantes-a-validar`,
    authHdr(data.directorToken)
  );
  check(res, {
    'asp-validar: status 200':    (r) => r.status === 200,
    'asp-validar: body es array': (r) => {
      try { return Array.isArray(JSON.parse(r.body)); } catch { return false; }
    },
  });
  sleep(0.5);
}

// ── S06: Cohortes del director (DIRECTOR) ─────────────────────────────────────
// Deriva el programa del usuario autenticado vía administrativo → cargo.
export function scenarioCohorteDirector(data) {
  const res = http.get(
    `${BASE}/api/application/case/director-programa/cohortes`,
    authHdr(data.directorToken)
  );
  check(res, {
    'cohortes-director: status 200':      (r) => r.status === 200,
    'cohortes-director: body es array':   (r) => {
      try { return Array.isArray(JSON.parse(r.body)); } catch { return false; }
    },
  });
  sleep(0.3);
}

// ── S07: Ranking de admitidos por cohorte (DIRECTOR) ─────────────────────────
// Consulta de ranking; puede retornar lista vacía si no hay admitidos confirmados.
export function scenarioRanking(data) {
  const res = http.get(
    `${BASE}/api/application/case/director-programa/cohorte/${COHORTE_ID}/admitidos/ranking`,
    authHdr(data.directorToken)
  );
  check(res, {
    'ranking: status 200':    (r) => r.status === 200,
    'ranking: body válido':   (r) => {
      try { JSON.parse(r.body); return true; } catch { return false; }
    },
  });
  sleep(0.3);
}

// ── handleSummary: escribe resultados en disco ────────────────────────────────
// Usa el resumen nativo de k6 en stdout y guarda el JSON completo en disco.
export function handleSummary(data) {
  return {
    'performance-tests/resultados/ronda1-all.json': JSON.stringify(data, null, 2),
  };
}
