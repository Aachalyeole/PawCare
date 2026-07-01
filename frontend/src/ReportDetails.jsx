// pages/ReportDetails.jsx
import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";

function ReportDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchReportDetails();
  }, [id]);

  const fetchReportDetails = async () => {
    try {
      const user = JSON.parse(localStorage.getItem("user"));
      if (!user) {
        navigate("/auth");
        return;
      }

      const response = await fetch(`http://localhost:8080/api/rescue/report/${id}?userEmail=${user.email}`, {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        }
      });

      const data = await response.json();

      if (response.ok) {
        setReport(data);
      } else {
        setError(data.error || "Failed to fetch report details");
      }
    } catch (err) {
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

  if (loading) return <div className="loading">Loading report details...</div>;
  if (error) return <div className="error-message">{error}</div>;
  if (!report) return <div className="error-message">Report not found</div>;

  return (
    <div className="report-details-container">
      <button onClick={() => navigate("/my-reports")} className="back-btn">
        ← Back to My Reports
      </button>

      <div className="report-details-card">
        <div className="details-header">
          <h2>🐾 Report #{report.reportId}</h2>
          {getStatusBadge(report.status)}
        </div>

        <div className="details-section">
          <h3>Animal Information</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Animal Type:</label>
              <span>{report.animalType}</span>
            </div>
            <div className="detail-item">
              <label>Condition:</label>
              <span>{report.condition}</span>
            </div>
            <div className="detail-item">
              <label>Emergency:</label>
              <span>{report.isEmergency ? "🚨 YES" : "No"}</span>
            </div>
          </div>
        </div>

        <div className="details-section">
          <h3>Location Details</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Latitude:</label>
              <span>{report.latitude}</span>
            </div>
            <div className="detail-item">
              <label>Longitude:</label>
              <span>{report.longitude}</span>
            </div>
            <div className="detail-item">
              <label>Address:</label>
              <span>{report.locationAddress || "Not provided"}</span>
            </div>
          </div>
        </div>

        <div className="details-section">
          <h3>Description</h3>
          <p className="description-text">{report.description}</p>
        </div>

        <div className="details-section">
          <h3>Contact Information</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Name:</label>
              <span>{report.contactName || report.reporterName}</span>
            </div>
            <div className="detail-item">
              <label>Phone:</label>
              <span>{report.contactPhone || report.reporterPhone}</span>
            </div>
            <div className="detail-item">
              <label>Email:</label>
              <span>{report.reporterEmail}</span>
            </div>
          </div>
        </div>

        {report.assignedNGOName && (
          <div className="details-section">
            <h3>Rescue Assignment</h3>
            <div className="details-grid">
              <div className="detail-item">
                <label>Assigned NGO:</label>
                <span>🏢 {report.assignedNGOName}</span>
              </div>
            </div>
          </div>
        )}

        <div className="details-section">
          <h3>Timeline</h3>
          <div className="details-grid">
            <div className="detail-item">
              <label>Reported:</label>
              <span>{new Date(report.createdAt).toLocaleString()}</span>
            </div>
            {report.updatedAt && (
              <div className="detail-item">
                <label>Last Updated:</label>
                <span>{new Date(report.updatedAt).toLocaleString()}</span>
              </div>
            )}
            {report.resolvedAt && (
              <div className="detail-item">
                <label>Resolved:</label>
                <span>{new Date(report.resolvedAt).toLocaleString()}</span>
              </div>
            )}
          </div>
        </div>

        {report.imageUrls && report.imageUrls.length > 0 && (
          <div className="details-section">
            <h3>Photos</h3>
            <div className="images-gallery">
              {report.imageUrls.map((url, index) => (
                <img 
                  key={index} 
                  src={`http://localhost:8080${url}`} 
                  alt={`Report ${report.reportId} - ${index + 1}`}
                  className="report-image"
                  onClick={() => window.open(`http://localhost:8080${url}`, "_blank")}
                />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default ReportDetails;