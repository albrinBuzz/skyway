using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

public class AvionService
{
    private readonly HttpClient _httpClient;
    private const string BaseUrl = "http://localhost:8080/api/aviones";

    public AvionService()
    {
        _httpClient = new HttpClient();
        _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    }

    public async Task<List<Avion>> GetAllAviones()
    {
        var response = await _httpClient.GetAsync(BaseUrl);
        response.EnsureSuccessStatusCode();
        var jsonResponse = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<List<Avion>>(jsonResponse);
    }

    public async Task<Avion> GetAvionById(int id)
    {
        var response = await _httpClient.GetAsync($"{BaseUrl}/{id}");
        response.EnsureSuccessStatusCode();
        var jsonResponse = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<Avion>(jsonResponse);
    }

    public async Task<Avion> CreateAvion(Avion avion)
    {
        var jsonContent = JsonSerializer.Serialize(avion);
        var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

        var response = await _httpClient.PostAsync(BaseUrl, content);
        response.EnsureSuccessStatusCode();
        var jsonResponse = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<Avion>(jsonResponse);
    }

    public async Task<Avion> UpdateAvion(int id, Avion avion)
    {
        var jsonContent = JsonSerializer.Serialize(avion);
        var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

        var response = await _httpClient.PutAsync($"{BaseUrl}/{id}", content);
        response.EnsureSuccessStatusCode();
        var jsonResponse = await response.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<Avion>(jsonResponse);
    }

    public async Task DeleteAvion(int id)
    {
        var response = await _httpClient.DeleteAsync($"{BaseUrl}/{id}");
        response.EnsureSuccessStatusCode();
    }
}
