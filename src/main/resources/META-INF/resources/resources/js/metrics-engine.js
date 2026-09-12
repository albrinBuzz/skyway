document.addEventListener("DOMContentLoaded", function () {
    const ctxRam = document.getElementById('ramChart').getContext('2d');
    const ctxCpu = document.getElementById('cpuChart').getContext('2d');

    const ramChart = new Chart(ctxRam, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                { label: 'Heap Utilizado (MB)', data: [], borderColor: '#38bdf8', backgroundColor: 'rgba(56, 189, 248, 0.1)', fill: true, tension: 0.3 },
                { label: 'Eden Space (MB)', data: [], borderColor: '#a855f7', borderDash: [2, 2], fill: false, tension: 0.3 },
                { label: 'Old Gen (MB)', data: [], borderColor: '#f97316', borderDash: [4, 4], fill: false, tension: 0.3 }
            ]
        },
        options: { responsive: true, maintainAspectRatio: false, animation: false, scales: { y: { beginAtZero: true } } }
    });

    const cpuChart = new Chart(ctxCpu, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                { label: 'CPU App (%)', data: [], borderColor: '#22c55e', backgroundColor: 'rgba(34, 197, 94, 0.05)', fill: true, tension: 0.3 },
                { label: 'CPU Kernel SO (%)', data: [], borderColor: '#eab308', borderDash: [5, 5], fill: false, tension: 0.3 }
            ]
        },
        options: { responsive: true, maintainAspectRatio: false, animation: false, scales: { y: { beginAtZero: true, max: 100 } } }
    });

    function formatUptime(millis) {
        if (!millis) return "0s";
        let seconds = Math.floor(millis / 1000);
        let minutes = Math.floor(seconds / 60);
        let hours = Math.floor(minutes / 60);
        let days = Math.floor(hours / 24);
        return `${days}d ${hours % 24}h ${minutes % 60}m ${seconds % 60}s`;
    }

    const socket = new SockJS('/ws-metrics');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, function () {
        const statusElem = document.getElementById('wsStatus');
        statusElem.innerText = '● Live APM WebSocket Conectado';
        statusElem.className = 'badge bg-success';

        stompClient.subscribe('/topic/metrics', function (message) {
            const dto = JSON.parse(message.body);
            const time = new Date(dto.timestamp).toLocaleTimeString();

            const jvm = dto.jvm;
            const os = dto.os;
            const db = dto.db;
            const jsf = dto.jsfWeb;

            // 1. KPI Top Cards
            document.getElementById('ramText').innerText = jvm.used_heap_mb + ' MB';
            document.getElementById('ramSubtext').innerText = `${jvm.used_heap_mb} MB / ${jvm.max_heap_mb} MB (${jvm.heap_pct}%)`;

            document.getElementById('cpuText').innerText = os.app_cpu + '%';
            document.getElementById('cpuSubtext').innerText = `OS CPU Global: ${os.system_cpu}% | Load Avg: ${os.system_load_avg}`;

            document.getElementById('dbText').innerText = db.latency_ms >= 0 ? db.latency_ms + ' ms' : 'N/A';
            document.getElementById('hikariText').innerText = `Hikari: ${db.active_conns} act / ${db.total_conns} tot (${db.status})`;

            document.getElementById('sessionsText').innerText = jsf.active_sessions;
            document.getElementById('sessionsSubtext').innerText = `Histórico Creadas: ${jsf.total_sessions}`;

            // 2. Modulo JSF / PrimeFaces / Tomcat
            document.getElementById('activeSessVal').innerText = jsf.active_sessions;
            document.getElementById('totalSessVal').innerText = jsf.total_sessions;
            document.getElementById('threadsVal').innerText = jsf.thread_count + ' hilos';
            document.getElementById('peakThreadsVal').innerText = jsf.peak_thread_count + ' hilos';
            document.getElementById('daemonThreadsVal').innerText = jsf.daemon_thread_count + ' hilos';
            document.getElementById('jsfCompVal').innerText = jsf.jsf_components + ' nodos';
            document.getElementById('jsfRenderVal').innerText = jsf.jsf_render_ms + ' ms';
            document.getElementById('pfVerVal').innerText = 'v' + jsf.primefaces_version;
            document.getElementById('omniVerVal').innerText = 'v' + jsf.omnifaces_version;

            // 3. Modulo Memory Generations & GC
            document.getElementById('edenVal').innerText = jvm.eden_mb + ' MB';
            document.getElementById('survivorVal').innerText = jvm.survivor_mb + ' MB';
            document.getElementById('oldGenVal').innerText = jvm.old_gen_mb + ' MB';
            document.getElementById('metaspaceVal').innerText = jvm.metaspace_mb + ' MB';
            document.getElementById('codeCacheVal').innerText = jvm.codecache_mb + ' MB';
            document.getElementById('classesVal').innerText = `${jvm.loaded_classes} (Descargadas: ${jvm.unloaded_classes})`;
            document.getElementById('gcAlgoVal').innerText = jvm.gc_algorithm;
            document.getElementById('gcCountVal').innerText = jvm.gc_count + ' ejecuciones';
            document.getElementById('gcTimeVal').innerText = jvm.gc_time_ms + ' ms total';

            // 4. Modulo Host & DB
            document.getElementById('osRamVal').innerText = `${os.used_ram_mb} MB / ${os.total_ram_mb} MB`;
            document.getElementById('swapVal').innerText = os.free_swap_mb + ' MB Libre';
            document.getElementById('diskVal').innerText = `${os.used_disk_gb} GB / ${os.total_disk_gb} GB`;
            document.getElementById('hikariDetailVal').innerText = `${db.active_conns} / ${db.total_conns}`;
            document.getElementById('idleConnsVal').innerText = db.idle_conns;
            document.getElementById('awaitingConnsVal').innerText = db.awaiting_threads;
            document.getElementById('uptimeVal').innerText = formatUptime(os.uptime_ms);
            document.getElementById('coresVal').innerText = os.processors + ' vCPU';
            document.getElementById('profileBadge').innerText = os.profile;

            // 5. Update Gráficos
            if (ramChart.data.labels.length >= 20) {
                ramChart.data.labels.shift();
                ramChart.data.datasets[0].data.shift();
                ramChart.data.datasets[1].data.shift();
                ramChart.data.datasets[2].data.shift();

                cpuChart.data.labels.shift();
                cpuChart.data.datasets[0].data.shift();
                cpuChart.data.datasets[1].data.shift();
            }

            ramChart.data.labels.push(time);
            ramChart.data.datasets[0].data.push(jvm.used_heap_mb);
            ramChart.data.datasets[1].data.push(jvm.eden_mb);
            ramChart.data.datasets[2].data.push(jvm.old_gen_mb);
            ramChart.update();

            cpuChart.data.labels.push(time);
            cpuChart.data.datasets[0].data.push(os.app_cpu);
            cpuChart.data.datasets[1].data.push(os.system_cpu);
            cpuChart.update();
        });
    });
});