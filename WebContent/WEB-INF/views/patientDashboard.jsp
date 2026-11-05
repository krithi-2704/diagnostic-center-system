<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList, com.medicare.model.Appointment, com.medicare.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Appointment> allAppointments = (List<Appointment>) request.getAttribute("appointmentsList");
    List<Appointment> activeList = new ArrayList<>();
    List<Appointment> historyList = new ArrayList<>();

    if (allAppointments != null) {
        for (Appointment a : allAppointments) {
            if ("COMPLETED".equalsIgnoreCase(a.getStatus()) || "CANCELLED".equalsIgnoreCase(a.getStatus())) {
                historyList.add(a);
            } else {
                activeList.add(a);
            }
        }
    }

    String bookedRef = request.getParameter("bookedRef");
    String cancelled = request.getParameter("cancelled");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Patient Portal - MediCare</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>

  <nav class="navbar">
    <a href="dashboard" class="nav-brand">
      <div class="brand-icon">+</div>
      <div>
        <div class="brand-title">Medi<span>Care</span></div>
        <div class="brand-subtitle">Patient Portal</div>
      </div>
    </a>
    <ul class="nav-links">
      <li><a href="#active-section">Active Bookings</a></li>
      <li><a href="#history-section">Test History & Reports</a></li>
      <li><a href="#catalog-section">Schedule Tests</a></li>
      <li>
        <button class="cart-trigger" onclick="openCartDrawer()">
          🛒 Cart <span class="cart-badge" id="cartItemCountBadge">0</span>
        </button>
      </li>
      <li><span style="font-weight: 700; color: var(--slate-900);">👤 <%= currentUser.getFullName() %></span></li>
      <li><a href="logout" class="btn btn-outline btn-sm">Sign Out</a></li>
    </ul>
  </nav>

  <main class="container">
    
    <% if (bookedRef != null && !bookedRef.isEmpty()) { %>
      <div class="alert alert-success">
        <div>🎉 <strong>Appointment Confirmed!</strong> Reference: <strong><%= bookedRef %></strong>. Scheduled successfully.</div>
      </div>
    <% } %>

    <% if ("true".equals(cancelled)) { %>
      <div class="alert alert-success">
        <div>✓ <strong>Appointment Cancelled.</strong> Your simulated refund/payment cancellation has been recorded.</div>
      </div>
    <% } %>

    <% if (error != null && !error.isEmpty()) { %>
      <div class="alert alert-danger">
        <div>⚠ <%= error %></div>
      </div>
    <% } %>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2.5rem; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h2 style="font-size: 2rem; color: var(--slate-900); font-weight: 800;">Welcome, <%= currentUser.getFullName() %></h2>
        <p style="color: var(--slate-500); font-size: 0.95rem;">Manage active appointments, view clinical test history, and download certified laboratory reports.</p>
      </div>
      <a href="#catalog-section" class="btn btn-primary">+ Book Diagnostic Test</a>
    </div>

    <!-- 1. Active & Upcoming Appointments -->
    <section id="active-section" class="card" style="padding: 0; overflow: hidden; margin-bottom: 3.5rem;">
      <div style="padding: 1.25rem 1.75rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; background: #ffffff;">
        <div>
          <h3 style="font-size: 1.25rem; font-weight: 800; color: var(--slate-900);">Active / Upcoming Appointments</h3>
          <p style="font-size: 0.85rem; color: var(--slate-500);">Scheduled bookings currently awaiting sample collection or testing</p>
        </div>
        <span class="card-category-tag" style="position: static; background: var(--primary-light); color: var(--primary-dark); font-size: 0.82rem;">
          <%= activeList.size() %> Active
        </span>
      </div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Booking Ref</th>
              <th>Tests / Package</th>
              <th>Bangalore Center</th>
              <th>Mode</th>
              <th>Date & Slot</th>
              <th>Payment</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <% 
              if (!activeList.isEmpty()) { 
                for (Appointment a : activeList) {
                  String statusClass = "status-confirmed";
                  if ("IN_PROGRESS".equalsIgnoreCase(a.getStatus())) statusClass = "status-progress";
            %>
              <tr>
                <td><strong style="color: var(--slate-900); font-family: monospace;"><%= a.getBookingReference() %></strong></td>
                <td><strong><%= a.getItemBooked() %></strong></td>
                <td><%= a.getBranchName() %></td>
                <td>
                  <% if ("HOME_COLLECTION".equalsIgnoreCase(a.getServiceType())) { %>
                    <span style="color: var(--accent); font-weight: 700;">🏠 Home Sample</span>
                  <% } else { %>
                    <span style="color: var(--primary); font-weight: 700;">🏥 Center Visit</span>
                  <% } %>
                </td>
                <td>
                  <div style="font-weight: 600;"><%= a.getAppointmentDate() %></div>
                  <small style="color: var(--slate-500);"><%= a.getTimeSlot() %></small>
                </td>
                <td>
                  <strong>₹<%= String.format("%.2f", a.getTotalAmount()) %></strong><br>
                  <small style="color: var(--slate-500);"><%= a.getPaymentMethod() %> (<%= a.getPaymentStatus() %>)</small>
                </td>
                <td><span class="badge-status <%= statusClass %>"><%= a.getStatus() %></span></td>
                <td>
                  <form action="cancel" method="POST" onsubmit="return confirm('Cancel booking <%= a.getBookingReference() %>?');">
                    <input type="hidden" name="appointmentId" value="<%= a.getAppointmentId() %>">
                    <button type="submit" class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fca5a5;">✕ Cancel</button>
                  </form>
                </td>
              </tr>
            <% 
                } 
              } else { 
            %>
              <tr>
                <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--slate-500);">
                  No active appointments. You can schedule tests from the catalog below.
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 2. Test History & Reports -->
    <section id="history-section" class="card" style="padding: 0; overflow: hidden; margin-bottom: 4rem;">
      <div style="padding: 1.25rem 1.75rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; background: #ffffff;">
        <div>
          <h3 style="font-size: 1.25rem; font-weight: 800; color: var(--slate-900);">Test History & Certified Reports</h3>
          <p style="font-size: 0.85rem; color: var(--slate-500);">Archive of completed tests, downloadable lab reports, and cancelled bookings</p>
        </div>
        <span class="card-category-tag" style="position: static; background: var(--slate-100); color: var(--slate-700); font-size: 0.82rem;">
          <%= historyList.size() %> History Records
        </span>
      </div>

      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Booking Ref</th>
              <th>Tests / Package</th>
              <th>Bangalore Center</th>
              <th>Date Completed</th>
              <th>Payment & Refund</th>
              <th>Status</th>
              <th>Diagnostic Report</th>
            </tr>
          </thead>
          <tbody>
            <% 
              if (!historyList.isEmpty()) { 
                for (Appointment h : historyList) {
                  String statusClass = "COMPLETED".equalsIgnoreCase(h.getStatus()) ? "status-completed" : "status-cancelled";
                  boolean isReportAvailable = "AVAILABLE".equalsIgnoreCase(h.getReportStatus());
            %>
              <tr>
                <td><strong style="font-family: monospace;"><%= h.getBookingReference() %></strong></td>
                <td><strong><%= h.getItemBooked() %></strong></td>
                <td><%= h.getBranchName() %></td>
                <td><%= h.getAppointmentDate() %></td>
                <td>
                  <strong>₹<%= String.format("%.2f", h.getTotalAmount()) %></strong><br>
                  <% if ("REFUND_INITIATED".equalsIgnoreCase(h.getPaymentStatus())) { %>
                    <span style="color: var(--warning); font-size: 0.8rem; font-weight: 700;">🔄 Refund Initiated</span>
                  <% } else if ("NO_REFUND_REQUIRED".equalsIgnoreCase(h.getPaymentStatus())) { %>
                    <span style="color: var(--slate-500); font-size: 0.8rem;">Pay at Center</span>
                  <% } else { %>
                    <span style="color: var(--success); font-size: 0.8rem;">Paid via <%= h.getPaymentMethod() %></span>
                  <% } %>
                </td>
                <td><span class="badge-status <%= statusClass %>"><%= h.getStatus() %></span></td>
                <td>
                  <% if ("COMPLETED".equalsIgnoreCase(h.getStatus())) { %>
                    <% if (h.isContainsImaging()) { %>
                      <div style="font-size: 0.85rem; color: #b45309; font-weight: 700;">
                        🏥 Physical Films: Collect from <%= h.getBranchName() %>
                      </div>
                    <% } else { %>
                      <% if (isReportAvailable) { %>
                        <a href="downloadReport?appointmentId=<%= h.getAppointmentId() %>" target="_blank" class="btn btn-primary btn-sm" style="padding: 0.35rem 0.75rem; text-decoration: none;">📥 Download Report</a>
                      <% } else { %>
                        <span style="color: var(--slate-500); font-size: 0.85rem; font-weight: 600; background: var(--slate-100); padding: 0.35rem 0.65rem; border-radius: 4px; display: inline-block;">
                          ⏳ No report available yet
                        </span>
                      <% } %>
                    <% } %>
                  <% } else { %>
                    <span style="color: var(--slate-400); font-size: 0.85rem;">Booking Cancelled</span>
                  <% } %>
                </td>
              </tr>
            <% 
                } 
              } else { 
            %>
              <tr>
                <td colspan="7" style="text-align: center; padding: 2.5rem; color: var(--slate-500);">
                  No past completed appointments found.
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 3. Test Catalog in Dashboard -->
    <section id="catalog-section" style="margin-bottom: 4rem;">
      <div class="section-header">
        <span class="section-badge">Diagnostic Catalog</span>
        <h2>Schedule Individual Tests or Health Packages</h2>
        <p>Select diagnostic pathology tests or radiology screenings across our Bangalore diagnostic centers.</p>
      </div>

      <h3 style="font-size: 1.4rem; font-weight: 800; color: var(--slate-900); margin-bottom: 1.5rem;">Popular Diagnostic Tests</h3>
      <div id="testsGrid" class="grid-4" style="margin-bottom: 3rem;"></div>

      <h3 style="font-size: 1.4rem; font-weight: 800; color: var(--slate-900); margin-bottom: 1.5rem;">Comprehensive Health Packages</h3>
      <div id="packagesGrid" class="grid-3"></div>
    </section>

  </main>

  <!-- Cart Slide-Over Drawer -->
  <div class="cart-overlay" id="cartOverlay" onclick="closeCartDrawer()"></div>
  <aside class="cart-drawer" id="cartDrawer">
    <div class="cart-header">
      <h3>Your Selected Tests</h3>
      <button class="cart-close-btn" onclick="closeCartDrawer()">✕</button>
    </div>
    <div class="cart-body" id="cartItemsList"></div>
    <div class="cart-footer">
      <div class="cart-total-row">
        <span>Total:</span>
        <span id="cartTotalDisplay" style="color: var(--primary);">₹0.00</span>
      </div>
      <button class="btn btn-primary btn-block" id="cartCheckoutBtn" onclick="proceedToCheckout()" disabled>Proceed to Schedule Slot</button>
    </div>
  </aside>

  <!-- Checkout / Slot Selection Modal -->
  <div class="modal-overlay" id="checkoutModal">
    <div class="modal-card" style="max-width: 680px; max-height: 90vh; overflow-y: auto;">
      <button class="modal-close-btn" onclick="closeCheckoutModal()">✕</button>
      <div style="padding: 2rem;">
        <h2 style="font-size: 1.6rem; color: var(--slate-900); margin-bottom: 0.35rem;">Schedule Appointment Slot</h2>
        <p style="color: var(--slate-500); font-size: 0.9rem; margin-bottom: 1.5rem;">Select your Bangalore center, service mode, date, and live slot.</p>

        <form action="book" method="POST" id="checkoutBookingForm">
          <div id="cartHiddenInputs"></div>
          <input type="hidden" name="timeSlot" id="selectedTimeSlotInput" required>

          <div class="form-group">
            <label for="bookingBranchSelect">1. Select Bangalore Center *</label>
            <select name="branchId" id="bookingBranchSelect" class="form-control" required>
              <option value="">-- Choose Center --</option>
            </select>
          </div>

          <div class="form-group">
            <label for="serviceTypeSelect">2. Service Mode *</label>
            <select name="serviceType" id="serviceTypeSelect" class="form-control" onchange="handleServiceTypeChange()" required>
              <option value="LAB_VISIT">Diagnostic Center Visit (At Center)</option>
              <option value="HOME_COLLECTION">Home Sample Collection (+ ₹150 Bangalore Doorstep Fee)</option>
            </select>
            <div id="imagingRestrictionWarning" style="display: none; color: #b45309; font-size: 0.85rem; font-weight: 700; margin-top: 0.4rem;">
              ℹ Your cart contains imaging/radiology tests (X-Ray/Scan). These must be conducted at the diagnostic center.
            </div>
          </div>

          <div class="form-group" id="addressGroup" style="display: none;">
            <label for="collectionAddress">Doorstep Address in Bangalore *</label>
            <textarea name="collectionAddress" id="collectionAddress" class="form-control" rows="2" placeholder="House/Flat No, Street, Landmark, Bangalore Area"></textarea>
          </div>

          <div class="form-group">
            <label for="appointmentDate">3. Appointment Date (Next 14 Days Only) *</label>
            <input type="date" name="appointmentDate" id="appointmentDate" class="form-control" required>
          </div>

          <div class="form-group">
            <label>4. Available Real-Time Slots *</label>
            <div class="slot-grid" id="slotGridContainer">
              <p style="grid-column: 1/-1; text-align: center; color: var(--slate-500); font-size: 0.88rem;">Select center and date to load slots.</p>
            </div>
          </div>

          <div class="form-group">
            <label for="paymentMethod">5. Payment Option *</label>
            <select name="paymentMethod" id="paymentMethod" class="form-control" required>
              <option value="UPI">UPI (Google Pay / PhonePe / Paytm / BHIM)</option>
              <option value="CARD">Debit / Credit Card</option>
              <option value="NET_BANKING">Net Banking</option>
              <option value="PAY_AT_CENTER">Pay at Center (Cash / Card upon visit)</option>
            </select>
          </div>

          <div style="background: var(--slate-50); border: 1px solid var(--border); padding: 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem;">
            <div style="font-weight: 700; font-size: 0.92rem; margin-bottom: 0.5rem; color: var(--slate-800);">Summary:</div>
            <ul id="checkoutItemsSummaryList" style="padding-left: 1.25rem; font-size: 0.88rem; color: var(--slate-600); margin-bottom: 0.75rem;"></ul>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 0.75rem;">
              <span style="font-weight: 800; color: var(--slate-900);">Total Payable Amount:</span>
              <span id="checkoutTotalDisplay" style="font-size: 1.35rem; font-weight: 800; color: var(--primary);">₹0.00</span>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-block" style="padding: 0.9rem; font-size: 1rem;">Confirm & Schedule Appointment</button>
        </form>
      </div>
    </div>
  </div>

  <!-- Test Details Modal -->
  <div class="modal-overlay" id="testDetailsModal">
    <div class="modal-card">
      <button class="modal-close-btn" onclick="closeTestDetailsModal()">✕</button>
      <div style="padding: 2rem;" id="testDetailsBody"></div>
    </div>
  </div>

  <footer class="footer">
    <div class="footer-bottom">&copy; 2026 MediCare Diagnostic Center System.</div>
  </footer>

  <script src="js/app.js"></script>
</body>
</html>