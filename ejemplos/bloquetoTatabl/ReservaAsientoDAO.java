import java.sql.*;
import java.time.Duration;
import java.time.Instant;

public class ReservaAsientoDAO {

    private static final String URL = "jdbc:postgresql://localhost:5432/tu_base";
    private static final String USER = "tu_usuario";
    private static final String PASS = "tu_password";

    // Intenta bloquear un asiento (reserva temporal)
    public boolean bloquearAsiento(int idVuelo, int idAsiento, int pasajeroId) throws SQLException {
        String sqlInsert = """
            INSERT INTO reserva_asiento (id_vuelo, id_asiento, estado, ts_bloqueo, pasajero_id)
            SELECT ?, ?, 'bloqueado', NOW(), ?
            WHERE NOT EXISTS (
                SELECT 1 FROM reserva_asiento
                WHERE id_vuelo = ?
                  AND id_asiento = ?
                  AND estado IN ('bloqueado', 'confirmado')
                  AND ts_bloqueo > NOW() - INTERVAL '5 minutes'
            )
            """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {

            conn.setAutoCommit(false);

            stmt.setInt(1, idVuelo);
            stmt.setInt(2, idAsiento);
            stmt.setInt(3, pasajeroId);
            stmt.setInt(4, idVuelo);
            stmt.setInt(5, idAsiento);

            int affected = stmt.executeUpdate();

            if (affected == 1) {
                conn.commit();
                System.out.println("Asiento bloqueado exitosamente.");
                return true;
            } else {
                conn.rollback();
                System.out.println("No se pudo bloquear el asiento (ya está bloqueado).");
                return false;
            }
        }
    }

    // Confirmar reserva (pasa de 'bloqueado' a 'confirmado')
    public boolean confirmarReserva(int idVuelo, int idAsiento, int pasajeroId) throws SQLException {
        String sqlUpdate = """
            UPDATE reserva_asiento
            SET estado = 'confirmado'
            WHERE id_vuelo = ? AND id_asiento = ? AND pasajero_id = ? AND estado = 'bloqueado'
            """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {

            conn.setAutoCommit(false);

            stmt.setInt(1, idVuelo);
            stmt.setInt(2, idAsiento);
            stmt.setInt(3, pasajeroId);

            int updated = stmt.executeUpdate();

            if (updated == 1) {
                conn.commit();
                System.out.println("Reserva confirmada.");
                return true;
            } else {
                conn.rollback();
                System.out.println("No se pudo confirmar la reserva.");
                return false;
            }
        }
    }

    // Liberar bloqueos expirados (que llevan más de 5 minutos en 'bloqueado')
    public void liberarBloqueosExpirados() throws SQLException {
        String sqlUpdate = """
            UPDATE reserva_asiento
            SET estado = 'liberado'
            WHERE estado = 'bloqueado' AND ts_bloqueo <= NOW() - INTERVAL '5 minutes'
            """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {

            int liberados = stmt.executeUpdate();
            System.out.println("Bloqueos expirados liberados: " + liberados);
        }
    }

    public static void main(String[] args) throws SQLException {
        ReservaAsientoDAO dao = new ReservaAsientoDAO();

        int vuelo = 1;
        int asiento = 42;
        int pasajero = 123;

        // Intentar bloquear asiento
        boolean bloqueado = dao.bloquearAsiento(vuelo, asiento, pasajero);

        if (bloqueado) {
            // Simular que pasajero confirma reserva a los 3 minutos (antes de 5)
            System.out.println("Esperando confirmación...");
            try { Thread.sleep(180000); } catch (InterruptedException ignored) {}

            dao.confirmarReserva(vuelo, asiento, pasajero);
        } else {
            System.out.println("Asiento ya está bloqueado por otro pasajero.");
        }

        // Liberar bloqueos expirados (puede ejecutarse periódicamente)
        dao.liberarBloqueosExpirados();
    }
}
