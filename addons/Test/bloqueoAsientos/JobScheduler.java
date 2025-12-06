
public enum JobStatus {
    PENDIENTE, EJECUTANDO, CANCELADO, TERMINADO
}

@Component
public class JobScheduler {

    private final ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    public JobScheduler(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    private final Map<Long, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    @Autowired
    private CompraService compraService;

    public void programarLiberacion(Long compraId) {
        if (jobs.containsKey(compraId)) {
            log.warn("Ya existe un job programado para la compra con ID: " + compraId);
            return;
        }

        ScheduledFuture<?> job = taskScheduler.schedule(() -> {
            try {
                compraService.verificarYLiberar(compraId);
            } catch (Exception e) {
                log.error("Error al verificar y liberar compra con ID: " + compraId, e);
            } finally {
                jobs.remove(compraId);
            }
        }, new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));

        jobs.put(compraId, job);
    }


    public void cancelarJob(Long compraId) {
        ScheduledFuture<?> job = jobs.get(compraId);
        if (job != null) {
            job.cancel(false); // NO interrumpir si está corriendo
            jobs.remove(compraId);
        }
    }

    public JobStatus obtenerEstado(Long compraId) {
        return jobStatusMap.getOrDefault(compraId, JobStatus.PENDIENTE);
    }

}
