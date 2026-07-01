// src/api.js - COMPLETE VERSION
const API_BASE_URL = "http://localhost:8080/api";

export const api = {
  // Auth endpoints
  signup: async (userData) => {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(userData),
    });
    return response;
  },

  ngoSignup: async (ngoData) => {
    const response = await fetch(`${API_BASE_URL}/auth/ngo/signup`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(ngoData),
    });
    return response;
  },

  sendOtp: async (email) => {
    const response = await fetch(`${API_BASE_URL}/auth/send-otp`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });
    return response;
  },

  verifyOtp: async (email, otp) => {
    const response = await fetch(`${API_BASE_URL}/auth/verify-otp`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, otp }),
    });
    return response;
  },

  login: async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify(credentials),
    });
    return response;
  },

  logout: async () => {
    const response = await fetch(`${API_BASE_URL}/auth/logout`, {
      method: "POST",
      credentials: "include",
    });
    return response;
  },

  getCurrentUser: async () => {
    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      credentials: "include",
    });
    return response;
  },

  // Rescue endpoints
  reportAnimal: async (formData) => {
    const response = await fetch(`${API_BASE_URL}/rescue/report`, {
      method: "POST",
      credentials: "include",
      body: formData, // Don't set Content-Type, let browser set it with boundary
    });
    return response;
  },

  getMyReports: async () => {
    const response = await fetch(`${API_BASE_URL}/rescue/my-reports`, {
      credentials: "include",
    });
    return response;
  },

  getPendingReports: async () => {
    const response = await fetch(`${API_BASE_URL}/rescue/pending-reports`, {
      credentials: "include",
    });
    return response;
  },

  getNGOAssignedReports: async () => {
    const response = await fetch(`${API_BASE_URL}/rescue/ngo/assigned-reports`, {
      credentials: "include",
    });
    return response;
  },

  claimReport: async (reportId) => {
    const response = await fetch(`${API_BASE_URL}/rescue/report/${reportId}/claim`, {
      method: "POST",
      credentials: "include",
    });
    return response;
  },

  updateReportStatus: async (reportId, status, notes) => {
    const response = await fetch(`${API_BASE_URL}/rescue/report/${reportId}/status`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ status, notes }),
    });
    return response;
  },

  getReportById: async (reportId) => {
    const response = await fetch(`${API_BASE_URL}/rescue/report/${reportId}`, {
      credentials: "include",
    });
    return response;
  },

  getNearbyNGOs: async (latitude, longitude, radius = 10) => {
    const response = await fetch(`${API_BASE_URL}/rescue/nearby-ngos`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ latitude, longitude, radius }),
    });
    return response;
  },

  // Chatbot endpoint
  sendChatMessage: async (message) => {
    const response = await fetch(`${API_BASE_URL}/chatbot/message`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message }),
    });
    return response;
  },
};