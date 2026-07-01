// pages/ReportAnimal.jsx - FIXED with proper authentication
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

function ReportAnimal() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedImages, setSelectedImages] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);
  const [formData, setFormData] = useState({
    animalType: "",
    condition: "",
    description: "",
    latitude: "",
    longitude: "",
    locationAddress: "",
    isEmergency: false,
    contactPhone: "",
    contactName: ""
  });

  const animalTypes = ["Dog", "Cat", "Cow", "Bird", "Other"];
  const conditions = ["Injured", "Abandoned", "Sick", "Stray", "Emergency"];

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value
    });
    setError("");
  };

  const handleImageSelect = (e) => {
    const files = Array.from(e.target.files);
    setSelectedImages(files);
    
    const previews = files.map(file => URL.createObjectURL(file));
    setImagePreviews(previews);
  };

  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData({
            ...formData,
            latitude: position.coords.latitude,
            longitude: position.coords.longitude
          });
          alert("Location captured successfully!");
        },
        (error) => {
          alert("Error getting location: " + error.message);
        }
      );
    } else {
      alert("Geolocation is not supported by this browser.");
    }
  };

  // In ReportAnimal.jsx, update the handleSubmit function:

const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
        const user = JSON.parse(localStorage.getItem("user"));
        if (!user) {
            setError("Please login first");
            navigate("/auth");
            return;
        }

        const submitData = new FormData();
        
        const reportData = {
            animalType: formData.animalType,
            condition: formData.condition,
            description: formData.description,
            latitude: parseFloat(formData.latitude) || 0,
            longitude: parseFloat(formData.longitude) || 0,
            locationAddress: formData.locationAddress || "",
            isEmergency: formData.isEmergency,
            contactPhone: formData.contactPhone || user.phone,
            contactName: formData.contactName || user.name,
            userEmail: user.email  // ADD THIS LINE
        };
        
        const reportBlob = new Blob([JSON.stringify(reportData)], { type: 'application/json' });
        submitData.append("report", reportBlob);
        
        selectedImages.forEach((image) => {
            submitData.append("images", image);
        });

        const response = await fetch("http://localhost:8080/api/rescue/report", {
            method: "POST",
            credentials: "include",
            body: submitData
        });

        const data = await response.json();

        if (response.ok) {
            alert("Report submitted successfully!");
            navigate("/my-reports");
        } else {
            setError(data.error || "Failed to submit report");
        }
    } catch (err) {
        setError("Server error: " + err.message);
    } finally {
        setLoading(false);
    }
};

  return (
    <div className="report-container">
      <div className="report-form">
        <h2>🐾 Report Injured Animal</h2>
        
        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Animal Type *</label>
            <select name="animalType" value={formData.animalType} onChange={handleChange} required>
              <option value="">Select Animal Type</option>
              {animalTypes.map(type => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Condition *</label>
            <select name="condition" value={formData.condition} onChange={handleChange} required>
              <option value="">Select Condition</option>
              {conditions.map(cond => (
                <option key={cond} value={cond}>{cond}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Description *</label>
            <textarea
              name="description"
              rows="4"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe the animal's condition, injuries, location details..."
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Latitude *</label>
              <input
                type="number"
                step="any"
                name="latitude"
                value={formData.latitude}
                onChange={handleChange}
                placeholder="0.0000"
                required
              />
            </div>
            <div className="form-group">
              <label>Longitude *</label>
              <input
                type="number"
                step="any"
                name="longitude"
                value={formData.longitude}
                onChange={handleChange}
                placeholder="0.0000"
                required
              />
            </div>
          </div>

          <button type="button" onClick={getCurrentLocation} className="btn-location">
            📍 Get Current Location
          </button>

          <div className="form-group">
            <label>Location Address</label>
            <input
              type="text"
              name="locationAddress"
              value={formData.locationAddress}
              onChange={handleChange}
              placeholder="Street address, landmark..."
            />
          </div>

          <div className="form-group">
            <label>Contact Name</label>
            <input
              type="text"
              name="contactName"
              value={formData.contactName}
              onChange={handleChange}
              placeholder="Your name"
            />
          </div>

          <div className="form-group">
            <label>Contact Phone</label>
            <input
              type="tel"
              name="contactPhone"
              value={formData.contactPhone}
              onChange={handleChange}
              placeholder="Your phone number"
            />
          </div>

          <div className="form-group checkbox">
            <label>
              <input
                type="checkbox"
                name="isEmergency"
                checked={formData.isEmergency}
                onChange={handleChange}
              />
              This is an EMERGENCY (Requires immediate attention)
            </label>
          </div>

          <div className="form-group">
            <label>Upload Photos (Optional)</label>
            <input
              type="file"
              multiple
              accept="image/*"
              onChange={handleImageSelect}
              className="file-input"
            />
            {imagePreviews.length > 0 && (
              <div className="image-previews">
                <p>{selectedImages.length} image(s) selected</p>
                <div className="preview-container">
                  {imagePreviews.map((preview, index) => (
                    <img key={index} src={preview} alt={`Preview ${index}`} className="preview-image" />
                  ))}
                </div>
              </div>
            )}
          </div>

          <button type="submit" disabled={loading} className="btn-submit">
            {loading ? "Submitting..." : "Submit Rescue Request"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default ReportAnimal;