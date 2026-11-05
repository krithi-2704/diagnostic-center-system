<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Register - MediCare Diagnostic Center</title>
  <link rel="stylesheet" href="css/style.css">
  <script>
    function switchRole(role) {
      document.getElementById('roleInput').value = role;
      if (role === 'PATIENT') {
        document.getElementById('patientFields').style.display = 'block';
        document.getElementById('adminFields').style.display = 'none';
        document.getElementById('tabPatient').classList.add('btn-primary');
        document.getElementById('tabPatient').classList.remove('btn-outline');
        document.getElementById('tabAdmin').classList.add('btn-outline');
        document.getElementById('tabAdmin').classList.remove('btn-primary');
        document.getElementById('submitBtn').textContent = 'Create Patient Account';
      } else {
        document.getElementById('patientFields').style.display = 'none';
        document.getElementById('adminFields').style.display = 'block';
        document.getElementById('tabAdmin').classList.add('btn-primary');
        document.getElementById('tabAdmin').classList.remove('btn-outline');
        document.getElementById('tabPatient').classList.add('btn-outline');
        document.getElementById('tabPatient').classList.remove('btn-primary');
        document.getElementById('submitBtn').textContent = 'Register as Branch Administrator';
      }
    }
  </script>
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
      <li><a href="login" class="btn btn-outline btn-sm">Sign In</a></li>
    </ul>
  </nav>

  <div class="container">
    <div class="auth-wrapper" style="max-width: 640px;">
      
      <!-- Role Switcher Tabs -->
      <div style="display: flex; gap: 0.5rem; margin-bottom: 2rem;">
        <button type="button" id="tabPatient" class="btn btn-primary" style="flex: 1;" onclick="switchRole('PATIENT')">👤 Register as Patient</button>
        <button type="button" id="tabAdmin" class="btn btn-outline" style="flex: 1;" onclick="switchRole('BRANCH_ADMIN')">🏢 Register as Branch Admin</button>
      </div>

      <div class="auth-header" style="margin-bottom: 1.5rem;">
        <h2>Create Your Account</h2>
        <p style="color: var(--slate-500); font-size: 0.92rem; margin-top: 0.25rem;">Choose your role and enter your details below</p>
      </div>

      <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
        <div class="alert alert-danger">
          ⚠ <%= errorMessage %>
        </div>
      <% } %>

      <form action="register" method="POST" id="registerForm">
        <input type="hidden" name="role" id="roleInput" value="PATIENT">

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
          <div class="form-group">
            <label for="name">Full Name *</label>
            <input type="text" name="name" id="name" class="form-control" placeholder="Gaurav Sharma" required autofocus>
          </div>
          <div class="form-group">
            <label for="email">Email Address *</label>
            <input type="email" name="email" id="email" class="form-control" placeholder="gaurav@example.com" required>
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
          <div class="form-group">
            <label for="password">Password *</label>
            <div class="password-container">
              <input type="password" name="password" id="password" class="form-control" placeholder="Min 6 chars (Aa1@)" required>
              <button type="button" class="password-toggle-btn" data-target="password"></button>
            </div>
          </div>
          <div class="form-group">
            <label for="confirmPassword">Confirm Password *</label>
            <div class="password-container">
              <input type="password" name="confirmPassword" id="confirmPassword" class="form-control" placeholder="Re-enter password" required>
              <button type="button" class="password-toggle-btn" data-target="confirmPassword"></button>
            </div>
          </div>
        </div>

        <!-- Patient Specific Fields -->
        <div id="patientFields">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
            <div class="form-group">
              <label for="phone">Phone Number (10 digits) *</label>
              <input type="tel" name="phone" id="phone" class="form-control" placeholder="9876543210">
            </div>
            <div class="form-group">
              <label for="age">Age *</label>
              <input type="number" name="age" id="age" class="form-control" value="25" min="1" max="120">
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.25rem;">
            <div class="form-group">
              <label for="gender">Gender *</label>
              <select name="gender" id="gender" class="form-control">
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div class="form-group">
              <label for="bloodGroup">Blood Group</label>
              <select name="bloodGroup" id="bloodGroup" class="form-control">
                <option value="O+">O+</option>
                <option value="A+">A+</option>
                <option value="B+">B+</option>
                <option value="AB+">AB+</option>
                <option value="O-">O-</option>
                <option value="A-">A-</option>
                <option value="B-">B-</option>
                <option value="AB-">AB-</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="address">Residential Address *</label>
            <textarea name="address" id="address" class="form-control" rows="2" placeholder="Street, Building, Area, and Pincode"></textarea>
          </div>
        </div>

        <!-- Admin Specific Fields -->
        <div id="adminFields" style="display: none; background: var(--slate-50); border: 1px solid var(--border); padding: 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem;">
          <div class="form-group">
            <label for="branchId">Select Assigned Bangalore Branch *</label>
            <select name="branchId" id="branchId" class="form-control">
              <option value="1">MediCare - Shivajinagar (Central)</option>
              <option value="2">MediCare - Whitefield (East)</option>
              <option value="3">MediCare - MG Road (CBD)</option>
              <option value="4">MediCare - Kamanahalli (North)</option>
            </select>
          </div>
          <div class="form-group">
            <label for="adminPasscode">Administrator Authorization Passcode *</label>
            <input type="password" name="adminPasscode" id="adminPasscode" class="form-control" placeholder="Enter MEDICARE_ADMIN_2026">
            <small style="color: var(--slate-500);">Security code: <strong>MEDICARE_ADMIN_2026</strong></small>
          </div>
        </div>

        <button type="submit" id="submitBtn" class="btn btn-primary btn-block" style="margin-top: 1.5rem; padding: 0.9rem;">Create Patient Account</button>
      </form>

      <div style="text-align: center; margin-top: 1.75rem; font-size: 0.92rem; color: var(--slate-600);">
        Already registered? <a href="login" style="color: var(--primary); font-weight: 700; text-decoration: none;">Sign In Here</a>
      </div>
    </div>
  </div>

  <footer class="footer">
    <div class="footer-bottom">&copy; 2026 MediCare Diagnostic Center System.</div>
  </footer>

  <script src="js/app.js"></script>
</body>
</html>