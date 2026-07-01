// pages/FindNearbyNGOs.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default marker icons in Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

// Custom marker icons
const userIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const ngoIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Component to center map on user location
function SetViewOnLocation({ center, zoom }) {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.setView(center, zoom);
    }
  }, [center, map, zoom]);
  return null;
}

function FindNearbyNGOs() {
  const [ngos, setNgos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [userLocation, setUserLocation] = useState(null);
  const [gettingLocation, setGettingLocation] = useState(false);
  const [radius, setRadius] = useState(10); // Default 10km
  const [searchPerformed, setSearchPerformed] = useState(false);
  const navigate = useNavigate();

  // Get current user location
  const getCurrentLocation = () => {
    setGettingLocation(true);
    setError("");
    
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };
          setUserLocation(location);
          setGettingLocation(false);
          // Auto search after getting location
          searchNearbyNGOs(location.lat, location.lng);
        },
        (error) => {
          setError("Error getting location: " + error.message);
          setGettingLocation(false);
        }
      );
    } else {
      setError("Geolocation is not supported by this browser.");
      setGettingLocation(false);
    }
  };

  // Search nearby NGOs
  const searchNearbyNGOs = async (lat, lng) => {
    setLoading(true);
    setError("");
    
    try {
      const response = await fetch("http://localhost:8080/api/rescue/nearby-ngos", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify({
          latitude: lat,
          longitude: lng,
          radius: radius
        })
      });

      const data = await response.json();

      if (response.ok) {
        setNgos(data.ngos || []);
        setSearchPerformed(true);
        if (data.ngos && data.ngos.length === 0) {
          setError(`No NGOs found within ${radius} km radius. Try increasing the radius.`);
        }
      } else {
        setError(data.error || "Failed to fetch nearby NGOs");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (userLocation) {
      searchNearbyNGOs(userLocation.lat, userLocation.lng);
    } else {
      getCurrentLocation();
    }
  };

  const handleRadiusChange = (e) => {
    setRadius(parseInt(e.target.value));
  };

  const handleManualLocation = (e) => {
    e.preventDefault();
    // You can add manual address search here
    alert("Manual location search coming soon! Please use 'Use My Location' button.");
  };

  const contactNGO = (ngo) => {
    if (ngo.emergencyContact) {
      window.location.href = `tel:${ngo.emergencyContact}`;
    } else if (ngo.phone) {
      window.location.href = `tel:${ngo.phone}`;
    } else {
      alert("No contact number available for this NGO");
    }
  };

  return (
    <div className="nearby-ngos-container">
      <h2>📍 Find Nearby Rescue Organizations</h2>
      <p className="subtitle">Locate animal rescue NGOs near you for immediate help</p>

      <div className="search-controls">
        <div className="location-controls">
          <button 
            onClick={getCurrentLocation} 
            className="btn-location"
            disabled={gettingLocation}
          >
            {gettingLocation ? "Getting Location..." : "📍 Use My Location"}
          </button>
          
          <div className="radius-control">
            <label>Search Radius: </label>
            <input
              type="range"
              min="1"
              max="50"
              value={radius}
              onChange={handleRadiusChange}
              className="radius-slider"
            />
            <span>{radius} km</span>
          </div>
          
          <button 
            onClick={handleSearch} 
            className="btn-search"
            disabled={loading || !userLocation}
          >
            {loading ? "Searching..." : "🔍 Search Nearby NGOs"}
          </button>
        </div>
        
        <div className="manual-location">
          <button onClick={handleManualLocation} className="btn-manual">
            📝 Enter Address Manually
          </button>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      {userLocation && searchPerformed && (
        <div className="map-container">
          <MapContainer
            center={[userLocation.lat, userLocation.lng]}
            zoom={12}
            style={{ height: "400px", width: "100%", borderRadius: "10px" }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            <SetViewOnLocation center={[userLocation.lat, userLocation.lng]} zoom={12} />
            
            {/* User Location Marker */}
            <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
              <Popup>
                <strong>You are here</strong>
              </Popup>
            </Marker>
            
            {/* NGO Markers */}
            {ngos.map((ngo) => (
              <Marker 
                key={ngo.ngoId} 
                position={[ngo.latitude, ngo.longitude]} 
                icon={ngoIcon}
              >
                <Popup>
                  <div className="popup-content">
                    <strong>{ngo.ngoName}</strong>
                    <p>{ngo.address || ngo.city}</p>
                    <p>📞 {ngo.emergencyContact || ngo.phone}</p>
                    <p>📏 {ngo.distance?.toFixed(1)} km away</p>
                    <button onClick={() => contactNGO(ngo)} className="popup-call-btn">
                      📞 Call Now
                    </button>
                  </div>
                </Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      )}

      {/* Results List */}
      {searchPerformed && (
        <div className="ngos-results">
          <h3>Nearby NGOs ({ngos.length} found)</h3>
          
          {ngos.length === 0 && !error && (
            <div className="no-results">
              <p>No NGOs found in your area. Try increasing the search radius.</p>
            </div>
          )}
          
          <div className="ngos-list">
            {ngos.map((ngo) => (
              <div key={ngo.ngoId} className="ngo-card">
                <div className="ngo-card-header">
                  <h4>{ngo.ngoName}</h4>
                  <span className="ngo-distance">{ngo.distance?.toFixed(1)} km away</span>
                </div>
                <div className="ngo-card-body">
                  <p><strong>📍 Address:</strong> {ngo.address}, {ngo.city}, {ngo.state}</p>
                  <p><strong>📞 Phone:</strong> {ngo.phone}</p>
                  {ngo.emergencyContact && (
                    <p><strong>🚨 Emergency:</strong> {ngo.emergencyContact}</p>
                  )}
                  {ngo.description && (
                    <p><strong>ℹ️ About:</strong> {ngo.description.substring(0, 150)}...</p>
                  )}
                </div>
                <div className="ngo-card-footer">
                  <button onClick={() => contactNGO(ngo)} className="btn-call">
                    📞 Call Now
                  </button>
                  <button 
                    onClick={() => window.open(`https://maps.google.com?q=${ngo.latitude},${ngo.longitude}`, "_blank")} 
                    className="btn-directions"
                  >
                    🗺️ Get Directions
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {!searchPerformed && !userLocation && (
        <div className="welcome-message">
          <div className="welcome-card">
            <span className="welcome-icon">🐾</span>
            <h3>Find Help Near You</h3>
            <p>Click "Use My Location" to find animal rescue organizations near you.</p>
            <p>You can also adjust the search radius to find NGOs farther away.</p>
          </div>
        </div>
      )}
    </div>
  );
}

export default FindNearbyNGOs;