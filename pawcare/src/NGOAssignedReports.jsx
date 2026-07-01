// pages/NGOAssignedReports.jsx
import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom"; 

function NGOAssignedReports() {
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
    
    fetchAssignedReports();
  };

  const fetchAssignedReports = async () => {
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      
      const response = await fetch(`http://localhost:8080/api/rescue/ngo/assigned-reports?userEmail=${user.email}`, {
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
        setError(data.error || "Failed to fetch assigned reports");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (reportId, newStatus) => {
    try {
      const response = await fetch(`http://localhost:8080/api/rescue/report/${reportId}/status`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ 
          status: newStatus,
          notes: `Status updated to ${newStatus} by NGO`
        })
      });

      const data = await response.json();

      if (response.ok) {
        alert(`Report status updated to ${newStatus}!`);
        fetchAssignedReports(); // Refresh the list
      } else {
        alert(data.error || "Failed to update status");
      }
    } catch (err) {
      alert("Server error: " + err.message);
    }
  };

  const getStatusBadge = (status) => {
    const statusColors = {
      ASSIGNED: "badge-assigned",
      IN_PROGRESS: "badge-progress",
      RESCUED: "badge-rescued",
      COMPLETED: "badge-completed"
    };
    return <span className={`badge ${statusColors[status] || "badge-pending"}`}>{status}</span>;
  };

  const getNextStatus = (currentStatus) => {
    const statusFlow = {
      "ASSIGNED": { next: "IN_PROGRESS", label: "🚀 Start Rescue", color: "btn-warning" },
      "IN_PROGRESS": { next: "RESCUED", label: "🎉 Mark Rescued", color: "btn-success" },
      "RESCUED": { next: "COMPLETED", label: "✅ Mark Completed", color: "btn-info" }
    };
    return statusFlow[currentStatus];
  };

  if (loading) return <div className="loading">Loading assigned rescues...</div>;

  return (
    <div className="reports-container">
      <h2>📋 My Assigned Rescue Requests</h2>
      <p className="subtitle">Rescue requests assigned to your organization</p>
      
      {error && <div className="error-message">{error}</div>}
      
      {reports.length === 0 ? (
        <div className="no-reports">
          <p>No assigned rescue requests yet.</p>
          <Link to="/pending-reports">
            <button className="btn">Browse Available Rescues</button>
          </Link>
        </div>
      ) : (
        <div className="reports-grid">
          {reports.map((report) => {
            const nextStatus = getNextStatus(report.status);
            return (
              <div key={report.reportId} className="report-card">
                <div className="report-header">
                  <h3>{report.animalType}</h3>
                  {getStatusBadge(report.status)}
                </div>
                <div className="report-body">
                  <p><strong>Condition:</strong> {report.condition}</p>
                  <p><strong>Location:</strong> {report.locationAddress || "Address not provided"}</p>
                  <p><strong>Reported by:</strong> {report.reporterName}</p>
                  <p><strong>Contact:</strong> {report.reporterPhone}</p>
                  <p><strong>Reported:</strong> {new Date(report.createdAt).toLocaleString()}</p>
                  {report.description && (
                    <p><strong>Description:</strong> {report.description}</p>
                  )}
                </div>
                <div className="report-footer">
                  {nextStatus && (
                    <button 
                      onClick={() => updateStatus(report.reportId, nextStatus.next)} 
                      className={`btn-status ${nextStatus.color}`}
                    >
                      {nextStatus.label}
                    </button>
                  )}
                  <button 
                    onClick={() => window.open(`https://maps.google.com?q=${report.latitude},${report.longitude}`, "_blank")} 
                    className="btn-location-small"
                  >
                    🗺️ View on Map
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default NGOAssignedReports;