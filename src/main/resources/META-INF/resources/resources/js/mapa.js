document.addEventListener("DOMContentLoaded", function () {
  var map = L.map("leafletMap").setView([20, 0], 2);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    attribution: "© OpenStreetMap contributors"
  }).addTo(map);

  var locations = [
    { name: "Nueva York, EE.UU.", lat: 40.7128, lng: -74.0060 },
    { name: "Madrid, España", lat: 40.4168, lng: -3.7038 },
    { name: "Río de Janeiro, Brasil", lat: -22.9068, lng: -43.1729 }
  ];

  locations.forEach(function (loc) {
    L.marker([loc.lat, loc.lng])
     .addTo(map)
     .bindPopup(loc.name);
  });
});
