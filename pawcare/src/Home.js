// Home.jsx - Original version (before location update)
import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import bg from "./bg_pawcare.avif";

function Home() {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const userData = localStorage.getItem("user");
    if (userData) {
      setUser(JSON.parse(userData));
    }
  }, []);

  const handleLogout = async () => {
    try {
      await fetch("http://localhost:8080/api/auth/logout", {
        method: "POST",
        credentials: "include",
      });
      localStorage.removeItem("user");
      setUser(null);
      alert("Logged out successfully");
      navigate("/");
      window.location.reload();
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  return (
    <>
      <div className="hero" style={{ backgroundImage: `url(${bg})` }}>
        <h1>Helping Animals, One Step at a Time 🐾</h1>
        <p>
          Report injured animals, find care tips, and connect with rescuers.
        </p>
        
        {user ? (
          <div className="user-greeting">
            <p>Welcome, {user.name}!</p>
            <p className="role-badge">
              Role: {user.role === "NGO" ? "🐾 Rescue Organization" : user.role === "ADMIN" ? "👑 Administrator" : "🐕 Animal Lover"}
              {/* {user.role === "NGO" && !user.isngoVerified && (
                <span className="pending-badge"> (Pending Verification)</span>
              )} */}
            </p>
            
            <div className="action-buttons">
              {user.role === "USER" && (
                <>
                  <Link to="/report-animal">
                    <button className="btn primary">📢 Report Injured Animal</button>
                  </Link>
                  <Link to="/my-reports">
                    <button className="btn secondary">📋 My Reports</button>
                  </Link>
                </>
              )}
              
              {user.role === "NGO" && (
                <>
                  <Link to="/pending-reports">
                    <button className="btn primary">🚑 View Rescue Requests</button>
                  </Link>
                  <Link to="/ngo-assigned">
                    <button className="btn secondary">📋 My Assigned Rescues</button>
                  </Link>
                </>
              )}
              
              {user.role === "ADMIN" && (
                <Link to="/admin">
                  <button className="btn primary">👑 Admin Dashboard</button>
                </Link>
              )}
              
              <Link to="/nearby-ngos">
                <button className="btn secondary">📍 Find Nearby NGOs</button>
              </Link>
              
              {/* <Link to="/vet-doctors">
                <button className="btn secondary">🩺 Vet Doctors</button>
              </Link> */}
              
              <button className="btn logout" onClick={handleLogout}>Logout</button>
            </div>
          </div>
        ) : (
          <Link to="/auth">
            <button className="btn">Join Us</button>
          </Link>
        )}
      </div>
    </>
  );
}

export default Home;