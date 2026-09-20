package com.SkyWay.modules.avion.domain.model;

import java.io.Serializable;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import jakarta.persistence.*;

@Entity
@Table(name="configuracion_cabina")
public class ConfiguracionCabina implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "configuracion_cabina_seq")
    @SequenceGenerator(name = "configuracion_cabina_seq", sequenceName = "configuracion_cabina_seq", allocationSize = 1)
    @Column(name="id_configuracion")
    private Integer idConfiguracion;

    @Column(name="fila_inicio")
    private Integer filaInicio;

    @Column(name="fila_fin")
    private Integer filaFin;

    @Column(name="distribucion_columnas")
    private String distribucionColumnas;

    @Column(name="letras_columnas")
    private String letrasColumnas;

    @Column(name="es_salida_emergencia")
    private Boolean esSalidaEmergencia;

    @ManyToOne
    @JoinColumn(name="id_modelo")
    private ModeloAvion modeloAvion;

    @ManyToOne
    @JoinColumn(name="id_clase")
    private ClaseAsiento claseAsiento;

    public ConfiguracionCabina() {}

    // Getters y Setters
    public Integer getIdConfiguracion() { return idConfiguracion; }
    public void setIdConfiguracion(Integer idConfiguracion) { this.idConfiguracion = idConfiguracion; }

    public Integer getFilaInicio() { return filaInicio; }
    public void setFilaInicio(Integer filaInicio) { this.filaInicio = filaInicio; }

    public Integer getFilaFin() { return filaFin; }
    public void setFilaFin(Integer filaFin) { this.filaFin = filaFin; }

    public String getDistribucionColumnas() { return distribucionColumnas; }
    public void setDistribucionColumnas(String distribucionColumnas) { this.distribucionColumnas = distribucionColumnas; }

    public String getLetrasColumnas() { return letrasColumnas; }
    public void setLetrasColumnas(String letrasColumnas) { this.letrasColumnas = letrasColumnas; }

    public Boolean getEsSalidaEmergencia() { return esSalidaEmergencia; }
    public void setEsSalidaEmergencia(Boolean esSalidaEmergencia) { this.esSalidaEmergencia = esSalidaEmergencia; }

    public ModeloAvion getModeloAvion() { return modeloAvion; }
    public void setModeloAvion(ModeloAvion modeloAvion) { this.modeloAvion = modeloAvion; }

    public ClaseAsiento getClaseAsiento() { return claseAsiento; }
    public void setClaseAsiento(ClaseAsiento claseAsiento) { this.claseAsiento = claseAsiento; }
}