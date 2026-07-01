// Auth.jsx - With OTP verification during login for NGOs
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Auth.css";
import bg from "./bg_pawcare.avif";

function Auth() {
  const [isLogin, setIsLogin] = useState(true);
  const [showOTP, setShowOTP] = useState(false);
  const [showLoginOTP, setShowLoginOTP] = useState(false);
  const [otp, setOtp] = useState("");
  const [tempEmail, setTempEmail] = useState("");
  const [tempPassword, setTempPassword] = useState("");
  const [userRole, setUserRole] = useState("user");
  const [resendMessage, setResendMessage] = useState("");
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    password: "",
    address: "",
    location: "",
    ngoName: "",
    registrationNumber: "",
    description: "",
    latitude: "",
    longitude: "",
    city: "",
    state: "",
    pincode: "",
    emergencyContact: ""
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError("");
  };

  // Resend OTP function
  const resendOTP = async () => {
    const emailToUse = tempEmail || formData.email;
    if (!emailToUse) {
      setError("Please enter email address");
      return;
    }

    setLoading(true);
    setResendMessage("");
    try {
      const response = await fetch("http://localhost:8080/api/auth/send-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: emailToUse })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        setResendMessage("New OTP sent successfully!");
        setTimeout(() => setResendMessage(""), 3000);
        alert("OTP resent! Check console for OTP code.");
      } else {
        setError(data.error || "Failed to resend OTP");
      }
    } catch (err) {
      setError("Server error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Send OTP for signup
  const sendOTP = async () => {
    if (isLogin) {
    // This should not be called during login
    return;
  }
  
    if (!formData.email) {
      setError("Please enter email address");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/send-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: formData.email })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        setTempEmail(formData.email);
        setShowOTP(true);
        alert("OTP sent! Check console for the OTP code.");
      } else if (data.error === "Email already registered") {
        setError("Email already registered. Please login instead.");
      } else {
        setError(data.error || "Failed to send OTP");
      }
    } catch (err) {
      setError("Server error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Send OTP for login (to verify email)
  const sendLoginOTP = async (email, password) => {
    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/auth/send-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email })
      });
      
      const data = await response.json();
      
      if (response.ok) {
        setTempEmail(email);
        setTempPassword(password);
        setShowLoginOTP(true);
        alert("OTP sent to your email! Please check console for OTP code.");
      } else {
        setError(data.error || "Failed to send OTP");
      }
    } catch (err) {
      setError("Server error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Verify OTP and complete login
  const verifyLoginOTP = async () => {
    setLoading(true);
    setError("");

    try {
      // First verify OTP
      const verifyResponse = await fetch("http://localhost:8080/api/auth/verify-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: tempEmail, otp: otp })
      });
      
      const verifyData = await verifyResponse.json();
      
      if (!verifyResponse.ok || !verifyData.verified) {
        setError(verifyData.error || "Invalid or expired OTP");
        setLoading(false);
        return;
      }

      // OTP verified, now complete login
      const response = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ 
          email: tempEmail, 
          password: tempPassword 
        })
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("user", JSON.stringify(data));
        
        if (data.role === "NGO") {
          if (!data.isngoVerified) {
            alert("NGO account pending verification. You'll be notified once approved.");
          } else {
            alert("Welcome NGO! You can now rescue animals.");
          }
        } else {
          alert("Login successful!");
        }
        
        navigate("/");
        window.location.reload();
      } else {
        setError(data.error || "Login failed");
      }
    } catch (err) {
      setError("Server error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // Complete signup after OTP verification
  const completeSignup = async () => {
    setLoading(true);
    setError("");

    try {
      const url = userRole === "ngo" 
        ? "http://localhost:8080/api/auth/ngo/signup"
        : "http://localhost:8080/api/auth/signup";
      
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData)
      });
      
      const data = await response.json();
      
      if (response.ok) {
        alert(data.message || "Registration successful! Please login.");
        setIsLogin(true);
        setShowOTP(false);
        setOtp("");
        setTempEmail("");
        setFormData({
          name: "",
          email: "",
          phone: "",
          password: "",
          address: "",
          location: "",
          ngoName: "",
          registrationNumber: "",
          description: "",
          latitude: "",
          longitude: "",
          city: "",
          state: "",
          pincode: "",
          emergencyContact: ""
        });
      } else {
        setError(data.error || "Signup failed");
      }
    } catch (err) {
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Verify OTP for signup
  const verifyOTP = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await fetch("http://localhost:8080/api/auth/verify-otp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: tempEmail, otp: otp })
      });
      
      const data = await response.json();
      
      if (response.ok && data.verified) {
        await completeSignup();
      } else {
        setError(data.error || "Invalid or expired OTP");
        setShowOTP(false);
      }
    } catch (err) {
      setError("Server error: " + err.message);
    } finally {
      setLoading(false);
    }
  };

 // Handle Login - FIXED error messages
