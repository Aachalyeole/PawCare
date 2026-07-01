// pages/MyReports.jsx - Complete updated version

import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";

function MyReports() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user) {
        navigate("/auth");
        return;
      }

      console.log("Fetching reports for user:", user.email);

      const response = await fetch(`http://localhost:8080/api/rescue/my-reports?userEmail=${user.email}`, {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        }
      });
      
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("user");
        navigate("/auth");
        return;
      }
      
      const data = await response.json();
      
      if (response.ok) {
        setReports(Array.isArray(data) ? data : []);
      } else {
        setError(data.error || "Failed to fetch reports");
      }
    } catch (err) {
      console.error("Error fetching reports:", err);
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusColors = {
      PENDING: "badge-pending",
      ASSIGNED: "badge-assigned",
      IN_PROGRESS: "badge-progress",
      RESCUED: "badge-rescued",
      COMPLETED: "badge-completed"
    };
    return <span className={`badge ${statusColors[status] || "badge-pending"}`}>{status}</span>;
  };

  if (loading) return <div className="loading">Loading your reports...</div>;

  return (
    <div className="reports-container">
      <h2>📋 My Rescue Reports</h2>
      
      {error && <div className="error-message">{error}</div>}
      
      {reports.length === 0 ? (
        <div className="no-reports">
          <p>You haven't reported any animals yet.</p>
          <Link to="/report-animal">
            <button className="btn">Report an Animal</button>
          </Link>
        </div>
      ) : (
        <div className="reports-grid">
          {reports.map((report) => (
            <div key={report.reportId} className="report-card">
              <div className="report-header">
                <h3>{report.animalType}</h3>
                {getStatusBadge(report.status)}
              </div>
              <div className="report-body">
                <p><strong>Condition:</strong> {report.condition}</p>
                <p><strong>Location:</strong> {report.locationAddress || "Address not provided"}</p>
                <p><strong>Reported:</strong> {new Date(report.createdAt).toLocaleString()}</p>
                {report.isEmergency && <p className="emergency-tag">🚨 EMERGENCY</p>}
                {report.assignedNGOName && (
                  <p><strong>Assigned to:</strong> {report.assignedNGOName}</p>
                )}
              </div>
              <div className="report-footer">
                <Link to={`/report/${report.reportId}`}>
                  <button className="btn-view">View Details</button>
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default MyReports;