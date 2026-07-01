// pages/AdminDashboard.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

function AdminDashboard() {
  const [pendingNGOs, setPendingNGOs] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [allReports, setAllReports] = useState([]);
  const [stats, setStats] = useState({});
  const [activeTab, setActiveTab] = useState("dashboard");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    checkAdminAndFetch();
  }, [activeTab]);

  const checkAdminAndFetch = async () => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user) {
      navigate("/auth");
      return;
    }
    
    if (user.role !== "ADMIN") {
      alert("Access denied. Admin privileges required.");
      navigate("/");
      return;
    }
    
    await fetchData();
  };

  const fetchData = async () => {
    setLoading(true);
    setError("");
    
    try {
      if (activeTab === "dashboard") {
        await fetchStats();
      } else if (activeTab === "ngos") {
        await fetchPendingNGOs();
      } else if (activeTab === "users") {
        await fetchAllUsers();
      } else if (activeTab === "reports") {
        await fetchAllReports();
      }
    } catch (err) {
      setError("Failed to fetch data: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    const response = await fetch("http://localhost:8080/api/admin/stats", {
      credentials: "include"
    });
    const data = await response.json();
    if (response.ok) {
      setStats(data);
    }
  };

  const fetchPendingNGOs = async () => {
    const response = await fetch("http://localhost:8080/api/admin/pending-ngos", {
      credentials: "include"
    });
    const data = await response.json();
    if (response.ok) {
      setPendingNGOs(data);
    } else {
      setError(data.error);
    }
  };

  const fetchAllUsers = async () => {
    const response = await fetch("http://localhost:8080/api/admin/all-users", {
      credentials: "include"
    });
    const data = await response.json();
    if (response.ok) {
      setAllUsers(data);
    } else {
      setError(data.error);
    }
  };

  const fetchAllReports = async () => {
    const response = await fetch("http://localhost:8080/api/admin/all-reports", {
      credentials: "include"
    });
    const data = await response.json();
    if (response.ok) {
      setAllReports(data);
    } else {
      setError(data.error);
    }
  };

  const verifyNGO = async (userId, ngoName) => {
    if (!window.confirm(`Verify ${ngoName}?`)) return;
    
    const response = await fetch(`http://localhost:8080/api/admin/verify-ngo/${userId}`, {
      method: "PUT",
      credentials: "include"
    });
    
    if (response.ok) {
      alert(`${ngoName} verified successfully!`);
      fetchPendingNGOs();
      fetchStats();
    } else {
      const data = await response.json();
      alert(data.error || "Failed to verify NGO");
    }
  };

  const rejectNGO = async (userId, ngoName) => {
    if (!window.confirm(`Reject and delete ${ngoName}? This action cannot be undone.`)) return;
    
    const response = await fetch(`http://localhost:8080/api/admin/reject-ngo/${userId}`, {
      method: "DELETE",
      credentials: "include"
    });
    
    if (response.ok) {
      alert(`${ngoName} rejected and removed.`);
      fetchPendingNGOs();
      fetchStats();
    } else {
      const data = await response.json();
      alert(data.error || "Failed to reject NGO");
    }
  };

  const getStatusBadge = (status) => {
    const colors = {
      PENDING: "badge-pending",
      ASSIGNED: "badge-assigned",
      IN_PROGRESS: "badge-progress",
      RESCUED: "badge-rescued",
      COMPLETED: "badge-completed"
    };
    return <span className={`badge ${colors[status] || "badge-pending"}`}>{status}</span>;
  };

  if (loading && activeTab !== "dashboard") {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="admin-container">
      <h1>👑 Admin Dashboard</h1>
      
      <div className="admin-tabs">
        <button className={activeTab === "dashboard" ? "tab-active" : "tab"} 
                onClick={() => setActiveTab("dashboard")}>
          📊 Dashboard
        </button>
        <button className={activeTab === "ngos" ? "tab-active" : "tab"} 
                onClick={() => setActiveTab("ngos")}>
          🏢 Pending NGOs ({pendingNGOs.length})
        </button>
        <button className={activeTab === "users" ? "tab-active" : "tab"} 
                onClick={() => setActiveTab("users")}>
          👥 Users ({allUsers.length})
        </button>
        <button className={activeTab === "reports" ? "tab-active" : "tab"} 
                onClick={() => setActiveTab("reports")}>
          📋 Reports ({allReports.length})
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Dashboard Tab */}
      {activeTab === "dashboard" && (
        <div className="dashboard-stats">
          <div className="stat-card">
            <div className="stat-value">{stats.totalUsers || 0}</div>
            <div className="stat-label">Total Users</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.totalNGOs || 0}</div>
            <div className="stat-label">Total NGOs</div>
          </div>
          <div className="stat-card warning">
            <div className="stat-value">{stats.pendingNGOs || 0}</div>
            <div className="stat-label">Pending NGO Verifications</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.totalReports || 0}</div>
            <div className="stat-label">Total Rescue Reports</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{stats.pendingReports || 0}</div>
            <div className="stat-label">Pending Rescues</div>
          </div>
          <div className="stat-card success">
            <div className="stat-value">{stats.completedReports || 0}</div>
            <div className="stat-label">Completed Rescues</div>
          </div>
        </div>
      )}

      {/* Pending NGOs Tab */}
      {activeTab === "ngos" && (
        <div className="admin-section">
          <h2>Pending NGO Verifications</h2>
          {pendingNGOs.length === 0 ? (
            <p className="no-data">No pending NGO verifications.</p>
          ) : (
            <div className="admin-table">
              <table>
                <thead>
                  <tr>
                    <th>NGO Name</th>
                    <th>Contact Person</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>City</th>
                    <th>Registration No</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingNGOs.map((ngo) => (
                    <tr key={ngo.userId}>
                      <td><strong>{ngo.ngoName || "N/A"}</strong></td>
                      <td>{ngo.name}</td>
                      <td>{ngo.email}</td>
                      <td>{ngo.phone}</td>
                      <td>{ngo.city || ngo.location || "N/A"}</td>
                      <td>{ngo.registrationNumber || "N/A"}</td>
                      <td className="actions-cell">
                        <button className="btn-verify" onClick={() => verifyNGO(ngo.userId, ngo.ngoName || ngo.name)}>
                          ✅ Verify
                        </button>
                        <button className="btn-reject" onClick={() => rejectNGO(ngo.userId, ngo.ngoName || ngo.name)}>
                          ❌ Reject
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* All Users Tab */}
      {activeTab === "users" && (
        <div className="admin-section">
          <h2>All Users</h2>
          <div className="admin-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Role</th>
                  <th>Email Verified</th>
                  <th>NGO Verified</th>
                  <th>Location</th>
                </tr>
              </thead>
              <tbody>
                {allUsers.map((user) => (
                  <tr key={user.userId}>
                    <td>{user.userId}</td>
                    <td>{user.name}</td>
                    <td>{user.email}</td>
                    <td>{user.phone}</td>
                    <td><span className={`role-badge role-${user.role}`}>{user.role}</span></td>
                    <td>{user.isEmailVerified ? "✅" : "❌"}</td>
                    <td>{user.isNGOVerified ? "✅" : "❌"}</td>
                    <td>{user.location || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* All Reports Tab */}
      {activeTab === "reports" && (
        <div className="admin-section">
          <h2>All Rescue Reports</h2>
          <div className="admin-table">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Animal</th>
                  <th>Condition</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Reported By</th>
                  <th>Emergency</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {allReports.map((report) => (
                  <tr key={report.reportId}>
                    <td>{report.reportId}</td>
                    <td>{report.animalType}</td>
                    <td>{report.condition}</td>
                    <td>{report.locationAddress || "-"}</td>
                    <td>{getStatusBadge(report.status)}</td>
                    <td>{report.reporterName}</td>
                    <td>{report.isEmergency ? "🚨 YES" : "NO"}</td>
                    <td>{new Date(report.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;