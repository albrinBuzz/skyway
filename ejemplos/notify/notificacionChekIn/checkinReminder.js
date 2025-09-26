// checkinReminder.js
const { Client } = require('pg');
const nodemailer = require('nodemailer');

const client = new Client({
  user: 'aerolinea_user',
  host: 'localhost',
  database: 'aerolinea_db',
  password: 'aerolinea_user',
  port: 5432,
});

const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 465,
  secure: true,
  auth: {
    user: 'tucorreo@gmail.com',
    pass: 'tupasswordapp',
  },
});

async function enviarRecordatoriosCheckin() {
  await client.connect();

  const res = await client.query(`
    SELECT DISTINCT r.rut_pasajero, u.correo_electronico, v.numero_vuelo, v.fecha_hora_salida, r.id_reserva
    FROM reserva r
    JOIN reserva_itinerario ri ON ri.id_reserva = r.id_reserva
    JOIN itinerario i ON i.id_itinerario = ri.id_itinerario
    JOIN itinerario_vuelo iv ON iv.id_itinerario = i.id_itinerario
    JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
    JOIN usuario u ON u.rut = r.rut_pasajero
    WHERE v.fecha_hora_salida BETWEEN NOW() + INTERVAL '23 hours' AND NOW() + INTERVAL '25 hours'
    AND NOT EXISTS (
      SELECT 1 FROM notificacion n
      WHERE n.rut_destinatario = r.rut_pasajero AND n.titulo = 'Recordatorio de Check-in'
      AND DATE(n.fecha) = CURRENT_DATE
    );
  `);

  for (const row of res.rows) {
    const msg = `
Hola, te recordamos hacer el check-in para tu vuelo ${row.numero_vuelo}.
Salida: ${new Date(row.fecha_hora_salida).toLocaleString()}.

Gracias por volar con nosotros.
    `;

    await transporter.sendMail({
      from: '"Aerolinea" <tucorreo@gmail.com>',
      to: row.correo_electronico,
      subject: 'Recordatorio de Check-in',
      text: msg,
    });

    await client.query(`
      INSERT INTO notificacion (rut_destinatario, titulo, mensaje, fecha, leido, canal, enviada)
      VALUES ($1, $2, $3, NOW(), FALSE, 'Email', TRUE)
    `, [row.rut_pasajero, 'Recordatorio de Check-in', msg]);

    console.log(`✅ Enviado a ${row.correo_electronico}`);
  }

  await client.end();
}

enviarRecordatoriosCheckin();
