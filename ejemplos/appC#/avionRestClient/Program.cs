using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Threading.Tasks;

public class Avion
{
    public int IdAvion { get; set; }
    public string NumeroDeRegistro { get; set; }
    public string Modelo { get; set; }
    public string Fabricante { get; set; }
    public int AnoDeFabricacion { get; set; }
    public int CapacidadDePasajeros { get; set; }
    public int CapacidadDeCarga { get; set; }
    public string EstadoDeMantenimiento { get; set; }

    public override string ToString()
    {
        return $"ID: {IdAvion}, " +
               $"Número de Registro: {NumeroDeRegistro}, " +
               $"Modelo: {Modelo}, " +
               $"Fabricante: {Fabricante}, " +
               $"Año de Fabricación: {AnoDeFabricacion}, " +
               $"Capacidad de Pasajeros: {CapacidadDePasajeros}, " +
               $"Capacidad de Carga: {CapacidadDeCarga}, " +
               $"Estado de Mantenimiento: {EstadoDeMantenimiento}";
    }
}

class Program
{
    private static readonly HttpClient _httpClient = new HttpClient();
    private const string BaseUrl = "http://localhost:8080/api/aviones";

    static async Task Main(string[] args)
    {
        _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

        // Crear un nuevo avión
        var nuevoAvion = new Avion
        {
            NumeroDeRegistro = "N12345",
            Modelo = "Boeing 737",
            Fabricante = "Boeing",
            AnoDeFabricacion = 2015,
            CapacidadDePasajeros = 180,
            CapacidadDeCarga = 20000,
            EstadoDeMantenimiento = "Operativo"
        };

        /*var createdAvion = await CreateAvionAsync(nuevoAvion);
        Console.WriteLine($"Avión creado: {createdAvion}");*/

        // Obtener todos los aviones
        var allAviones = await GetAllAviones();
        foreach (var avion in allAviones)
        {
            Console.WriteLine(avion);
        }

        // Ejemplo de obtención de un avión por ID
        /*if (createdAvion != null)
        {
            var avionById = await GetAvionById(createdAvion.IdAvion);
            Console.WriteLine($"Avión obtenido por ID: {avionById}");
        }*/

        // Actualizar un avión
        /*createdAvion.Modelo = "Boeing 737 MAX";
        var updatedAvion = await UpdateAvion(createdAvion.IdAvion, createdAvion);
        Console.WriteLine($"Avión actualizado: {updatedAvion}");*/

        // Eliminar un avión
        //await DeleteAvion(createdAvion.IdAvion);
        //Console.WriteLine($"Avión eliminado con ID: {createdAvion.IdAvion}");
    }

    public static async Task<List<Avion>> GetAllAviones()
    {
        var response = await _httpClient.GetAsync(BaseUrl);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<List<Avion>>();
    }

    public static async Task<Avion> GetAvionById(int id)
    {
        var response = await _httpClient.GetAsync($"{BaseUrl}/{id}");
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<Avion>();
    }

    public static async Task<Avion> CreateAvionAsync(Avion avion)
    {
        var response = await _httpClient.PostAsJsonAsync(BaseUrl + "/crear", avion);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<Avion>();
    }

    public static async Task<Avion> UpdateAvion(int id, Avion avion)
    {
        var response = await _httpClient.PutAsJsonAsync($"{BaseUrl}/{id}", avion);
        response.EnsureSuccessStatusCode();
        return await response.Content.ReadFromJsonAsync<Avion>();
    }

    public static async Task DeleteAvion(int id)
    {
        var response = await _httpClient.DeleteAsync($"{BaseUrl}/{id}");
        response.EnsureSuccessStatusCode();
    }
}
