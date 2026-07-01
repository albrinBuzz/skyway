using System;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Newtonsoft.Json;

namespace FlightApp
{
    public partial class AddFlightForm : Form
    {
        // Configura el HttpClient
        private readonly HttpClient _httpClient = new HttpClient { BaseAddress = new Uri("http://tuapi.com/api/") }; // Cambia la URL base de la API.

        public AddFlightForm()
        {
            InitializeComponent();
        }

        private async void AddFlightForm_Load(object sender, EventArgs e)
        {
            // Cargar los datos de aeropuertos, aviones y pilotos al inicio
            await CargarAeropuertos();
            await CargarAviones();
            await CargarPilotos();
        }

        // Método para cargar aeropuertos desde la API
        private async Task CargarAeropuertos()
        {
            var response = await _httpClient.GetStringAsync("aeropuertos");
            var aeropuertos = JsonConvert.DeserializeObject<List<Aeropuerto>>(response);
            cmbAeropuertoSalida.DataSource = aeropuertos;
            cmbAeropuertoSalida.DisplayMember = "Nombre";
            cmbAeropuertoSalida.ValueMember = "Id";

            cmbAeropuertoLlegada.DataSource = aeropuertos;
            cmbAeropuertoLlegada.DisplayMember = "Nombre";
            cmbAeropuertoLlegada.ValueMember = "Id";
        }

        // Método para cargar aviones desde la API
        private async Task CargarAviones()
        {
            var response = await _httpClient.GetStringAsync("aviones");
            var aviones = JsonConvert.DeserializeObject<List<Avion>>(response);
            cmbAvion.DataSource = aviones;
            cmbAvion.DisplayMember = "Modelo";
            cmbAvion.ValueMember = "Id";
        }

        // Método para cargar pilotos desde la API
        private async Task CargarPilotos()
        {
            var response = await _httpClient.GetStringAsync("pilotos");
            var pilotos = JsonConvert.DeserializeObject<List<Piloto>>(response);
            cmbPiloto.DataSource = pilotos;
            cmbPiloto.DisplayMember = "Nombre";
            cmbPiloto.ValueMember = "Id";
        }

        // Listener para el cambio de selección en el ComboBox de Avión
        private async void cmbAvion_SelectedIndexChanged(object sender, EventArgs e)
        {
            // Solo ejecuta si se selecciona un avión
            if (cmbAvion.SelectedItem != null)
            {
                var avionId = ((Avion)cmbAvion.SelectedItem).Id;
                var avion = await ObtenerDetallesAvion(avionId);

                // Actualiza los campos relacionados con la capacidad y precios del avión
                if (avion != null)
                {
                    txtPrecioPrimera.Text = avion.PrecioPrimera.ToString();
                    txtPrecioEjecutiva.Text = avion.PrecioEjecutiva.ToString();
                    txtPrecioEconomica.Text = avion.PrecioEconomica.ToString();

                    txtAsientosPrimera.Text = avion.CapacidadPrimera.ToString();
                    txtAsientosEjecutiva.Text = avion.CapacidadEjecutiva.ToString();
                    txtAsientosEconomica.Text = avion.CapacidadEconomica.ToString();
                }
            }
        }

        // Método para obtener los detalles de un avión específico
        private async Task<Avion> ObtenerDetallesAvion(int avionId)
        {
            var response = await _httpClient.GetStringAsync($"aviones/{avionId}");
            var avion = JsonConvert.DeserializeObject<Avion>(response);
            return avion;
        }

        // Método para manejar el clic en el botón "Agregar Vuelo"
        private async void btnAgregarVuelo_Click(object sender, EventArgs e)
        {
            var vuelo = new
            {
                numeroVuelo = txtNumeroVuelo.Text,
                fechaHoraSalida = dtpFechaHoraSalida.Value,
                fechaHoraLlegada = dtpFechaHoraLlegada.Value,
                aeropuertoSalida = cmbAeropuertoSalida.SelectedValue, 
                aeropuertoLlegada = cmbAeropuertoLlegada.SelectedValue,
                precioVuelo = decimal.Parse(txtPrecioVuelo.Text),
                avion = cmbAvion.SelectedValue, 
                piloto = cmbPiloto.SelectedValue 
            };

            var content = new StringContent(JsonConvert.SerializeObject(vuelo), Encoding.UTF8, "application/json");
            var response = await _httpClient.PostAsync("vuelos/agregar", content);

            if (response.IsSuccessStatusCode)
            {
                MessageBox.Show("Vuelo agregado exitosamente.");
            }
            else
            {
                MessageBox.Show("Error al agregar el vuelo.");
            }
        }
    }

    // Clases de ejemplo para representar datos (puedes adaptar las clases a tus necesidades)
    public class Aeropuerto
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
    }

    public class Avion
    {
        public int Id { get; set; }
        public string Modelo { get; set; }
        public int CapacidadPrimera { get; set; }
        public int CapacidadEjecutiva { get; set; }
        public int CapacidadEconomica { get; set; }
        public decimal PrecioPrimera { get; set; }
        public decimal PrecioEjecutiva { get; set; }
        public decimal PrecioEconomica { get; set; }
    }

    public class Piloto
    {
        public int Id { get; set; }
        public string Nombre { get; set; }
    }
}
