import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReservaConcurrenteSimulation {

    private static final int NUM_PASAJEROS = 10; // número de hilos concurrentes

    public static void main(String[] args) {

        ReservaAsientoDAO dao = new ReservaAsientoDAO();
        int vuelo = 1;
        int asiento = 42;

        ExecutorService executor = Executors.newFixedThreadPool(NUM_PASAJEROS);

        for (int i = 1; i <= NUM_PASAJEROS; i++) {
            final int pasajeroId = i;

            executor.submit(() -> {
                try {
                    System.out.println("Pasajero " + pasajeroId + " intenta reservar asiento " + asiento);

                    boolean bloqueado = dao.bloquearAsiento(vuelo, asiento, pasajeroId);

                    if (bloqueado) {
                        System.out.println("Pasajero " + pasajeroId + " bloqueó el asiento.");
                        // Simular confirmación inmediata
                        boolean confirmado = dao.confirmarReserva(vuelo, asiento, pasajeroId);

                        if (confirmado) {
                            System.out.println("Pasajero " + pasajeroId + " confirmó la reserva.");
                        } else {
                            System.out.println("Pasajero " + pasajeroId + " no pudo confirmar la reserva.");
                        }
                    } else {
                        System.out.println("Pasajero " + pasajeroId + " no pudo bloquear el asiento (ya reservado).");
                    }
                } catch (SQLException e) {
                    System.err.println("Error pasajero " + pasajeroId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
    }
}
