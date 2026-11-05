<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String registered = request.getParameter("registered");
    String roleParam = request.getParameter("role");
    String loggedOut = request.getParameter("loggedOut");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login - MediCare Diagnostic Center</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>

  <nav class="navbar">
    <a href="index.html" class="nav-brand">
      <div class="brand-icon">+</div>
      <div>
        <div class="brand-title">Medi<span>Care</span></div>
        <div class="brand-subtitle">Diagnostic Center System</div>
      </div>
    </a>
    <ul class="nav-links">
      <li><a href="index.html">Home</a></li>
      <li><a href="register" class="btn btn-primary btn-sm">Register</a></li>
    </ul>
  </nav>

  <div class="container">
    <div class="auth-wrapper">
      <div class="auth-header">
        <h2>Sign In to MediCare</h2>
        <p style="color: var(--slate-500); font-size: 0.92rem; margin-top: 0.35rem;">Enter your email and password to access your dashboard</p>
      </div>

      <% if ("true".equals(registered)) { %>
        <div class="alert alert-success" style="background-color: #ecfdf5; border: 1.5px solid #a7f3d0; color: #065f46; padding: 1rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem;">
          ✓ <strong>Your account has been created successfully. Please log in to continue.</strong>
        </div>
      <% } %>

      <% if ("true".equals(loggedOut)) { %>
        <div class="alert alert-success">
          You have been successfully signed out.
        </div>
      <% } %>

      <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <div class="alert alert-danger">
          ⚠ <%= errorMessage %>
        </div>
      <% } %>

      <form action="login" method="POST" id="loginForm">
        <div class="form-group">
          <label for="email">Email Address *</label>
          <input type="email" name="email" id="email" class="form-control" placeholder="name@example.com" required autofocus>
        </div>

        <div class="form-group">
          <label for="password">Password *</label>
          <div class="password-container">
            <input type="password" name="password" id="password" class="form-control" placeholder="Enter your password" required>
            <button type="button" class="password-toggle-btn" data-target="password"></button>
          </div>
        </div>

        <button type="submit" class="btn btn-primary btn-block" style="margin-top: 1.5rem; padding: 0.85rem;">Sign In to Dashboard</button>
      </form>

      <div style="text-align: center; margin-top: 1.75rem; font-size: 0.92rem; color: var(--slate-600);">
        New user? <a href="register" style="color: var(--primary); font-weight: 700; text-decoration: none;">Create Patient or Admin Account</a>
      </div>
    </div>
  </div>

  <footer class="footer">
    <div class="footer-bottom">&copy; 2026 MediCare Diagnostic Center System.</div>
  </footer>

  <script src="js/app.js"></script>
</body>
</html>