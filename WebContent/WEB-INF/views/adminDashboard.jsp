<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.medicare.model.Appointment, com.medicare.model.Branch, com.medicare.model.SlotAvailability, com.medicare.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    String userRole = (String) session.getAttribute("userRole");
    if (currentUser == null || !"BRANCH_ADMIN".equalsIgnoreCase(userRole)) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    Branch branch = (Branch) request.getAttribute("assignedBranch");
    List<Appointment> appointments = (List<Appointment>) request.getAttribute("appointmentsList");
    List<SlotAvailability> branchSlots = (List<SlotAvailability>) request.getAttribute("branchSlots");
    int[] stats = (int[]) request.getAttribute("stats");
    if (stats == null) stats = new int[]{0, 0, 0, 0, 0};
    String reportUploaded = request.getParameter("reportUploaded");
    String updated = request.getParameter("updated");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Branch Administrator - MediCare</title>
  <link rel="stylesheet" href="css/style.css">
  <script>
    // Live filter function to search table rows by Patient ID, Booking Ref, or Patient Name
    function filterPatientQueue() {
      const input = document.getElementById("queueSearchInput").value.toLowerCase().trim();
      const rows = document.querySelectorAll("#appointmentsTableBody tr");

      rows.forEach(row => {
        const text = row.innerText.toLowerCase();
        if (text.includes(input)) {
          row.style.display = "";
        } else {
          row.style.display = "none";
        }
      });
    }
  </script>
