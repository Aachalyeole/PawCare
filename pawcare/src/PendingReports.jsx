// pages/PendingReports.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

function PendingReports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    checkNGOAndFetch();
  }, []);

  const checkNGOAndFetch = async () => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user) {
      navigate("/auth");
      return;
    }
    
    if (user.role !== "NGO") {
      alert("Access denied. NGO privileges required.");
      navigate("/");
      return;
    }
    
    fetchPendingReports();
  };

  const fetchPendingReports = async () => {
    try {
      const response = await fetch("http://localhost:8080/api/rescue/pending-reports", {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        }
      });

      const data = await response.json();

      if (response.ok) {
        setReports(Array.isArray(data) ? data : []);
      } else {
        setError(data.error || "Failed to fetch pending reports");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const claimReport = async (reportId) => {
    if (!window.confirm("Are you sure you want to claim this rescue request?")) {
      return;
    }

    try {
      const response = await fetch(`http://localhost:8080/api/rescue/report/${reportId}/claim`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        }
      });

      const data = await response.json();

      if (response.ok) {
        alert("Report claimed successfully!");
        fetchPendingReports(); // Refresh the list
      } else {
        alert(data.error || "Failed to claim report");
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  const getEmergencyBadge = (isEmergency) => {
    if (isEmergency) {
      return <span className="emergency-badge">🚨 EMERGENCY</span>;
    }
    return null;
  };

  if (loading) return <div className="loading">Loading pending rescue requests...</div>;

  return (
    <div className="reports-container">
      <h2>🚑 Available Rescue Requests</h2>
      <p className="subtitle">Rescue requests waiting for NGO response</p>
      
      {error && <div className="error-message">{error}</div>}
      
      {reports.length === 0 ? (
        <div className="no-reports">
          <p>No pending rescue requests at the moment.</p>
          <p>Check back later for animals in need! 🐾</p>
        </div>
      ) : (
        <div className="reports-grid">
          {reports.map((report) => (
            <div key={report.reportId} className={`report-card ${report.isEmergency ? 'emergency-card' : ''}`}>
              <div className="report-header">
                <h3>{report.animalType}</h3>
                {getEmergencyBadge(report.isEmergency)}
                <span className="badge badge-pending">PENDING</span>
              </div>
              <div className="report-body">
                <p><strong>Condition:</strong> {report.condition}</p>
                <p><strong>Location:</strong> {report.locationAddress || "Address not provided"}</p>
                <p><strong>Reported by:</strong> {report.reporterName}</p>
                <p><strong>Contact:</strong> {report.reporterPhone}</p>
                <p><strong>Reported:</strong> {new Date(report.createdAt).toLocaleString()}</p>
                {report.description && (
                  <p><strong>Description:</strong> {report.description.substring(0, 100)}...</p>
                )}
              </div>
              <div className="report-footer">
                <button onClick={() => claimReport(report.reportId)} className="btn-claim">
                  📍 Claim Rescue
                </button>
                <button 
                  onClick={() => window.open(`https://maps.google.com?q=${report.latitude},${report.longitude}`, "_blank")} 
                  className="btn-location-small"
                >
                  🗺️ View on Map
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default PendingReports;