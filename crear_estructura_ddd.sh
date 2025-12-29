#!/bin/bash

# Nombre del proyecto
PROJECT_NAME="SkyWay"

# Directorio raíz del proyecto
ROOT_DIR=$(pwd)

# Crear directorios para cada dominio de entidad (modelo DDD)
DOMAINS=(
  "aerolinea"
  "avion"
  "claseasiento"
  "fabricante"
  "modeloavion"
  "pasajero"
  "reservaasiento"
  "segmentovuelo"
  "turno"
  "aeropuerto"
  "capacidadclase"
  "equipaje"
  "itinerario"
  "notificacion"
  "personaladministrativo"
  "reservaitinerario"
  "tipoequipaje"
  "turnotripulacion"
  "asiento"
  "checkin"
  "estadoreserva"
  "itinerariovuelo"
  "pago"
  "precioasiento"
  "piloto"
  "reserva"
  "tipoturno"
  "vuelo"
  "asignacionpuerta"
  "ciudad"
  "estadovuelo"
  "metodopago"
  "pai"
  "puertaembarque"
  "rolusuario"
  "tripulacion"
  "usuario"
  "tarifa"
  "tarifaItinerario"
  "CaracteristicaTarifa"
  "TarifaCaracteristica"
  "PasajeroReserva"
)

# Crear directorios para cada dominio y sus respectivas capas DDD
for domain in "${DOMAINS[@]}"; do
    # Dominio (Entidad + Agregado)
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/domain/model"

    # Repositorios (Persistencia)
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/domain/repository"

    # Servicios del Dominio (lógica de negocio)
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/domain/service"

    # Servicio de Aplicación (serviceImpl) - Orquesta la lógica del dominio
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/application/serviceImpl"

    # Presentación (Controller, DTO, Mapper)
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/presentation/controller"
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/presentation/dto"  # DTOs
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/presentation/mapper"  # Mapper

    # Excepciones (Captura y manejo de errores)
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/infrastructure/exception"

    # Validadores y Utilidades
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/infrastructure/validator"

    # Beans (Managed Beans) si se usa JSF
    mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/modules/$domain/presentation/bean"
done

# Directorios comunes globales
mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/infrastructure/exception"
mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/infrastructure/validator"
mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/infrastructure/mapper"
mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/infrastructure/exceptionhandler"

# Directorios para configuración global (por ejemplo: JSF, seguridad)
#mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/config"
#mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/security"
#mkdir -p "$ROOT_DIR/src/main/java/com/SkyWay/util"


# Mensaje de confirmación
echo "Estructura DDD y compatible con JSF para el proyecto '$PROJECT_NAME' creada con éxito."
