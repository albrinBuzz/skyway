package flightapp;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FlightApp extends Application {

    private static final String BASE_URL = "http://tuapi.com/api/";

    private ComboBox<Aeropuerto> aeropuertoSalidaComboBox;
    private ComboBox<Aeropuerto> aeropuertoLlegadaComboBox;
    private ComboBox<Avion> avionComboBox;
    private ComboBox<Piloto> pilotoComboBox;

    private TextField numeroVueloTextField;
    private DatePicker fechaHoraSalidaDatePicker;
    private DatePicker fechaHoraLlegadaDatePicker;
    private TextField precioVueloTextField;
    private TextField asientosPrimeraTextField;
    private TextField asientosEjecutivaTextField;
    private TextField asientosEconomicaTextField;
    private TextField precioPrimeraTextField;
    private TextField precioEjecutivaTextField;
    private TextField precioEconomicaTextField;

    private HttpClient client;
    private Gson gson;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Inicialización del HttpClient y Gson
        client = HttpClient.newHttpClient();
        gson = new Gson();

        // Creación del formulario
        GridPane root = new GridPane();
        root.setVgap(10);
        root.setHgap(10);

        // Campos del formulario
        numeroVueloTextField = new TextField();
        fechaHoraSalidaDatePicker = new DatePicker();
        fechaHoraLlegadaDatePicker = new DatePicker();
        precioVueloTextField = new TextField();

        // ComboBoxes
        aeropuertoSalidaComboBox = new ComboBox<>();
        aeropuertoLlegadaComboBox = new ComboBox<>();
        avionComboBox = new ComboBox<>();
        pilotoComboBox = new ComboBox<>();

        // TextFields para los detalles de los aviones
        asientosPrimeraTextField = new TextField();
        asientosEjecutivaTextField = new TextField();
        asientosEconomicaTextField = new TextField();
        precioPrimeraTextField = new TextField();
        precioEjecutivaTextField = new TextField();
        precioEconomicaTextField = new TextField();

        // Agregar al formulario
        root.add(new Label("Número de Vuelo:"), 0, 0);
        root.add(numeroVueloTextField, 1, 0);

        root.add(new Label("Fecha y Hora de Salida:"), 0, 1);
        root.add(fechaHoraSalidaDatePicker, 1, 1);

        root.add(new Label("Fecha y Hora de Llegada:"), 0, 2);
        root.add(fechaHoraLlegadaDatePicker, 1, 2);

        root.add(new Label("Precio del Vuelo:"), 0, 3);
        root.add(precioVueloTextField, 1, 3);

        root.add(new Label("Aeropuerto de Salida:"), 0, 4);
        root.add(aeropuertoSalidaComboBox, 1, 4);

        root.add(new Label("Aeropuerto de Llegada:"), 0, 5);
        root.add(aeropuertoLlegadaComboBox, 1, 5);

        root.add(new Label("Avión:"), 0, 6);
        root.add(avionComboBox, 1, 6);

        root.add(new Label("Piloto:"), 0, 7);
        root.add(pilotoComboBox, 1, 7);

        root.add(new Label("Asientos Primera Clase:"), 0, 8);
        root.add(asientosPrimeraTextField, 1, 8);

        root.add(new Label("Asientos Ejecutiva:"), 0, 9);
        root.add(asientosEjecutivaTextField, 1, 9);

        root.add(new Label("Asientos Económica:"), 0, 10);
        root.add(asientosEconomicaTextField, 1, 10);

        root.add(new Label("Precio Primera Clase:"), 0, 11);
        root.add(precioPrimeraTextField, 1, 11);

        root.add(new Label("Precio Ejecutiva:"), 0, 12);
        root.add(precioEjecutivaTextField, 1, 12);

        root.add(new Label("Precio Económica:"), 0, 13);
        root.add(precioEconomicaTextField, 1, 13);

        // Botón de agregar vuelo
        Button agregarVueloButton = new Button("Añadir Vuelo");
        root.add(agregarVueloButton, 1, 14);

        // Configurar el listener para el ComboBox del avión
        avionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                actualizarDetallesAvion(newValue);
            }
        });

        // Cargar datos iniciales al iniciar el formulario
        cargarAeropuertos();
        cargarAviones();
        cargarPilotos();

        // Configurar la escena y mostrar la ventana
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Añadir Vuelo");
        primaryStage.show();
    }

    // Método para cargar aeropuertos desde la API
    private void cargarAeropuertos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "aeropuertos"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    List<Aeropuerto> aeropuertos = gson.fromJson(response, new TypeToken<List<Aeropuerto>>() {}.getType());
                    ObservableList<Aeropuerto> observableList = FXCollections.observableArrayList(aeropuertos);
                    aeropuertoSalidaComboBox.setItems(observableList);
                    aeropuertoLlegadaComboBox.setItems(observableList);
                });
    }

    // Método para cargar aviones desde la API
    private void cargarAviones() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "aviones"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    List<Avion> aviones = gson.fromJson(response, new TypeToken<List<Avion>>() {}.getType());
                    ObservableList<Avion> observableList = FXCollections.observableArrayList(aviones);
                    avionComboBox.setItems(observableList);
                });
    }

    // Método para cargar pilotos desde la API
    private void cargarPilotos() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "pilotos"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    List<Piloto> pilotos = gson.fromJson(response, new TypeToken<List<Piloto>>() {}.getType());
                    ObservableList<Piloto> observableList = FXCollections.observableArrayList(pilotos);
                    pilotoComboBox.setItems(observableList);
                });
    }

    // Método para actualizar los detalles del avión seleccionado
    private void actualizarDetallesAvion(Avion avion) {
        asientosPrimeraTextField.setText(String.valueOf(avion.getCapacidadPrimera()));
        asientosEjecutivaTextField.setText(String.valueOf(avion.getCapacidadEjecutiva()));
        asientosEconomicaTextField.setText(String.valueOf(avion.getCapacidadEconomica()));

        precioPrimeraTextField.setText(String.valueOf(avion.getPrecioPrimera()));
        precioEjecutivaTextField.setText(String.valueOf(avion.getPrecioEjecutiva()));
        precioEconomicaTextField.setText(String.valueOf(avion.getPrecioEconomica()));
    }

    // Clases internas que representan los objetos de la API

    class Aeropuerto {
        private int id;
        private String nombre;

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre;
        }
    }

    class Avion {
        private int id;
        private String modelo;
        private int capacidadPrimera;
        private int capacidadEjecutiva;
        private int capacidadEconomica;
        private double precioPrimera;
        private double precioEjecutiva;
        private double precioEconomica;

        public int getId() { return id; }
        public String getModelo() { return modelo; }
        public int getCapacidadPrimera() { return capacidadPrimera; }
        public int getCapacidadEjecutiva() { return capacidadEjecutiva; }
        public int getCapacidadEconomica() { return capacidadEconomica; }
        public double getPrecioPrimera() { return precioPrimera; }
        public double getPrecioEjecutiva() { return precioEjecutiva; }
        public double getPrecioEconomica() { return precioEconomica; }

        @Override
        public String toString() {
            return modelo;
        }
    }

    class Piloto {
        private int id;
        private String nombre;

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
