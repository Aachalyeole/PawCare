// App.js - COMPLETE WITH ALL ROUTES
import React from "react";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import "./App.css";
import Home from "./Home";
import About from "./About_us";
import Auth from "./Auth";
import ChatBot from "./ChatBot";
import ReportAnimal from "./ReportAnimal";
import MyReports from "./MyReports";
import ReportDetails from "./ReportDetails";
import AdminDashboard from "./AdminDashboard";
import PendingReports from "./PendingReports";
import NGOAssignedReports from "./NGOAssignedReports";
import FindNearbyNGOs from "./FindNearbyNGOs";
import VetDoctors from "./VetDoctors";

function App() {
  const user = JSON.parse(localStorage.getItem("user"));

  return (
    <Router>
      <div>
        {/* Navbar */}
        <nav className="navbar">
          <Link to="/" style={{ textDecoration: "none", color: "white" }}>
            <h2 className="logo">🐾 PawCare</h2>
          </Link>
          <ul className="nav-links">
            <li>
              <Link to="/about_us">About Us</Link>
            </li>
            <li>
              <Link to="/">🏠 Home</Link>
            </li>
            <li>
    <Link to="/vet-doctors">🩺 Vet Doctors</Link>
  </li>
            
{user && user.role === "ADMIN" && (
  <li>
    <Link to="/admin">👑 Admin</Link>
  </li>
)}
            {user && (
              <>
                <li>
                  <Link to="/my-reports">My Reports</Link>
                </li>
                {user.role === "USER" && (
                  <li>
                    <Link to="/report-animal">Report Animal</Link>
                  </li>
                )}
              </>
            )}
          </ul>
        </nav>

        {/* Routes */}
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/about_us" element={<About />} />
          <Route path="/auth" element={<Auth />} />
          <Route path="/report-animal" element={<ReportAnimal />} />
          <Route path="/my-reports" element={<MyReports />} />
          <Route path="/report/:id" element={<ReportDetails />} />
          <Route path="/admin" element={<AdminDashboard />} />
          <Route path="/pending-reports" element={<PendingReports />} />
          <Route path="/ngo-assigned" element={<NGOAssignedReports />} />
          <Route path="/nearby-ngos" element={<FindNearbyNGOs />} />
          <Route path="/vet-doctors" element={<VetDoctors />} />
        </Routes>
        <ChatBot />
      </div>
    </Router>
  );
}

export default App;