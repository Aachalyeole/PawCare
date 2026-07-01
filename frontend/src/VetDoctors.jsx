// pages/VetDoctors.jsx
import React, { useState } from "react";

function VetDoctors() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCity, setSelectedCity] = useState("all");

  // Static list of veterinary doctors
  const vetDoctors = [
    {
      id: 1,
      name: "Dr. Rajesh Sharma",
      qualification: "BVSc & AH, MVSc",
      specialization: "Small Animal Surgery",
      clinic: "Pune Pet Clinic",
      address: "123, Shivajinagar, Pune - 411005",
      city: "Pune",
      phone: "+91 98765 43210",
      emergency: "+91 98765 43211",
      email: "dr.rajesh@punepetclinic.com",
      experience: "15 years",
      available: "24/7 Emergency",
      rating: 4.8,
      image: "🩺"
    },
    {
      id: 2,
      name: "Dr. Sneha Patil",
      qualification: "BVSc, PhD",
      specialization: "Veterinary Medicine",
      clinic: "Animal Care Center",
      address: "45, Koregaon Park, Pune - 411001",
      city: "Pune",
      phone: "+91 98223 45678",
      emergency: "+91 98223 45679",
      email: "dr.sneha@animalcare.com",
      experience: "10 years",
      available: "9 AM - 9 PM",
      rating: 4.9,
      image: "🐱"
    },
    {
      id: 3,
      name: "Dr. Vikram Singh",
      qualification: "BVSc & AH",
      specialization: "Orthopedics",
      clinic: "Vet Speciality Clinic",
      address: "89, Baner Road, Pune - 411045",
      city: "Pune",
      phone: "+91 98501 23456",
      emergency: "+91 98501 23457",
      email: "dr.vikram@vetspeciality.com",
      experience: "8 years",
      available: "10 AM - 8 PM",
      rating: 4.7,
      image: "🐕"
    },
    {
      id: 4,
      name: "Dr. Priya Kulkarni",
      qualification: "BVSc, MVSc",
      specialization: "Dermatology",
      clinic: "Pets & Vets Clinic",
      address: "12, Kothrud, Pune - 411038",
      city: "Pune",
      phone: "+91 98901 78901",
      emergency: "+91 98901 78902",
      email: "dr.priya@petsandvets.com",
      experience: "7 years",
      available: "9 AM - 7 PM",
      rating: 4.6,
      image: "🐾"
    },
    {
      id: 5,
      name: "Dr. Amit Joshi",
      qualification: "BVSc & AH",
      specialization: "Emergency & Critical Care",
      clinic: "Emergency Vet Hospital",
      address: "56, Hinjewadi, Pune - 411057",
      city: "Pune",
      phone: "+91 99600 11223",
      emergency: "+91 99600 11224",
      email: "dr.amit@emergencyvet.com",
      experience: "12 years",
      available: "24/7 Emergency",
      rating: 4.9,
      image: "🚑"
    },
    {
      id: 6,
      name: "Dr. Meera Deshmukh",
      qualification: "BVSc, MVSc",
      specialization: "Dentistry",
      clinic: "Smile Pet Dental",
      address: "34, Wakad, Pune - 411057",
      city: "Pune",
      phone: "+91 99234 56789",
      emergency: "+91 99234 56780",
      email: "dr.meera@smilepet.com",
      experience: "6 years",
      available: "10 AM - 6 PM",
      rating: 4.7,
      image: "🐈"
    },
    {
      id: 7,
      name: "Dr. Sanjay More",
      qualification: "BVSc & AH",
      specialization: "General Medicine",
      clinic: "City Pet Hospital",
      address: "78, Camp Area, Pune - 411001",
      city: "Pune",
      phone: "+91 98888 99900",
      emergency: "+91 98888 99901",
      email: "dr.sanjay@citypethospital.com",
      experience: "20 years",
      available: "8 AM - 10 PM",
      rating: 5.0,
      image: "🏥"
    },
    {
      id: 8,
      name: "Dr. Neha Mahajan",
      qualification: "BVSc, PhD",
      specialization: "Nutrition",
      clinic: "Healthy Paws Clinic",
      address: "23, Aundh, Pune - 411007",
      city: "Pune",
      phone: "+91 97654 32109",
      emergency: "+91 97654 32108",
      email: "dr.neha@healthypaws.com",
      experience: "5 years",
      available: "9 AM - 8 PM",
      rating: 4.5,
      image: "🥗"
    }
  ];

  // Filter doctors based on search and city
  const filteredDoctors = vetDoctors.filter(doctor => {
    const matchesSearch = doctor.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          doctor.specialization.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          doctor.clinic.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCity = selectedCity === "all" || doctor.city === selectedCity;
    return matchesSearch && matchesCity;
  });

  // Get unique cities for filter
  const cities = ["all", ...new Set(vetDoctors.map(d => d.city))];

  const handleCall = (phone) => {
    window.location.href = `tel:${phone}`;
  };

  const getRatingStars = (rating) => {
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (halfStar ? 1 : 0);
    
    return (
      <div className="rating-stars">
        {'⭐'.repeat(fullStars)}
        {halfStar && '½'}
        {'☆'.repeat(emptyStars)}
        <span className="rating-value">({rating})</span>
      </div>
    );
  };

  return (
    <div className="vet-doctors-container">
      <div className="vet-header">
        <h1>🐾 Veterinary Doctors Contact Information</h1>
        <p>Find trusted veterinary doctors near you for emergency and regular care</p>
      </div>

      <div className="vet-search-section">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Search by doctor name, specialization, or clinic..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <span className="search-icon">🔍</span>
        </div>

        <div className="filter-section">
          <label>Filter by City:</label>
          <select value={selectedCity} onChange={(e) => setSelectedCity(e.target.value)} className="city-filter">
            {cities.map(city => (
              <option key={city} value={city}>
                {city === "all" ? "All Cities" : city}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="emergency-tips">
        <div className="tip-card">
          <span className="tip-icon">🚨</span>
          <h4>Emergency?</h4>
          <p>Call the emergency number directly for immediate assistance</p>
        </div>
        <div className="tip-card">
          <span className="tip-icon">📋</span>
          <h4>Before Visit</h4>
          <p>Call ahead to confirm availability and emergency status</p>
        </div>
        <div className="tip-card">
          <span className="tip-icon">📄</span>
          <h4>Documents</h4>
          <p>Carry previous medical records and vaccination history</p>
        </div>
      </div>

      <div className="doctors-count">
        {filteredDoctors.length} {filteredDoctors.length === 1 ? "Doctor" : "Doctors"} available
      </div>

      <div className="doctors-grid">
        {filteredDoctors.map((doctor) => (
          <div key={doctor.id} className="doctor-card">
            <div className="doctor-header">
              <div className="doctor-avatar">
                <span className="avatar-icon">{doctor.image}</span>
              </div>
              <div className="doctor-info">
                <h3>{doctor.name}</h3>
                <p className="qualification">{doctor.qualification}</p>
                {getRatingStars(doctor.rating)}
              </div>
            </div>
            
            <div className="doctor-details">
              <div className="detail-row">
                <span className="detail-icon">🎯</span>
                <span className="detail-text">
                  <strong>Specialization:</strong> {doctor.specialization}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">🏥</span>
                <span className="detail-text">
                  <strong>Clinic:</strong> {doctor.clinic}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">📍</span>
                <span className="detail-text">
                  <strong>Address:</strong> {doctor.address}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">📞</span>
                <span className="detail-text">
                  <strong>Phone:</strong> {doctor.phone}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">🚨</span>
                <span className="detail-text emergency">
                  <strong>Emergency:</strong> {doctor.emergency}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">✉️</span>
                <span className="detail-text">
                  <strong>Email:</strong> {doctor.email}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">⏰</span>
                <span className="detail-text">
                  <strong>Available:</strong> {doctor.available}
                </span>
              </div>
              <div className="detail-row">
                <span className="detail-icon">📅</span>
                <span className="detail-text">
                  <strong>Experience:</strong> {doctor.experience}
                </span>
              </div>
            </div>
            
            <div className="doctor-actions">
              <button onClick={() => handleCall(doctor.phone)} className="btn-call-now">
                📞 Call {doctor.name.split(" ")[1]}
              </button>
              <button onClick={() => handleCall(doctor.emergency)} className="btn-emergency">
                🚨 Emergency
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredDoctors.length === 0 && (
        <div className="no-doctors">
          <p>No doctors found matching your search.</p>
          <p>Try different search terms or clear filters.</p>
        </div>
      )}

      <div className="vet-footer-note">
        <p>⚠️ Note: This is a static list for demonstration. In production, this information would be fetched from verified sources.</p>
        <p>🐾 For any corrections or additions, please contact PawCare support.</p>
      </div>
    </div>
  );
}

export default VetDoctors;