const handleLogin = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError("");

  try {
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ 
        email: formData.email, 
        password: formData.password 
      })
    });

    const data = await response.json();

    if (response.ok) {
      // Store user data
      localStorage.setItem("user", JSON.stringify(data));
      
      // Show role-specific message
      if (data.role === "NGO") {
        if (!data.isngoVerified) {
          alert("NGO account pending verification. You'll be notified once approved.");
        } else {
          alert("Welcome NGO! You can now rescue animals.");
        }
      } else if (data.role === "ADMIN") {
        alert("Welcome Admin!");
      } else {
        alert("Login successful!");
      }
      
      navigate("/");
      window.location.reload();
    } else {
      // FIXED: Show proper login error messages
      if (data.error === "Email already registered") {
        setError("This email is registered. Please login with your password.");
      } else if (data.error && data.error.includes("Email not verified")) {
        setError("Email not verified. Please verify your email.");
        setTempEmail(formData.email);
      } else {
        setError(data.error || "Login failed. Please check your credentials.");
      }
    }
  } catch (err) {
    setError("Server error. Please try again.");
  } finally {
    setLoading(false);
  }
};

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (isLogin && showLoginOTP) {
      verifyLoginOTP();
    } else if (isLogin) {
      handleLogin(e);
    } else if (!showOTP) {
      sendOTP();
    } else {
      verifyOTP();
    }
  };

  const resetToLogin = () => {
    setIsLogin(true);
    setShowOTP(false);
    setShowLoginOTP(false);
    setError("");
    setOtp("");
    setTempEmail("");
    setTempPassword("");
    setResendMessage("");
    setFormData({
      name: "",
      email: "",
      phone: "",
      password: "",
      address: "",
      location: "",
      ngoName: "",
      registrationNumber: "",
      description: "",
      latitude: "",
      longitude: "",
      city: "",
      state: "",
      pincode: "",
      emergencyContact: ""
    });
  };

  const switchToSignup = () => {
    setIsLogin(false);
    setShowOTP(false);
    setShowLoginOTP(false);
    setError("");
    setOtp("");
    setTempEmail("");
    setTempPassword("");
    setResendMessage("");
    setUserRole("user");
    setFormData({
      name: "",
      email: "",
      phone: "",
      password: "",
      address: "",
      location: "",
      ngoName: "",
      registrationNumber: "",
      description: "",
      latitude: "",
      longitude: "",
      city: "",
      state: "",
      pincode: "",
      emergencyContact: ""
    });
  };

  return (
    <div className="auth-container">
      <div className="auth-left" style={{ backgroundImage: `url(${bg})` }}>
        <h1>🐾 PawCare</h1>
        <p>Care for every paw with love ❤️</p>
      </div>
      
      <div className="auth-right">
        <h2>
          {isLogin ? (showLoginOTP ? "Verify OTP" : "Login") : (showOTP ? "Verify OTP" : "Sign Up")}
        </h2>
        
        {error && <div className="error-message">{error}</div>}
        {resendMessage && <div className="success-message">{resendMessage}</div>}

        <form onSubmit={handleSubmit}>
          {/* Email field */}
          {!showLoginOTP && !showOTP && (
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          )}
          
          {/* Password field */}
          {!showLoginOTP && !showOTP && (
            <input
              type="password"
              name="password"
              placeholder="Password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          )}

          {/* Show email during OTP verification */}
          {(showLoginOTP || showOTP) && (
            <div className="otp-email-info">
              <p>Verifying: <strong>{tempEmail}</strong></p>
            </div>
          )}

          {/* OTP input field */}
          {(showLoginOTP || showOTP) && (
            <>
              <input
                type="text"
                placeholder="Enter 6-digit OTP"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                maxLength="6"
                required
                autoFocus
              />
              <p className="otp-note">
                OTP sent to {tempEmail}. Check backend console for OTP code.
              </p>
              <button 
                type="button" 
                onClick={resendOTP} 
                className="resend-btn"
                disabled={loading}
              >
                Resend OTP
              </button>
            </>
          )}

          {/* Signup fields */}
          {!isLogin && !showOTP && (
            <>
              <div className="role-selection">
                <label>
                  <input
                    type="radio"
                    value="user"
                    checked={userRole === "user"}
                    onChange={(e) => setUserRole(e.target.value)}
                  />
                  Regular User
                </label>
                <label>
                  <input
                    type="radio"
                    value="ngo"
                    checked={userRole === "ngo"}
                    onChange={(e) => setUserRole(e.target.value)}
                  />
                  NGO / Rescue Organization
                </label>
              </div>

              <input
                type="text"
                name="name"
                placeholder="Full Name"
                value={formData.name}
                onChange={handleChange}
                required
              />
              <input
                type="tel"
                name="phone"
                placeholder="Phone Number"
                value={formData.phone}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="address"
                placeholder="Address (Optional)"
                value={formData.address}
                onChange={handleChange}
              />
              <input
                type="text"
                name="location"
                placeholder="City/Location (Optional)"
                value={formData.location}
                onChange={handleChange}
              />
              
              {userRole === "ngo" && (
                <>
                  <input
                    type="text"
                    name="ngoName"
                    placeholder="NGO Name"
                    value={formData.ngoName}
                    onChange={handleChange}
                    required
                  />
                  <input
                    type="text"
                    name="registrationNumber"
                    placeholder="Registration Number"
                    value={formData.registrationNumber}
                    onChange={handleChange}
                    required
                  />
                  <textarea
                    name="description"
                    placeholder="NGO Description"
                    value={formData.description}
                    onChange={handleChange}
                    rows="3"
                  />
                  <input
                    type="text"
                    name="city"
                    placeholder="City"
                    value={formData.city}
                    onChange={handleChange}
                    required
                  />
                  <input
                    type="text"
                    name="state"
                    placeholder="State"
                    value={formData.state}
                    onChange={handleChange}
                    required
                  />
                  <input
                    type="text"
                    name="emergencyContact"
                    placeholder="Emergency Contact Number"
                    value={formData.emergencyContact}
                    onChange={handleChange}
                  />
                  <div className="location-note">
                    <small>📍 NGO location will be used to find nearby rescue requests</small>
                  </div>
                </>
              )}
            </>
          )}

          <button type="submit" disabled={loading}>
            {loading ? "Processing..." : (
              isLogin ? (showLoginOTP ? "Verify & Login" : "Login") : (showOTP ? "Verify & Complete" : "Send OTP")
            )}
          </button>
        </form>

        <p className="toggle-text">
          {isLogin ? "Don't have an account?" : "Already have an account?"}
          <span onClick={isLogin ? switchToSignup : resetToLogin}>
            {isLogin ? " Sign Up" : " Login"}
          </span>
        </p>
      </div>
    </div>
  );
}

export default Auth;