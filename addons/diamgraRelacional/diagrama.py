import graphviz

dot = graphviz.Digraph(comment='Modelo Relacional Aerolínea', format='png')
dot.attr(rankdir='LR', fontsize='10')

# Definir tablas principales (simplificadas)
tablas = {
    "Usuario": ["RUT (PK)", "Nombre", "Correo_Electronico", "Telefono"],
    "Roles": ["id_rol (PK)", "nombre"],
    "RolUsuario": ["id_rol_usuario (PK)", "id_rol (FK)", "rut_usuario (FK)"],
    "Pasajero": ["RUT (PK/FK)", "Tipo_Documento", "Nacionalidad"],
    "Piloto": ["RUT (PK/FK)", "Licencia", "Experiencia_anos"],
    "Tripulacion": ["RUT (PK/FK)", "Cargo"],
    "Vuelo": ["ID_VUELO (PK)", "Numero_Vuelo", "ID_ESTADO_VUELO (FK)", "ID_AVION (FK)", "RUT_PILOTO (FK)"],
    "Avion": ["ID_AVION (PK)", "Numero_de_Registro"],
    "Clase_asiento": ["ID_CLASE (PK)", "Descripcion"],
    "Asiento": ["ID_ASIENTO (PK)", "Numero_Asiento", "ID_CLASE (FK)", "ID_AVION (FK)"],
    "Reserva": ["ID_RESERVA (PK)", "Fecha_Reserva", "RUT_PASAJERO (FK)", "Estado_Reserva"],
    "Reserva_Asiento": ["ID_RESERVA_ASIENTO (PK)", "ID_RESERVA (FK)", "ID_VUELO (FK)", "ID_ASIENTO (FK)"],
    "Notificacion": ["ID_NOTIFICACION (PK)", "RUT_DESTINATARIO (FK)", "Titulo", "Leido"],
    "Reembolso": ["ID_REEMBOLSO (PK)", "ID_RESERVA (FK)", "MONTO", "ESTADO"],
    "Segmento_Vuelo": ["ID_SEGMENTO (PK)", "ID_VUELO (FK)", "ORDEN_SEGMENTO"],
    "Itinerario": ["ID_ITINERARIO (PK)", "ORIGEN_AEROPUERTO (FK)", "DESTINO_AEROPUERTO (FK)"],
    "Itinerario_Vuelo": ["ID_ITINERARIO_VUELO (PK)", "ID_ITINERARIO (FK)", "ID_VUELO (FK)"],
    "Reserva_Itinerario": ["ID_RESERVA_ITINERARIO (PK)", "ID_RESERVA (FK)", "ID_ITINERARIO (FK)"],
}

# Agregar nodos
for tabla, campos in tablas.items():
    label = f"<<b>{tabla}</b><br/>" + "<br/>".join(campos) + ">"
    dot.node(tabla, label=label, shape='plaintext')

# Relaciones (FK simplificadas)
relaciones = [
    ("RolUsuario", "Usuario"),
    ("RolUsuario", "Roles"),
    ("Pasajero", "Usuario"),
    ("Piloto", "Usuario"),
    ("Tripulacion", "Usuario"),
    ("Vuelo", "Avion"),
    ("Vuelo", "Piloto"),
    ("Asiento", "Avion"),
    ("Asiento", "Clase_asiento"),
    ("Reserva", "Pasajero"),
    ("Reserva_Asiento", "Reserva"),
    ("Reserva_Asiento", "Asiento"),
    ("Reserva_Asiento", "Vuelo"),
    ("Notificacion", "Usuario"),
    ("Reembolso", "Reserva"),
    ("Segmento_Vuelo", "Vuelo"),
    ("Itinerario_Vuelo", "Itinerario"),
    ("Itinerario_Vuelo", "Vuelo"),
    ("Reserva_Itinerario", "Reserva"),
    ("Reserva_Itinerario", "Itinerario"),
]

# Agregar relaciones
for origen, destino in relaciones:
    dot.edge(origen, destino)

# Renderizar diagrama
dot.render('modelo_relacional_aerolinea', cleanup=True)
dot.view()
