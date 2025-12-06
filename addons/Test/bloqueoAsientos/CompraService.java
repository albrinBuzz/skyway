@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepo;

    @Autowired
    private CompraArticuloRepository compraArticuloRepo;

    @Autowired
    private ArticuloRepository articuloRepo;

    @Autowired
    private JobScheduler scheduler;

    @Transactional
    public Compra iniciarCompra(Long usuarioId, List<Long> articuloIds) {

        // Crear compra
        Compra compra = new Compra();
        compra.setUsuarioId(usuarioId);
        compra = compraRepo.save(compra);

        // Registrar artículos
        for (Long id : articuloIds) {
            CompraArticulo ca = new CompraArticulo();
            ca.setCompra(compra);
            ca.setArticulo(new Articulo());
            ca.getArticulo().setId(id);
            compraArticuloRepo.save(ca);
        }

        // Bloquear artículos mediante SP
        articuloRepo.bloquearArticulosSP(compra.getId());

        // PROGRAMAR LIBERACIÓN EN 10 MINUTOS
        scheduler.programarLiberacion(compra.getId());

        return compra;
    }

    @Transactional
    public void marcarCompraPagada(Long compraId) {
        compraRepo.marcarPagadaSP(compraId);

        // cancelar job asociado
        scheduler.cancelarJob(compraId);
    }


    @Transactional
    public void verificarYLiberar(Long compraId) {

        Compra compra = compraRepo.findById(compraId).orElseThrow();

        if (!compra.isPagada()) {
            articuloRepo.liberarArticulosSP(compraId);
        }
    }
}
