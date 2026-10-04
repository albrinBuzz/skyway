package com.SkyWay.modules.reserva.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

/** Fragmento incorporado a ReservaRepository; solo SQL parametrizado y tablas V3. */
public interface MobileReservaQueries {
    List<Object[]> mobileReservas(String rut, boolean activas, LocalDateTime ahora, int limit, int offset);
    long mobileCountReservas(String rut, boolean activas, LocalDateTime ahora);
    Object[] mobileReserva(String rut, int id, boolean lock);
    List<Object[]> mobileItinerarios(int id);
    List<Object[]> mobileConexiones(int id);
    List<Object[]> mobileSegmentos(int id);
    List<Object[]> mobileCaracteristicas(int id);
    List<Object[]> mobileEquipajes(int id, String rut);
    List<Object[]> mobileAsignados(int id, String rut);
    List<Object[]> mobileCheckins(int id);
    List<Object[]> mobileMapa(int vuelo, int reserva, String rut);
    List<Object[]> mobileNext(String rut, LocalDateTime ahora);
    void mobileLockVuelo(int id);
    int mobileCambiarAsiento(int idAsignacion, int reserva, int vuelo, String rut, int asiento);
    void mobileInsertCheckin(int reserva, LocalDateTime ahora);
    List<Object[]> mobileFaltanAsientos(int reserva, LocalDateTime ahora);
}
