const { Client } = require('pg');
const nodemailer = require('nodemailer');

// === DATOS DE CONEXIÓN POSTGRES ===
const client = new Client({
  user: 'aerolinea_user',
  host: '192.168.100.99',
  database: 'aerolinea_db',
  password: 'aerolinea_user', // ← tu contraseña aquí si tienes
  port: 5432,
});

// === CONFIGURACIÓN SMTP (GMAIL) ===
const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 465,
  secure: true, // SSL
  auth: {
    user: 'cristobalromanzamora13@gmail.com',
    pass: 'vgeakinstnxqzdkh',
  },
});


// === INICIAR CONEXIÓN Y ESCUCHA ===
async function iniciar() {
  await client.connect();
  await client.query('LISTEN nuevo_correo');
  console.log('📡 Escuchando canal "nuevo_correo"...');

  client.on('notification', async (msg) => {
    const idNotificacion = msg.payload;

    console.log(`🔔 Nueva notificación ID: ${idNotificacion}`);

    try {
      const { rows } = await client.query(`
        SELECT n.id_notificacion, n.titulo, n.mensaje, u.correo_electronico
        FROM notificacion n
        JOIN usuario u ON u.rut = n.rut_destinatario
        WHERE n.id_notificacion = $1 AND n.enviada = FALSE AND n.canal = 'Email'
      `, [idNotificacion]);

      if (rows.length === 0) {
        console.log(`❌ Notificación ${idNotificacion} ya enviada o no válida.`);
        return;
      }

      const noti = rows[0];

      // 👇 Mostrar lo que se enviaría
      console.log('📤 Contenido del correo:');
      console.log('--------------------------------------');
      console.log(`📬 Para: ${noti.correo_electronico}`);
      console.log(`✉️  Asunto: ${noti.titulo}`);
      console.log(`📝 Mensaje:\n${noti.mensaje}`);
      console.log('--------------------------------------');

      // ✅ Enviar el correo
      await transporter.sendMail({
        from: '"Aerolinea" <ferremascontra78@gmail.com>',
        to: noti.correo_electronico,
        subject: noti.titulo,
        text: noti.mensaje,
      });

      // ✅ Marcar como enviada
      await client.query(`
        UPDATE notificacion SET enviada = TRUE WHERE id_notificacion = $1
      `, [noti.id_notificacion]);

      console.log(`✅ Correo enviado a ${noti.correo_electronico}`);

    } catch (err) {
      console.error('❌ Error al procesar notificación:', err);
    }
  });
}

iniciar();
