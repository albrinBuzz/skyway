(function () {
    "use strict";

    var baseCtx = window.SKYWAY_TELEMETRY_BASE || "";
    var stompClient = null;
    var telemetryEnabled = false;
    var liveChart = null;
    var trafficChart = null;
    var threadsChart = null;
    var httpCodesChart = null;
    var historyLoaded = false;

    function byId(id) {
        return document.getElementById(id);
    }

    function setText(id, value) {
        var el = byId(id);
        if (el) {
            el.textContent = value;
        }
    }

    function escapeHtml(texto) {
        if (texto === null || texto === undefined) return "";
        return String(texto)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function formatNumber(n) {
        return Number(n || 0).toLocaleString("es-CL");
    }

    function formatCurrency(n) {
        return "$" + Number(n || 0).toLocaleString("es-CL", { maximumFractionDigits: 0 });
    }

    function formatUptime(millis) {
        var seconds = Math.floor((millis || 0) / 1000);
        var minutes = Math.floor(seconds / 60);
        var hours = Math.floor(minutes / 60);
        var days = Math.floor(hours / 24);
        return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m " + (seconds % 60) + "s";
    }

    function initTabs() {
        var buttons = document.querySelectorAll(".tel-nav-tab");
        var sections = document.querySelectorAll(".tel-tab-section");

        function activarTab(tabId) {
            buttons.forEach(function (btn) {
                btn.classList.toggle("active", btn.getAttribute("data-tab") === tabId);
            });
            sections.forEach(function (sec) {
                var secId = sec.getAttribute("data-section");
                var visible = tabId === "all" || secId === tabId;
                sec.style.display = visible ? "block" : "none";
            });
            if (liveChart) liveChart.resize();
            if (trafficChart) trafficChart.resize();
            if (threadsChart) threadsChart.resize();
            if (httpCodesChart) httpCodesChart.resize();
        }

        buttons.forEach(function (btn) {
            btn.addEventListener("click", function () {
                activarTab(btn.getAttribute("data-tab"));
            });
        });

        activarTab("overview");
    }

    function initCharts() {
        var c1 = byId("liveChart");
        var c2 = byId("trafficChart");
        var c3 = byId("threadsChart");
        var c4 = byId("httpCodesChart");
        if (!c1 || !c2 || !c3 || !c4) return;

        liveChart = new Chart(c1.getContext("2d"), {
            type: "line",
            data: {
                labels: [],
                datasets: [
                    { label: "RAM Proceso JVM (MB)", data: [], borderColor: "#60a5fa", backgroundColor: "rgba(96, 165, 250, 0.14)", fill: true, tension: 0.25 },
                    { label: "Latencia SQL (ms)", data: [], borderColor: "#f59e0b", borderDash: [4, 4], fill: false, tension: 0.25 },
                    { label: "CPU Proceso (%)", data: [], borderColor: "#4ade80", borderDash: [2, 2], fill: false, tension: 0.25 }
                ]
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, grid: { color: "#19253a" }, ticks: { color: "#94a3b8" } },
                    x: { grid: { display: false }, ticks: { color: "#94a3b8", maxTicksLimit: 8 } }
                },
                plugins: { legend: { labels: { color: "#cbd5e1", font: { size: 11 } } } }
            }
        });

        trafficChart = new Chart(c2.getContext("2d"), {
            type: "line",
            data: {
                labels: [],
                datasets: [
                    { label: "Heap Usado (MB)", data: [], borderColor: "#38bdf8", backgroundColor: "rgba(56, 189, 248, 0.12)", fill: true, tension: 0.25 },
                    { label: "Heap Committed (MB)", data: [], borderColor: "#818cf8", borderDash: [5, 5], fill: false, tension: 0.25 },
                    { label: "Tráfico HTTP (req/min)", data: [], borderColor: "#22c55e", fill: false, tension: 0.25 },
                    { label: "Latencia HTTP Media (ms)", data: [], borderColor: "#f472b6", borderDash: [2, 2], fill: false, tension: 0.25 }
                ]
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, grid: { color: "#19253a" }, ticks: { color: "#94a3b8" } },
                    x: { grid: { display: false }, ticks: { color: "#94a3b8", maxTicksLimit: 8 } }
                },
                plugins: { legend: { labels: { color: "#cbd5e1", font: { size: 11 } } } }
            }
        });

        threadsChart = new Chart(c3.getContext("2d"), {
            type: "bar",
            data: {
                labels: ["Runnable", "Waiting", "Timed Waiting", "Blocked", "Daemon"],
                datasets: [{
                    label: "Hilos",
                    data: [0, 0, 0, 0, 0],
                    backgroundColor: ["#22c55e", "#f59e0b", "#38bdf8", "#ef4444", "#818cf8"],
                    borderRadius: 6
                }]
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, grid: { color: "#19253a" }, ticks: { color: "#94a3b8" } },
                    x: { grid: { display: false }, ticks: { color: "#cbd5e1" } }
                },
                plugins: { legend: { display: false } }
            }
        });

        httpCodesChart = new Chart(c4.getContext("2d"), {
            type: "doughnut",
            data: {
                labels: ["2xx OK", "3xx Redirect", "4xx Cliente", "5xx Servidor"],
                datasets: [{
                    data: [1, 0, 0, 0],
                    backgroundColor: ["#22c55e", "#38bdf8", "#f59e0b", "#ef4444"],
                    borderColor: "#10192b",
                    borderWidth: 2
                }]
            },
            options: {
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: "right", labels: { color: "#cbd5e1", font: { size: 11 } } } }
            }
        });
    }

    function precargarHistorial(history) {
        if (historyLoaded || !Array.isArray(history) || history.length === 0 || !liveChart || !trafficChart) {
            return false;
        }
        history.forEach(function (pt) {
            var lbl = pt.ts ? new Date(pt.ts).toLocaleTimeString() : "-";
            liveChart.data.labels.push(lbl);
            liveChart.data.datasets[0].data.push(pt.ram_mb || 0);
            liveChart.data.datasets[1].data.push(pt.sql_ms || 0);
            liveChart.data.datasets[2].data.push(pt.cpu_pct || 0);

            trafficChart.data.labels.push(lbl);
            trafficChart.data.datasets[0].data.push(pt.heap_used_mb || 0);
            trafficChart.data.datasets[1].data.push(pt.heap_total_mb || 0);
            trafficChart.data.datasets[2].data.push(pt.http_rpm || 0);
            trafficChart.data.datasets[3].data.push(pt.http_avg_ms || 0);
        });
        liveChart.update();
        trafficChart.update();
        historyLoaded = true;
        return true;
    }

    function renderAlerts(alerts) {
        var container = byId("alertsContainer");
        if (!container) return;
        container.innerHTML = "";
        if (!alerts || alerts.length === 0) return;

        alerts.forEach(function (al) {
            var div = document.createElement("div");
            var cls = al.level === "danger"
                ? "tel-alert tel-alert-danger"
                : (al.level === "warning" ? "tel-alert tel-alert-warning" : "tel-alert tel-alert-ok");
            var icon = al.level === "ok" ? "pi-check-circle" : "pi-exclamation-triangle";
            div.className = cls;
            div.innerHTML = '<i class="pi ' + icon + '"></i><span>' + escapeHtml(al.message) + "</span>";
            container.appendChild(div);
        });
    }

    function renderMemoryPools(pools) {
        var container = byId("memoryPoolsContainer");
        if (!container) return;
        container.innerHTML = "";
        if (!pools || pools.length === 0) return;

        pools.forEach(function (pool) {
            var pct = Math.min(100, Math.max(0, Number(pool.usage_pct || 0)));
            var maxTxt = pool.max_mb > 0 ? pool.max_mb + " MB" : "Sin límite";
            var barColor = pct > 85 ? "#ef4444" : (pct > 65 ? "#f59e0b" : "#3b82f6");

            var row = document.createElement("div");
            row.className = "mb-2";
            row.innerHTML =
                '<div class="d-flex justify-content-between small mb-1">' +
                '<span class="text-light fw-semibold">' + escapeHtml(pool.name) + ' <small class="text-secondary">(' + escapeHtml(pool.type) + ")</small></span>" +
                '<span class="text-info">' + pool.used_mb + " / " + maxTxt + (pool.max_mb > 0 ? " (" + pct + "%)" : "") + "</span>" +
                "</div>" +
                '<div class="progress-custom" style="height:6px;"><div style="width:' + pct + "%;height:100%;background:" + barColor + ';"></div></div>';
            container.appendChild(row);
        });
    }

    function renderGcCollectors(collectors) {
        var container = byId("gcCollectorsContainer");
        if (!container) return;
        container.innerHTML = "";
        if (!collectors || collectors.length === 0) {
            container.innerHTML = '<small class="text-secondary">Sin colectores reportados</small>';
            return;
        }
        collectors.forEach(function (gc) {
            var row = document.createElement("div");
            row.className = "detail-item";
            row.innerHTML = "<span>" + escapeHtml(gc.name) + '</span><strong class="text-light">' + gc.count + " ev · " + gc.time_ms + " ms</strong>";
            container.appendChild(row);
        });
    }

    function renderTopThreads(topThreads) {
        var tbody = byId("topThreadsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!topThreads || topThreads.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-secondary">Sin hilos reportados</td></tr>';
            return;
        }
        topThreads.forEach(function (t) {
            var cls = t.state === "RUNNABLE" ? "bg-success" : (t.state === "BLOCKED" ? "bg-danger" : "bg-secondary");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-info">' + escapeHtml(t.id) + "</td>" +
                '<td class="text-light text-truncate" style="max-width:320px;" title="' + escapeHtml(t.name) + '">' + escapeHtml(t.name) + "</td>" +
                '<td><span class="badge ' + cls + '">' + escapeHtml(t.state) + "</span></td>" +
                '<td class="text-secondary">' + (t.daemon ? "Daemon" : "Usuario") + "</td>" +
                '<td class="text-end text-warning fw-semibold">' + formatNumber(t.cpu_ms) + " ms</td>";
            tbody.appendChild(tr);
        });
    }

    function renderTopTables(tables) {
        var tbody = byId("pgTopTablesBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!tables || tables.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-secondary">Sin estadísticas disponibles</td></tr>';
            return;
        }
        tables.forEach(function (t) {
            var deadCls = (t.dead_tup || 0) > 1000 ? "text-warning" : "text-secondary";
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-light fw-semibold">' + escapeHtml(t.table) + "</td>" +
                '<td class="text-end text-info">' + formatNumber(t.rows_est) + "</td>" +
                '<td class="text-end ' + deadCls + '">' + formatNumber(t.dead_tup || 0) + "</td>" +
                '<td class="text-end text-secondary">' + formatNumber(t.idx_scans || 0) + " / " + formatNumber(t.seq_scans || 0) + "</td>" +
                '<td class="text-end text-warning">' + Number(t.size_mb || 0).toFixed(2) + " MB</td>";
            tbody.appendChild(tr);
        });
    }

    function renderNetworkInterfaces(nics) {
        var tbody = byId("networkInterfacesBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!nics || nics.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-secondary">Sin interfaces externas activas</td></tr>';
            return;
        }
        nics.forEach(function (n) {
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-light fw-semibold">' + escapeHtml(n.name) + ' <small class="text-secondary">(' + escapeHtml(n.display_name) + ")</small></td>" +
                '<td class="text-info">' + escapeHtml(n.ipv4 || "-") + "</td>" +
                '<td class="text-secondary text-truncate" style="max-width:140px;">' + escapeHtml(n.ipv6 || "-") + "</td>" +
                '<td class="text-secondary">' + escapeHtml(n.mac || "-") + "</td>" +
                '<td class="text-end text-light">' + escapeHtml(n.mtu || "-") + "</td>";
            tbody.appendChild(tr);
        });
    }

    function renderDiskVolumes(vols) {
        var tbody = byId("diskVolumesBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!vols || vols.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-secondary">Sin volúmenes detectados</td></tr>';
            return;
        }
        vols.forEach(function (v) {
            var cls = (v.usage_pct || 0) > 85 ? "text-danger" : ((v.usage_pct || 0) > 70 ? "text-warning" : "text-info");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-light fw-semibold">' + escapeHtml(v.path) + "</td>" +
                '<td class="text-end text-light">' + v.used_gb + " GB</td>" +
                '<td class="text-end text-success">' + v.free_gb + " GB</td>" +
                '<td class="text-end ' + cls + '">' + v.total_gb + " GB (" + v.usage_pct + "%)</td>";
            tbody.appendChild(tr);
        });
    }

    function renderTopClientIps(ips) {
        var tbody = byId("httpTopIpsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!ips || ips.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-secondary">Sin IPs registradas aún</td></tr>';
            return;
        }
        ips.forEach(function (ip) {
            var devCls = ip.device === "Mobile" ? "bg-info text-dark" : (ip.device === "Bot/API" ? "bg-warning text-dark" : "bg-primary");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-info fw-semibold">' + escapeHtml(ip.ip) + "</td>" +
                '<td><span class="badge ' + devCls + '">' + escapeHtml(ip.device) + "</span></td>" +
                '<td class="text-end text-light">' + formatNumber(ip.hits) + "</td>" +
                '<td class="text-secondary text-truncate" style="max-width:200px;">' + escapeHtml(ip.last_uri) + "</td>";
            tbody.appendChild(tr);
        });
    }

    function renderPgSessions(sessions) {
        var tbody = byId("pgSessionsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!sessions || sessions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-secondary">Sin sesiones activas adicionales</td></tr>';
            return;
        }
        sessions.forEach(function (s) {
            var badge = s.state === "active" ? "bg-success" : (s.state === "idle in transaction" ? "bg-warning text-dark" : "bg-secondary");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-info">' + escapeHtml(s.pid) + "</td>" +
                '<td class="text-light">' + escapeHtml(s.client_ip || "local") + ' <small class="text-secondary">(' + escapeHtml(s.app_name || "-") + ")</small></td>" +
                '<td><span class="badge ' + badge + '">' + escapeHtml(s.state) + "</span></td>" +
                '<td class="text-secondary">' + escapeHtml(s.wait_event) + "</td>" +
                '<td class="text-end text-light">' + formatNumber(s.duration_ms) + " ms</td>" +
                '<td class="text-secondary text-truncate" style="max-width:220px;" title="' + escapeHtml(s.query) + '">' + escapeHtml(s.query) + "</td>";
            tbody.appendChild(tr);
        });
    }

    function renderTopEndpoints(endpoints) {
        var tbody = byId("httpTopEndpointsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!endpoints || endpoints.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-secondary">Sin tráfico HTTP registrado aún</td></tr>';
            return;
        }
        endpoints.forEach(function (ep) {
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-light text-truncate" style="max-width:260px;">' + escapeHtml(ep.endpoint) + "</td>" +
                '<td class="text-end text-info">' + formatNumber(ep.hits) + "</td>" +
                '<td class="text-end text-light">' + ep.avg_ms + " ms</td>" +
                '<td class="text-end text-warning">' + ep.max_ms + " ms</td>" +
                '<td class="text-end ' + (ep.errors > 0 ? "text-danger fw-bold" : "text-secondary") + '">' + ep.errors + "</td>";
            tbody.appendChild(tr);
        });
    }

    function renderRecentErrors(errors) {
        var tbody = byId("httpRecentErrorsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!errors || errors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-success">Sin errores HTTP 4xx/5xx recientes</td></tr>';
            return;
        }
        errors.forEach(function (err) {
            var hora = err.timestamp ? new Date(err.timestamp).toLocaleTimeString() : "-";
            var cls = err.status >= 500 ? "bg-danger" : "bg-warning text-dark";
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-secondary">' + escapeHtml(hora) + "</td>" +
                '<td class="text-info">' + escapeHtml(err.ip || "-") + "</td>" +
                '<td><span class="badge ' + cls + '">' + escapeHtml(err.status) + "</span></td>" +
                '<td class="text-light text-truncate" style="max-width:210px;">' + escapeHtml(err.method + " " + err.uri) + "</td>" +
                '<td class="text-end text-secondary">' + err.latency_ms + " ms</td>";
            tbody.appendChild(tr);
        });
    }

    function renderUpcomingFlights(flights) {
        var tbody = byId("opsUpcomingFlightsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!flights || flights.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-secondary">Sin vuelos programados en las próximas 24h</td></tr>';
            return;
        }
        flights.forEach(function (f) {
            var cls = String(f.status || "").toLowerCase().indexOf("cancel") !== -1
                ? "bg-danger"
                : (String(f.status || "").toLowerCase().indexOf("demor") !== -1 ? "bg-warning text-dark" : "bg-primary");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-info fw-semibold">' + escapeHtml(f.flight_number) + "</td>" +
                '<td class="text-light">' + escapeHtml(f.route) + "</td>" +
                '<td class="text-secondary">' + escapeHtml(f.departure) + "</td>" +
                '<td class="text-light">' + escapeHtml(f.aircraft) + "</td>" +
                '<td><span class="badge ' + cls + '">' + escapeHtml(f.status) + "</span></td>";
            tbody.appendChild(tr);
        });
    }

    function renderPaymentsByMethod(payments) {
        var container = byId("opsPaymentsMethodContainer");
        if (!container) return;
        container.innerHTML = "";
        if (!payments || payments.length === 0) {
            container.textContent = "Sin pagos registrados en últimas 24h";
            return;
        }
        payments.forEach(function (p) {
            var row = document.createElement("div");
            row.className = "detail-item";
            row.innerHTML = "<span>" + escapeHtml(p.method) + " (" + p.count + ' trx)</span><strong class="text-success">' + formatCurrency(p.total) + "</strong>";
            container.appendChild(row);
        });
    }

    function renderAuditEvents(events) {
        var tbody = byId("auditEventsBody");
        if (!tbody) return;
        tbody.innerHTML = "";
        if (!events || events.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-secondary">Sin eventos recientes en Log_Auditoria</td></tr>';
            return;
        }
        events.forEach(function (ev) {
            var cls = ev.nivel === "ERROR" || ev.nivel === "SECURITY"
                ? "bg-danger"
                : (ev.nivel === "WARN" ? "bg-warning text-dark" : "bg-primary");
            var tr = document.createElement("tr");
            tr.innerHTML =
                '<td class="text-secondary">' + escapeHtml(ev.fecha) + "</td>" +
                '<td><span class="badge ' + cls + '">' + escapeHtml(ev.nivel) + "</span></td>" +
                '<td class="text-light fw-semibold">' + escapeHtml(ev.accion) + "</td>" +
                '<td class="text-info">' + escapeHtml(ev.tabla) + "</td>" +
                '<td class="text-light">' + escapeHtml(ev.rut) + "</td>" +
                '<td class="text-secondary">' + escapeHtml(ev.ip) + "</td>";
            tbody.appendChild(tr);
        });
    }

    function actualizarControlesEstado(tel) {
        telemetryEnabled = !!tel.enabled;
        var badge = byId("telemetryModeBadge");
        var btn = byId("btnToggleTelemetry");
        var sel = byId("selIntervalo");

        if (badge) {
            badge.textContent = telemetryEnabled ? ("ACTIVA (" + (tel.interval_sec || 5) + "s)") : "PAUSADA";
            badge.className = telemetryEnabled ? "badge bg-success" : "badge bg-secondary";
        }
        if (btn) {
            btn.textContent = telemetryEnabled ? "Pausar Telemetría en Vivo" : "Activar Telemetría en Vivo";
            btn.className = telemetryEnabled ? "tel-btn-danger" : "tel-btn-primary";
        }
        if (sel && tel.interval_sec) {
            var valStr = String(tel.interval_sec);
            if (sel.querySelector('option[value="' + valStr + '"]')) {
                sel.value = valStr;
            }
        }
        setText("envFlagVal", String(!!tel.env_default_enabled));
        setText("viewersVal", formatNumber(tel.active_viewers || 0));
        setText("ticksVal", formatNumber(tel.ticks_emitted || 0));

        var rem = tel.auto_pause_remaining_sec;
        if (!telemetryEnabled) {
            setText("autoPauseVal", "Inactiva");
        } else if (rem === undefined || rem < 0) {
            setText("autoPauseVal", "Fija por Env");
        } else {
            var min = Math.floor(rem / 60);
            var sec = rem % 60;
            setText("autoPauseVal", min + "m " + (sec < 10 ? "0" : "") + sec + "s");
        }
    }

    function formatCheckinsCanales(ops) {
        var total = formatNumber(ops.checkins_hoy || 0);
        var ch = ops.checkins_by_channel_hoy || {};
        var partes = Object.keys(ch).map(function (k) { return k + ": " + ch[k]; });
        return partes.length > 0 ? total + " (" + partes.join(" · ") + ")" : total;
    }

    function renderPayload(data, agregarPuntoGrafico) {
        if (!data) return;
        var tel = data.telemetry || {};
        var cloud = data.cloud || {};
        var db = data.db || {};
        var gc = data.gc || {};
        var threads = data.threads || {};
        var http = data.http || {};
        var jsf = data.jsf || {};
        var app = data.app || {};
        var ops = data.ops || {};
        var tomcat = data.tomcat || {};
        var wsBroker = data.ws_broker || {};
        var runtime = data.runtime_info || {};

        var cargoHistorialAhora = precargarHistorial(data.history);

        actualizarControlesEstado(tel);
        renderAlerts(data.alerts);

        var timeLabel = data.timestamp ? new Date(data.timestamp).toLocaleTimeString() : "-";
        setText("lastUpdateVal", timeLabel);
        setText("profileHeaderVal", app.active_profile || "default");

        // KPIs superiores
        setText("ramText", (cloud.jvm_process_ram_mb || 0) + " MB");
        setText("ramSubtext", "Heap: " + (cloud.heap_usage_pct || 0) + "% · F1: " + (cloud.azure_f1_pressure_pct || 0) + "%");
        setText("cpuText", (cloud.process_cpu_load || 0) + "%");
        setText("cpuSubtext", "Sistema: " + (cloud.system_cpu_load || 0) + "% · " + (cloud.available_processors || 1) + " vCPU");
        setText("dbText", db.latency_ping_ms >= 0 ? db.latency_ping_ms + " ms" : "ERROR");
        setText("hikariText", "Hikari: " + (db.active_connections || 0) + "/" + (db.total_connections || 0) + " · " + (db.pg_database_size_mb || 0) + " MB");
        setText("pgCacheHitKpi", (db.pg_cache_hit_ratio_pct !== undefined ? db.pg_cache_hit_ratio_pct : 100) + "%");
        setText("pgBackendsKpi", "Backends: " + (db.pg_active_backends || 0) + " act / " + (db.pg_total_backends || 0) + " tot");
        setText("httpRpmKpi", (http.requests_per_sec || 0) + " rps · " + (http.requests_per_min || 0) + " rpm");
        setText("httpSubKpi", "Activas: " + (http.active_requests || 0) + " · Err: " + (http.error_rate_pct || 0) + "%");
        setText("sessionsText", formatNumber(app.active_sessions || 0));
        setText("sessionsSubtext", "Hilos: " + (threads.total || 0) + " · Deadlocks: " + (threads.deadlocked || 0));
        setText("opsFlightsKpi", formatNumber(ops.vuelos_24h || 0));
        setText("opsFlightsSubKpi", "En vuelo ahora: " + formatNumber(ops.vuelos_en_aire || 0));
        setText("opsBookingsKpi", formatNumber(ops.reservas_hoy || 0));
        setText("opsRevenueSubKpi", "Hoy: " + formatCurrency(ops.ingresos_hoy || 0) + " · Check-ins: " + formatNumber(ops.checkins_hoy || 0));

        // Barra F1
        var f1Pct = cloud.azure_f1_pressure_pct || 0;
        setText("f1RamVal", (cloud.jvm_process_ram_mb || 0) + " MB / 1024 MB (" + f1Pct + "%)");
        var f1Bar = byId("f1RamBar");
        if (f1Bar) {
            f1Bar.style.width = Math.min(f1Pct, 100) + "%";
            f1Bar.className = f1Pct > 85 ? "progress-bar bg-danger" : (f1Pct > 70 ? "progress-bar bg-warning" : "progress-bar bg-info");
        }

        // Tab Infra & JVM
        var azure = cloud.azure_infra || {};
        setText("azureRegionVal", (azure.environment_type || "Local") + " · " + (azure.region || "-"));
        setText("azureSkuVal", (azure.site_name || "-") + " (" + (azure.sku || "Local") + ")");
        setText("azureInstanceVal", (azure.public_hostname || "-") + " · " + (azure.instance_id || "-"));
        setText("hostNetVal", (cloud.hostname || "localhost") + " (" + (cloud.host_ip || "127.0.0.1") + ")");
        setText("pidVal", "PID " + (app.pid || "-"));
        setText("osVal", cloud.os_name || "-");
        setText("coresVal", (cloud.available_processors || 1) + " vCPU · Load " + (cloud.system_load_average || 0));
        setText("physicalRamVal", formatNumber(cloud.physical_ram_used_mb) + " / " + formatNumber(cloud.physical_ram_free_mb) + " / " + formatNumber(cloud.physical_ram_total_mb) + " MB");
        setText("swapVal", formatNumber(cloud.swap_used_mb) + " MB / " + formatNumber(cloud.swap_total_mb) + " MB");
        setText("diskVal", (cloud.used_disk_gb || 0) + " GB / " + (cloud.total_disk_gb || 0) + " GB (" + (cloud.disk_usage_pct || 0) + "%)");
        setText("buffersVal", (cloud.direct_buffers_mb || 0) + " MB / " + (cloud.mapped_buffers_mb || 0) + " MB");
        setText("classesVal", formatNumber(cloud.loaded_classes) + " / " + formatNumber(cloud.unloaded_classes));
        renderNetworkInterfaces(cloud.network_interfaces_detail);
        renderDiskVolumes(cloud.disk_volumes);

        setText("heapSummaryVal", (cloud.heap_used_mb || 0) + " / " + (cloud.heap_total_mb || 0) + " / " + (cloud.heap_max_mb || 0) + " MB (" + (cloud.heap_usage_pct || 0) + "%)");
        setText("gcTotalsVal", formatNumber(gc.total_collections) + " ev / " + formatNumber(gc.total_time_ms) + " ms");
        renderMemoryPools(data.memory_pools);
        renderGcCollectors(gc.collectors);

        setText("threadsVal", (threads.total || 0) + " / " + (threads.peak || 0));
        setText("daemonThreadsVal", (threads.daemon || 0) + " / " + formatNumber(threads.total_started));
        setText("threadStatesSummaryVal", (threads.runnable || 0) + " / " + (threads.waiting || 0) + " / " + (threads.timed_waiting || 0) + " / " + (threads.blocked || 0));
        var deadlocksEl = byId("deadlocksVal");
        if (deadlocksEl) {
            var dl = threads.deadlocked || 0;
            deadlocksEl.textContent = dl;
            deadlocksEl.className = dl > 0 ? "text-danger fw-bold" : "text-success fw-bold";
        }
        setText("tomcatThreadsVal", tomcat.max_threads !== undefined ? ((tomcat.threads_busy || 0) + " / " + (tomcat.current_threads || 0) + " / " + tomcat.max_threads) : "N/A");
        setText("tomcatBytesVal", tomcat.bytes_received_mb !== undefined ? (tomcat.bytes_received_mb + " MB / " + tomcat.bytes_sent_mb + " MB") : "N/A");
        setText("tomcatReqErrVal", tomcat.request_count !== undefined ? (formatNumber(tomcat.request_count) + " / " + formatNumber(tomcat.error_count)) : "N/A");
        setText("runtimeTzVal", (runtime.timezone || "-") + " · " + (runtime.charset || "UTF-8"));
        setText("versionsVal", "Boot " + (app.spring_boot_version || "-") + " · Java " + (runtime.java_runtime_version || app.java_version || "-"));
        setText("springUptimeVal", formatNumber(app.spring_beans_count) + " beans · " + formatUptime(app.uptime_ms));
        setText("wsSessionStatsVal", wsBroker.session_stats || "N/A");
        setText("wsStompStatsVal", wsBroker.stomp_relay_stats || "N/A");
        setText("jvmFlagsVal", runtime.jvm_flags && runtime.jvm_flags.length > 0 ? runtime.jvm_flags : "Sin argumentos JVM explícitos");
        renderTopThreads(threads.top_threads);

        // Tab PostgreSQL
        setText("hikariPoolNameVal", (db.db_status || "UP") + " · " + (db.pool_name || "HikariPool"));
        setText("pgEndpointHostVal", db.pg_endpoint_host || "-");
        setText("pgServerAddrVal", db.pg_server_addr || "-");
        setText("dbProductVal", (db.db_product_name || "PostgreSQL") + " " + (db.db_product_version || ""));
        setText("dbCatalogVal", "PostGIS " + (db.postgis_version || "N/A") + " · " + (db.catalog || "aerolinea_db"));
        setText("pgStartEncVal", (db.pg_start_time || "-") + " · " + (db.pg_encoding || "UTF8"));
        setText("pgMemParamsVal", (db.pg_shared_buffers || "-") + " / " + (db.pg_work_mem || "-"));
        setText("dbDriverVal", (db.driver_name || "JDBC") + " (" + (db.driver_version || "") + ")");
        setText("pgSizeVal", (db.pg_database_size_mb || 0) + " MB");
        setText("pgCacheHitVal", (db.pg_cache_hit_ratio_pct !== undefined ? db.pg_cache_hit_ratio_pct : 100) + "% / " + (db.pg_idx_scan_pct !== undefined ? db.pg_idx_scan_pct : 100) + "%");
        setText("pgBackendsVal", (db.pg_active_backends || 0) + " act / " + (db.pg_total_backends || 0) + " tot (máx " + (db.pg_max_connections || "?") + ", idle-tx " + (db.pg_idle_in_tx || 0) + ")");
        setText("pgLocksVal", formatNumber(db.pg_granted_locks) + " / " + formatNumber(db.pg_waiting_locks));
        setText("pgTempFilesVal", formatNumber(db.pg_temp_files) + " / " + (db.pg_temp_mb || 0) + " MB");
        setText("pgDeadTuplesVal", formatNumber(db.pg_total_dead_tuples));
        setText("pgTxVal", formatNumber(db.pg_xact_commit) + " / " + formatNumber(db.pg_xact_rollback) + " / " + formatNumber(db.pg_deadlocks));
        setText("pgTuplesIoVal", formatNumber(db.pg_tup_returned) + " / " + formatNumber(db.pg_tup_fetched) + " / " + formatNumber(db.pg_tup_inserted) + " / " + formatNumber(db.pg_tup_updated) + " / " + formatNumber(db.pg_tup_deleted));
        setText("hikariDetailVal", (db.active_connections || 0) + " / " + (db.idle_connections || 0) + " / " + (db.max_pool_size || 0) + " / " + (db.threads_awaiting || 0));
        setText("dbAcquireVal", (db.acquire_time_ms >= 0 ? db.acquire_time_ms : 0) + " ms / " + (db.max_acquire_time_ms || 0) + " ms");
        setText("totalQueriesVal", (db.avg_ping_ms || 0) + " ms / " + formatNumber(db.total_queries_executed) + " / " + formatNumber(db.total_connection_failures));
        renderTopTables(db.top_tables);
        renderPgSessions(db.active_sessions_detail);

        // Tab HTTP & JSF
        setText("httpTotalsVal", formatNumber(http.total_requests) + " / " + formatNumber(http.active_requests));
        setText("httpRateVal", (http.requests_per_sec || 0) + " req/s · " + formatNumber(http.requests_per_min) + " req/m");
        setText("httpLatencyVal", (http.last_latency_ms || 0) + " / " + (http.avg_latency_ms || 0) + " / " + (http.max_latency_ms || 0) + " ms");
        setText("httpCodesVal", formatNumber(http.status_2xx) + " / " + formatNumber(http.status_3xx) + " / " + formatNumber(http.status_4xx) + " / " + formatNumber(http.status_5xx));
        setText("httpErrorRateVal", (http.error_rate_pct || 0) + "%");
        setText("httpLastUriVal", (http.last_method || "-") + " " + (http.last_uri || "-"));
        setText("httpClientIpVal", (http.last_client_ip || "-") + " · " + formatNumber(http.unique_client_ips) + " únicas");
        setText("httpXffVal", http.last_forwarded_for || "-");
        setText("httpHostProtoVal", (http.last_host_header || "-") + " · " + (http.last_scheme_proto || "-"));
        setText("httpDevicesVal", formatNumber(http.device_desktop_hits) + " / " + formatNumber(http.device_mobile_hits) + " / " + formatNumber(http.device_api_hits));
        setText("httpReqKbVal", (http.total_request_kb || 0) + " KB");
        setText("jsfViewVal", (jsf.last_view_id || "-") + " (" + (jsf.last_component_count || 0) + " nodos)");
        setText("jsfRenderVal", (jsf.last_render_time_ms || 0) + " / " + (jsf.avg_render_time_ms || 0) + " / " + (jsf.max_render_time_ms || 0) + " ms");
        setText("jsfTotalReqVal", formatNumber(jsf.total_jsf_requests) + " / " + formatNumber(jsf.total_postbacks));
        setText("sessionsDetailVal", formatNumber(app.active_sessions) + " / " + formatNumber(app.total_created_sessions));
        renderTopClientIps(http.top_client_ips);
        renderTopEndpoints(http.top_endpoints);
        renderRecentErrors(http.recent_errors);

        // Tab Operación SkyWay
        setText("opsVuelosSummaryVal", formatNumber(ops.vuelos_24h) + " / " + formatNumber(ops.vuelos_en_aire));
        setText("opsItinerariosVal", formatNumber(ops.itinerarios_24h) + " / " + formatNumber(ops.directos_24h));
        setText("opsInfraRedVal", formatNumber(ops.aeropuertos_activos) + " aeropuertos / " + formatNumber(ops.aviones_activos) + " aviones");
        setText("opsReservasVal", formatNumber(ops.reservas_hoy) + " / " + formatNumber(ops.reservas_24h));
        setText("opsIngresosVal", formatCurrency(ops.ingresos_hoy) + " / " + formatCurrency(ops.ingresos_24h));
        setText("opsReembolsosVal", formatNumber(ops.reembolsos_pendientes) + " / " + formatCurrency(ops.monto_reembolsos_pendientes));
        setText("opsCheckinsHoyVal", formatCheckinsCanales(ops));
        setText("opsEquipajeHoyVal", formatNumber(ops.equipajes_hoy) + " piezas / " + Number(ops.peso_equipaje_hoy_kg || 0).toFixed(1) + " kg");
        setText("opsSecurityVal", formatNumber(ops.usuarios_hoy) + " / " + formatNumber(ops.tokens_recuperacion_activos));
        setText("opsNotifVal", formatNumber(ops.notificaciones_hoy) + " / " + formatNumber(ops.notificaciones_pendientes));
        setText("opsCacheSeatsVal", formatNumber(ops.cached_flight_seatmaps) + " vuelos / " + formatNumber(ops.active_seat_locks) + " asientos");
        setText("opsAudit24hVal", formatNumber(ops.auditoria_24h));
        renderPaymentsByMethod(ops.payments_by_method_24h);
        renderUpcomingFlights(ops.upcoming_flights);
        renderAuditEvents(ops.recent_audit_events);

        // Gráficos (solo agrega punto nuevo si no acaba de precargarse el historial completo en esta llamada)
        if (!cargoHistorialAhora && agregarPuntoGrafico && liveChart && trafficChart) {
            if (liveChart.data.labels.length >= 30) {
                liveChart.data.labels.shift();
                liveChart.data.datasets.forEach(function (ds) { ds.data.shift(); });
            }
            liveChart.data.labels.push(timeLabel);
            liveChart.data.datasets[0].data.push(cloud.jvm_process_ram_mb || 0);
            liveChart.data.datasets[1].data.push(db.latency_ping_ms >= 0 ? db.latency_ping_ms : 0);
            liveChart.data.datasets[2].data.push(cloud.process_cpu_load || 0);
            liveChart.update();

            if (trafficChart.data.labels.length >= 30) {
                trafficChart.data.labels.shift();
                trafficChart.data.datasets.forEach(function (ds) { ds.data.shift(); });
            }
            trafficChart.data.labels.push(timeLabel);
            trafficChart.data.datasets[0].data.push(cloud.heap_used_mb || 0);
            trafficChart.data.datasets[1].data.push(cloud.heap_total_mb || 0);
            trafficChart.data.datasets[2].data.push(http.requests_per_min || 0);
            trafficChart.data.datasets[3].data.push(http.avg_latency_ms || 0);
            trafficChart.update();
        }

        if (threadsChart) {
            threadsChart.data.datasets[0].data = [
                threads.runnable || 0,
                threads.waiting || 0,
                threads.timed_waiting || 0,
                threads.blocked || 0,
                threads.daemon || 0
            ];
            threadsChart.update();
        }

        if (httpCodesChart) {
            httpCodesChart.data.datasets[0].data = [
                http.status_2xx || 0,
                http.status_3xx || 0,
                http.status_4xx || 0,
                http.status_5xx || 0
            ];
            httpCodesChart.update();
        }
    }

    function renderErrorAlert(message) {
        renderAlerts([{ level: "danger", message: message }]);
    }

    function fetchJson(url, method) {
        var controller = typeof AbortController !== "undefined" ? new AbortController() : null;
        var timer = controller ? setTimeout(function () { controller.abort(); }, 12000) : null;
        return fetch(url, {
            method: method || "GET",
            credentials: "same-origin",
            headers: { "Accept": "application/json" },
            signal: controller ? controller.signal : undefined
        }).then(function (resp) {
            if (!resp.ok) throw new Error("HTTP " + resp.status);
            return resp.json();
        }).finally(function () {
            if (timer) clearTimeout(timer);
        });
    }

    function cargarSnapshotInicial() {
        fetchJson(baseCtx + "/api/admin/telemetry/snapshot?quick=true", "GET")
            .then(function (data) { renderPayload(data, true); })
            .catch(function (err) {
                var badge = byId("telemetryModeBadge");
                if (badge) {
                    badge.textContent = "ERROR DE CARGA";
                    badge.className = "badge bg-danger";
                }
                renderErrorAlert("No se pudo obtener el estado inicial de telemetría (" + (err && err.message ? err.message : "timeout") + ").");
            });
    }

    function conectarWebSocket() {
        var statusElem = byId("wsStatus");
        if (statusElem) {
            statusElem.textContent = "Conectando WS...";
            statusElem.className = "badge bg-warning text-dark";
        }

        var socket = new SockJS(baseCtx + "/ws-metrics");
        stompClient = Stomp.over(socket);
        stompClient.debug = null;

        stompClient.connect({}, function () {
            if (statusElem) {
                statusElem.textContent = "WebSocket Online";
                statusElem.className = "badge bg-success";
            }
            stompClient.subscribe("/topic/metrics", function (message) {
                try {
                    renderPayload(JSON.parse(message.body), true);
                } catch (e) {}
            });
        }, function () {
            if (statusElem) {
                statusElem.textContent = "Reconectando WS...";
                statusElem.className = "badge bg-danger";
            }
            setTimeout(conectarWebSocket, 4000);
        });
    }

    function enlazarControles() {
        var btnToggle = byId("btnToggleTelemetry");
        var selIntervalo = byId("selIntervalo");
        var btnSnapshot = byId("btnSnapshotNow");
        var btnGc = byId("btnTriggerGc");

        if (btnToggle) {
            btnToggle.addEventListener("click", function () {
                var intervalo = selIntervalo ? selIntervalo.value : "5";
                var targetActive = !telemetryEnabled;
                var prevText = btnToggle.textContent;
                btnToggle.disabled = true;
                btnToggle.textContent = targetActive ? "Activando..." : "Pausando...";
                fetchJson(baseCtx + "/api/admin/telemetry/toggle?active=" + targetActive + "&interval=" + encodeURIComponent(intervalo), "POST")
                    .then(function (data) { renderPayload(data, true); })
                    .catch(function (err) {
                        btnToggle.textContent = prevText;
                        renderErrorAlert("Error al cambiar el estado de telemetría: " + (err && err.message ? err.message : "timeout"));
                    })
                    .finally(function () { btnToggle.disabled = false; });
            });
        }

        if (selIntervalo) {
            selIntervalo.addEventListener("change", function () {
                selIntervalo.disabled = true;
                fetchJson(baseCtx + "/api/admin/telemetry/interval?seconds=" + encodeURIComponent(selIntervalo.value), "POST")
                    .then(function (data) { renderPayload(data, true); })
                    .catch(function (err) {
                        renderErrorAlert("Error al cambiar el intervalo: " + (err && err.message ? err.message : "timeout"));
                    })
                    .finally(function () { selIntervalo.disabled = false; });
            });
        }

        if (btnSnapshot) {
            btnSnapshot.addEventListener("click", function () {
                var originalHtml = btnSnapshot.innerHTML;
                btnSnapshot.disabled = true;
                btnSnapshot.textContent = "Capturando...";
                fetchJson(baseCtx + "/api/admin/telemetry/snapshot?quick=false", "GET")
                    .then(function (data) { renderPayload(data, true); })
                    .catch(function (err) {
                        renderErrorAlert("Error al capturar snapshot completo: " + (err && err.message ? err.message : "timeout"));
                    })
                    .finally(function () {
                        btnSnapshot.disabled = false;
                        btnSnapshot.innerHTML = originalHtml;
                    });
            });
        }

        if (btnGc) {
            btnGc.addEventListener("click", function () {
                var originalHtml = btnGc.innerHTML;
                btnGc.disabled = true;
                btnGc.textContent = "Ejecutando GC...";
                fetchJson(baseCtx + "/api/admin/telemetry/gc", "POST")
                    .then(function (data) { renderPayload(data, true); })
                    .catch(function (err) {
                        renderErrorAlert("Error al solicitar Garbage Collector: " + (err && err.message ? err.message : "timeout"));
                    })
                    .finally(function () {
                        btnGc.disabled = false;
                        btnGc.innerHTML = originalHtml;
                    });
            });
        }
    }

    function startMonitor() {
        if (!byId("liveChart")) {
            setTimeout(startMonitor, 100);
            return;
        }
        initTabs();
        initCharts();
        enlazarControles();
        cargarSnapshotInicial();
        conectarWebSocket();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", startMonitor);
    } else {
        startMonitor();
    }
})();