</head>
<body>

  <nav class="navbar">
    <a href="admin" class="nav-brand">
      <div class="brand-icon">⚙</div>
      <div>
        <div class="brand-title">Medi<span>Care</span></div>
        <div class="brand-subtitle">Branch Administration</div>
      </div>
    </a>
    <ul class="nav-links">
      <li><span class="card-category-tag" style="position: static; background: var(--primary-light); color: var(--primary-dark); font-size: 0.85rem;">🏢 <%= (branch != null ? branch.getBranchName() : "Bangalore Center") %></span></li>
      <li><span style="font-weight: 700; color: var(--slate-900);">👤 <%= currentUser.getFullName() %></span></li>
      <li><a href="logout" class="btn btn-outline btn-sm">Sign Out</a></li>
    </ul>
  </nav>

  <main class="container">
    
    <% if ("true".equals(reportUploaded)) { %>
      <div class="alert alert-success">✓ Diagnostic report successfully uploaded and linked to the patient appointment!</div>
    <% } %>

    <% if ("true".equals(updated)) { %>
      <div class="alert alert-success">✓ Appointment status / branch settings updated.</div>
    <% } %>

    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2.5rem; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h2 style="font-size: 2rem; color: var(--slate-900); font-weight: 800;"><%= (branch != null ? branch.getBranchName() : "Center Admin") %></h2>
        <p style="color: var(--slate-500); font-size: 0.95rem;">
          📍 <%= (branch != null ? branch.getAddress() : "") %> | 📞 <%= (branch != null ? branch.getContactPhone() : "") %>
        </p>
      </div>
      <span class="badge-status status-confirmed" style="padding: 0.55rem 1.15rem; font-size: 0.82rem;">
        🔒 Server-Side Branch Isolation Active
      </span>
    </div>

    <!-- Stats -->
    <div class="grid-4" style="margin-bottom: 2.5rem;">
      <div class="card" style="border-left: 4px solid var(--primary); padding: 1.5rem;">
        <p style="font-size: 0.8rem; color: var(--slate-500); font-weight: 700; text-transform: uppercase;">TOTAL APPOINTMENTS</p>
        <div style="font-size: 2.25rem; font-weight: 800; color: var(--slate-900);"><%= stats[0] %></div>
      </div>
      <div class="card" style="border-left: 4px solid var(--accent); padding: 1.5rem;">
        <p style="font-size: 0.8rem; color: var(--slate-500); font-weight: 700; text-transform: uppercase;">CENTER VISITS</p>
        <div style="font-size: 2.25rem; font-weight: 800; color: var(--accent);"><%= stats[1] %></div>
      </div>
      <div class="card" style="border-left: 4px solid #f59e0b; padding: 1.5rem;">
        <p style="font-size: 0.8rem; color: var(--slate-500); font-weight: 700; text-transform: uppercase;">HOME COLLECTIONS</p>
        <div style="font-size: 2.25rem; font-weight: 800; color: #b45309;"><%= stats[2] %></div>
      </div>
      <div class="card" style="border-left: 4px solid var(--success); padding: 1.5rem;">
        <p style="font-size: 0.8rem; color: var(--slate-500); font-weight: 700; text-transform: uppercase;">COMPLETED TESTS</p>
        <div style="font-size: 2.25rem; font-weight: 800; color: var(--success);"><%= stats[3] %></div>
      </div>
    </div>

    <!-- Search Bar & Queue Header -->
    <section class="card" style="padding: 0; overflow: hidden; margin-bottom: 3rem;">
      <div style="padding: 1.5rem 1.75rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div>
          <h3 style="font-size: 1.25rem; font-weight: 800; color: var(--slate-900);">Patient Queue & Diagnostic Report Management</h3>
          <p style="font-size: 0.85rem; color: var(--slate-500);">Search patient by Name, ID, or Token number to update status and upload reports</p>
        </div>
        <!-- Live Patient Search Box -->
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <input type="text" id="queueSearchInput" onkeyup="filterPatientQueue()" placeholder="🔍 Search Name, Ref #, ID..." class="form-control" style="width: 280px; padding: 0.5rem 0.85rem; font-size: 0.88rem;">
        </div>
      </div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Ref / Token #</th>
              <th>Patient Details</th>
              <th>Tests / Package</th>
              <th>Mode</th>
              <th>Status Control</th>
              <th>Report Upload (PDF / Image)</th>
            </tr>
          </thead>
          <tbody id="appointmentsTableBody">
            <% 
              if (appointments != null && !appointments.isEmpty()) { 
                for (Appointment a : appointments) {
                  String statusClass = "status-confirmed";
                  if ("COMPLETED".equalsIgnoreCase(a.getStatus())) statusClass = "status-completed";
                  else if ("IN_PROGRESS".equalsIgnoreCase(a.getStatus())) statusClass = "status-progress";
                  else if ("CANCELLED".equalsIgnoreCase(a.getStatus())) statusClass = "status-cancelled";
                  boolean isAvailable = "AVAILABLE".equalsIgnoreCase(a.getReportStatus());
            %>
              <tr>
                <td>
                  <strong style="font-family: monospace; font-size: 0.95rem; color: var(--slate-900);"><%= a.getBookingReference() %></strong><br>
                  <small style="color: var(--slate-500);"><%= a.getAppointmentDate() %> (<%= a.getTimeSlot() %>)</small>
                </td>
                <td>
                  <strong><%= (a.getPatientName() != null ? a.getPatientName() : "Patient #" + a.getPatientId()) %></strong><br>
                  <small style="color: var(--slate-500);">Patient ID: #<%= a.getPatientId() %> | 📞 <%= (a.getPatientPhone() != null ? a.getPatientPhone() : "N/A") %></small>
                </td>
                <td><%= a.getItemBooked() %></td>
                <td>
                  <% if ("HOME_COLLECTION".equalsIgnoreCase(a.getServiceType())) { %>
                    <span style="color: var(--accent); font-weight: 700;">🏠 Home Collection</span>
                  <% } else { %>
                    <span style="color: var(--primary); font-weight: 700;">🏥 Center Visit</span>
                  <% } %>
                </td>
                <td>
                  <form action="admin" method="POST" style="display: flex; gap: 0.35rem; align-items: center;">
                    <input type="hidden" name="appointmentId" value="<%= a.getAppointmentId() %>">
                    <select name="status" class="form-control" style="padding: 0.35rem 0.5rem; font-size: 0.82rem; width: auto;">
                      <option value="CONFIRMED" <%= "CONFIRMED".equalsIgnoreCase(a.getStatus()) ? "selected" : "" %>>CONFIRMED</option>
                      <option value="IN_PROGRESS" <%= "IN_PROGRESS".equalsIgnoreCase(a.getStatus()) ? "selected" : "" %>>IN_PROGRESS</option>
                      <option value="COMPLETED" <%= "COMPLETED".equalsIgnoreCase(a.getStatus()) ? "selected" : "" %>>COMPLETED</option>
                      <option value="CANCELLED" <%= "CANCELLED".equalsIgnoreCase(a.getStatus()) ? "selected" : "" %>>CANCELLED</option>
                    </select>
                    <button type="submit" class="btn btn-outline btn-sm" style="padding: 0.35rem 0.65rem;">Update</button>
                  </form>
                  <div style="margin-top: 0.35rem;"><span class="badge-status <%= statusClass %>"><%= a.getStatus() %></span></div>
                </td>
                <td>
                  <% if ("COMPLETED".equalsIgnoreCase(a.getStatus())) { %>
                    <% if (a.isContainsImaging()) { %>
                      <span style="color: #b45309; font-size: 0.82rem; font-weight: 700;">🏥 Physical X-Ray Films (Collect at center)</span>
                    <% } else { %>
                      <!-- File Upload Form -->
                      <form action="uploadReport" method="POST" enctype="multipart/form-data" style="display: flex; flex-direction: column; gap: 0.35rem;">
                        <input type="hidden" name="appointmentId" value="<%= a.getAppointmentId() %>">
                        <div style="display: flex; gap: 0.35rem; align-items: center;">
                          <input type="file" name="reportFile" accept=".pdf,.png,.jpg,.jpeg" required style="font-size: 0.78rem; max-width: 170px;">
                          <button type="submit" class="btn btn-primary btn-sm" style="padding: 0.3rem 0.6rem; font-size: 0.78rem;">Upload</button>
                        </div>
                        <% if (isAvailable) { %>
                          <span style="color: var(--success); font-size: 0.78rem; font-weight: 700;">✓ Report is Live on Patient Portal</span>
                        <% } else { %>
                          <span style="color: var(--slate-400); font-size: 0.78rem;">⏳ No report uploaded yet</span>
                        <% } %>
                      </form>
                    <% } %>
                  <% } else { %>
                    <small style="color: var(--slate-400);">Mark 'COMPLETED' to upload report</small>
                  <% } %>
                </td>
              </tr>
            <% 
                } 
              } else { 
            %>
              <tr><td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--slate-500);">No appointments found for this branch.</td></tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Branch Slot Capacity Management -->
    <section class="card" style="padding: 1.5rem; margin-bottom: 3rem;">
      <h3 style="font-size: 1.25rem; font-weight: 800; color: var(--slate-900); margin-bottom: 0.35rem;">Branch Slot Capacity Management</h3>
      <p style="font-size: 0.88rem; color: var(--slate-500); margin-bottom: 1.5rem;">Configure max patient capacity per time slot. Full slots are automatically hidden from patients.</p>

      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem;">
        <% 
          if (branchSlots != null) {
            for (SlotAvailability s : branchSlots) {
        %>
          <form action="admin" method="POST" style="background: var(--slate-50); border: 1px solid var(--border); padding: 1rem; border-radius: var(--radius-sm); display: flex; justify-content: space-between; align-items: center;">
            <input type="hidden" name="action" value="updateSlot">
            <input type="hidden" name="timeSlot" value="<%= s.getTimeSlot() %>">
            <div>
              <strong style="font-size: 0.92rem; color: var(--slate-900);"><%= s.getTimeSlot() %></strong><br>
              <label style="font-size: 0.8rem; color: var(--slate-500);">Max Capacity: 
                <input type="number" name="capacity" value="<%= s.getMaxCapacity() %>" min="1" max="20" style="width: 50px; padding: 0.2rem; margin-left: 0.25rem;">
              </label>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <label style="font-size: 0.8rem;"><input type="checkbox" name="isActive" <%= s.isAvailable() ? "checked" : "" %>> Active</label>
              <button type="submit" class="btn btn-outline btn-sm" style="padding: 0.3rem 0.6rem;">Update</button>
            </div>
          </form>
        <% 
            }
          }
        %>
      </div>
    </section>

  </main>

  <footer class="footer">
    <div class="footer-bottom">&copy; 2026 MediCare Diagnostic Center System.</div>
  </footer>

</body>
</html>