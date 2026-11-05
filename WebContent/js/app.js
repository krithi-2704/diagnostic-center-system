/**
 * MediCare Diagnostic Center System
 * Client-Side Shopping Cart, Slot Availability & Imaging Rules Engine
 */

let globalCatalog = { tests: [], packages: [] };
let globalBranches = [];
let cart = JSON.parse(localStorage.getItem("medicare_cart") || "[]");

document.addEventListener("DOMContentLoaded", function () {
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.has("bookedRef")) {
    cart = [];
    localStorage.removeItem("medicare_cart");
  }

  initSVGPasswordToggles();
  initFAQAccordion();
  initDateConstraints();
  loadDataFeeds();
  renderCart();
  bindCheckoutFormSubmit();

  if (urlParams.get("openCart") === "true") {
    openCartDrawer();
  }
});

function bindCheckoutFormSubmit() {
  const form = document.getElementById("checkoutBookingForm");
  if (form) {
    form.addEventListener("submit", function () {
      localStorage.removeItem("medicare_cart");
      cart = [];
    });
  }
}

// 1. Password Visibility
const SVG_EYE = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
const SVG_EYE_OFF = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;

function initSVGPasswordToggles() {
  document.querySelectorAll(".password-toggle-btn").forEach((btn) => {
    btn.innerHTML = SVG_EYE;
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      const targetId = this.getAttribute("data-target");
      const input = document.getElementById(targetId);
      if (input) {
        if (input.type === "password") {
          input.type = "text";
          this.innerHTML = SVG_EYE_OFF;
        } else {
          input.type = "password";
          this.innerHTML = SVG_EYE;
        }
      }
    });
  });
}

// 2. Date Constraints (Today -> Next 14 Days Only)
function initDateConstraints() {
  const dateInput = document.getElementById("appointmentDate");
  if (!dateInput) return;

  const today = new Date();
  const maxDate = new Date();
  maxDate.setDate(today.getDate() + 14);

  const formatDate = (d) => d.toISOString().split("T")[0];
  dateInput.min = formatDate(today);
  dateInput.max = formatDate(maxDate);
  dateInput.value = formatDate(today);

  dateInput.addEventListener("change", fetchAvailableSlots);
}

// 3. Load Data Feeds
function loadDataFeeds() {
  fetch("data/branches.json")
    .then((res) => res.json())
    .then((data) => {
      globalBranches = data;
      renderBranches(data);
      populateBranchOptions(data);
    })
    .catch((err) => console.log("Branches loaded locally"));

  fetch("data/tests.json")
    .then((res) => res.json())
    .then((data) => {
      globalCatalog = data;
      renderTests(data.tests || []);
      renderPackages(data.packages || []);
    })
    .catch((err) => console.log("Catalog loaded locally"));
}

function renderBranches(branches) {
  const container = document.getElementById("branchesGrid");
  if (!container) return;

  container.innerHTML = branches
    .map(
      (b) => `
      <div class="card">
        <div class="card-img-wrap">
          <img src="${b.image}" alt="${b.name}" loading="lazy">
          <span class="card-category-tag">Bangalore Center</span>
        </div>
        <div class="card-body">
          <h3 class="card-title">${b.name}</h3>
          <p class="card-desc" style="margin-bottom: 0.5rem;">📍 ${b.address}</p>
          <div class="card-meta">
            <div>⏰ <strong>Hours:</strong> ${b.hours}</div>
            <div style="margin-top: 0.25rem;">📞 <strong>Desk:</strong> ${b.phone}</div>
          </div>
          <button class="btn btn-outline btn-sm btn-block" onclick="selectBranch('${b.branchId}')">Book at this Branch</button>
        </div>
      </div>
    `
    )
    .join("");
}

function renderTests(tests) {
  const container = document.getElementById("testsGrid");
  if (!container) return;

  container.innerHTML = tests
    .map(
      (t) => `
      <div class="card">
        <div class="card-img-wrap">
          <img src="${t.image}" alt="${t.name}" loading="lazy">
          <span class="card-category-tag">${t.category}</span>
          ${t.code === "XRY07" ? `<span class="card-discount-tag" style="background: var(--slate-900);">Center Only (Imaging)</span>` : ""}
        </div>
        <div class="card-body">
          <h3 class="card-title">${t.name}</h3>
          <p class="card-desc">${t.description}</p>
          <div class="card-meta">
            <div>⏱ <strong>Turnaround:</strong> ${t.turnaround}</div>
            <div style="margin-top: 0.25rem;">📋 <strong>Prep:</strong> ${t.instructions}</div>
          </div>
          <div class="card-footer">
            <div class="card-price">₹${t.price.toFixed(2)}</div>
            <div style="display: flex; gap: 0.5rem;">
              <button class="btn btn-outline btn-sm" onclick="viewTestDetails('TEST', ${t.testId})">Details</button>
              <button class="btn btn-primary btn-sm" onclick="addToCart('TEST', ${t.testId}, '${t.name.replace(/'/g, "\\'")}', ${t.price}, ${t.code === "XRY07"})">+ Add</button>
            </div>
          </div>
        </div>
      </div>
    `
    )
    .join("");
}

function renderPackages(packages) {
  const container = document.getElementById("packagesGrid");
  if (!container) return;

  container.innerHTML = packages
    .map(
      (p) => `
      <div class="card" style="border: 1.5px solid var(--primary-border);">
        <div class="card-img-wrap" style="height: 200px;">
          <img src="${p.image}" alt="${p.name}" loading="lazy">
          <span class="card-category-tag">${p.category}</span>
          <span class="card-discount-tag">${p.discount}</span>
        </div>
        <div class="card-body">
          <h3 class="card-title">${p.name}</h3>
          <p class="card-desc">${p.description}</p>
          <div class="card-meta">
            <div><strong>Includes:</strong> ${p.includes}</div>
            <div style="margin-top: 0.35rem; color: var(--primary-dark);">💡 <strong>Instructions:</strong> ${p.instructions}</div>
          </div>
          <div class="card-footer">
            <div>
              <span class="card-price">₹${p.price.toFixed(2)}</span>
              <span class="card-price-strike">₹${p.originalPrice.toFixed(2)}</span>
            </div>
            <div style="display: flex; gap: 0.5rem;">
              <button class="btn btn-outline btn-sm" onclick="viewTestDetails('PACKAGE', ${p.packageId})">Details</button>
              <button class="btn btn-primary btn-sm" onclick="addToCart('PACKAGE', ${p.packageId}, '${p.name.replace(/'/g, "\\'")}', ${p.price}, false)">+ Add</button>
            </div>
          </div>
        </div>
      </div>
    `
    )
    .join("");
}

function populateBranchOptions(branches) {
  const select = document.getElementById("bookingBranchSelect");
  if (!select) return;
  select.innerHTML = `<option value="">-- Choose Bangalore Center --</option>` +
    branches.map((b) => `<option value="${b.branchId}">${b.name} (${b.area})</option>`).join("");
  select.addEventListener("change", fetchAvailableSlots);
}

// 4. Cart Logic with Imaging Check
window.addToCart = function (type, id, name, price, isImaging) {
  const existing = cart.find((item) => item.type === type && item.id === id);
  if (existing) {
    alert(`"${name}" is already in your cart.`);
  } else {
    cart.push({ type, id, name, price, isImaging: !!isImaging });
    saveCart();
    renderCart();
    openCartDrawer();
  }
};

window.removeFromCart = function (index) {
  cart.splice(index, 1);
  saveCart();
  renderCart();
};

function saveCart() {
  localStorage.setItem("medicare_cart", JSON.stringify(cart));
}

function renderCart() {
  const badge = document.getElementById("cartItemCountBadge");
  const drawerBody = document.getElementById("cartItemsList");
  const totalDisplay = document.getElementById("cartTotalDisplay");
  const checkoutBtn = document.getElementById("cartCheckoutBtn");

  if (badge) badge.textContent = cart.length;
  if (!drawerBody) return;

  if (cart.length === 0) {
    drawerBody.innerHTML = `<p style="text-align: center; color: var(--slate-500); padding: 2rem;">Your cart is currently empty.</p>`;
    if (totalDisplay) totalDisplay.textContent = "₹0.00";
    if (checkoutBtn) checkoutBtn.disabled = true;
    return;
  }

  let total = 0;
  drawerBody.innerHTML = cart
    .map((item, idx) => {
      total += item.price;
      return `
      <div class="cart-item">
        <div>
          <div class="cart-item-title">${item.name}</div>
          <span style="font-size: 0.75rem; color: var(--slate-500); font-weight: 600;">
            ${item.isImaging ? "🏥 Center Visit Only (Imaging/X-Ray)" : "🩸 Sample Collection Available"}
          </span>
          <div class="cart-item-price">₹${item.price.toFixed(2)}</div>
        </div>
        <button class="cart-item-remove" onclick="removeFromCart(${idx})">✕ Remove</button>
      </div>
    `;
    })
    .join("");

  if (totalDisplay) totalDisplay.textContent = `₹${total.toFixed(2)}`;
  if (checkoutBtn) checkoutBtn.disabled = false;

  updateCheckoutSummary();
}

window.openCartDrawer = function () {
  const overlay = document.getElementById("cartOverlay");
  const drawer = document.getElementById("cartDrawer");
  if (overlay && drawer) {
    overlay.classList.add("active");
    drawer.classList.add("active");
  }
};

window.closeCartDrawer = function () {
  const overlay = document.getElementById("cartOverlay");
  const drawer = document.getElementById("cartDrawer");
  if (overlay && drawer) {
    overlay.classList.remove("active");
    drawer.classList.remove("active");
  }
};

window.proceedToCheckout = function () {
  closeCartDrawer();
  const checkoutModal = document.getElementById("checkoutModal");
  if (checkoutModal) {
    checkoutModal.classList.add("active");
    updateCheckoutSummary();
    fetchAvailableSlots();
  }
};

window.closeCheckoutModal = function () {
  const checkoutModal = document.getElementById("checkoutModal");
  if (checkoutModal) checkoutModal.classList.remove("active");
};

// 5. Dynamic Slot Availability Engine
window.fetchAvailableSlots = function () {
  const branchSelect = document.getElementById("bookingBranchSelect");
  const dateInput = document.getElementById("appointmentDate");
  const slotGrid = document.getElementById("slotGridContainer");
  const selectedSlotInput = document.getElementById("selectedTimeSlotInput");

  if (!branchSelect || !dateInput || !slotGrid) return;

  const branchId = branchSelect.value;
  const date = dateInput.value;

  if (!branchId || !date) {
    slotGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--slate-500); font-size: 0.9rem; padding: 1rem;">Select a Bangalore Center and valid date to view available slots.</p>`;
    return;
  }

  slotGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--primary); font-size: 0.9rem; padding: 1rem;">Checking real-time lab capacity...</p>`;

  fetch(`api/slots?branchId=${branchId}&date=${date}`)
    .then((res) => res.json())
    .then((slots) => {
      if (!slots || slots.length === 0) {
        slotGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--danger); font-size: 0.9rem;">No active slots configured for this date.</p>`;
        return;
      }

      slotGrid.innerHTML = slots
        .map((s) => {
          const isFull = !s.available || s.remaining <= 0;
          return `
          <div class="slot-btn ${isFull ? "disabled" : ""}" onclick="${isFull ? "" : `selectSlot(this, '${s.timeSlot}')`}">
            <div class="slot-time">${s.timeSlot}</div>
            <span class="slot-capacity-pill ${isFull ? "pill-full" : "pill-available"}">
              ${isFull ? "Fully Booked" : `${s.remaining} left`}
            </span>
          </div>
        `;
        })
        .join("");

      if (selectedSlotInput) selectedSlotInput.value = "";
    })
    .catch((err) => {
      slotGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--slate-500);">Slots loaded.</p>`;
    });
};

window.selectSlot = function (el, slotTime) {
  document.querySelectorAll(".slot-btn").forEach((b) => b.classList.remove("selected"));
  el.classList.add("selected");
  const input = document.getElementById("selectedTimeSlotInput");
  if (input) input.value = slotTime;
};

// 6. Enforce Home Sample Collection Restrictions on Imaging
function updateCheckoutSummary() {
  const hiddenContainer = document.getElementById("cartHiddenInputs");
  const checkoutItemsList = document.getElementById("checkoutItemsSummaryList");
  const serviceTypeSelect = document.getElementById("serviceTypeSelect");
  const addressGroup = document.getElementById("addressGroup");
  const checkoutTotalDisplay = document.getElementById("checkoutTotalDisplay");
  const imagingWarning = document.getElementById("imagingRestrictionWarning");

  if (!hiddenContainer) return;

  hiddenContainer.innerHTML = cart
    .map((item) => `<input type="hidden" name="cartItem" value="${item.type}::${item.id}::${item.price.toFixed(2)}::${item.name.replace(/"/g, "&quot;")}">`)
    .join("");

  const hasImagingItem = cart.some((i) => i.isImaging || i.name.toLowerCase().includes("x-ray") || i.name.toLowerCase().includes("mri") || i.name.toLowerCase().includes("scan"));

  if (serviceTypeSelect) {
    const homeOpt = serviceTypeSelect.querySelector('option[value="HOME_COLLECTION"]');
    if (hasImagingItem) {
      if (homeOpt) {
        homeOpt.disabled = true;
        homeOpt.textContent = "Home Collection Unavailable (Cart contains X-Ray/Imaging)";
      }
      serviceTypeSelect.value = "LAB_VISIT";
      if (imagingWarning) imagingWarning.style.display = "block";
    } else {
      if (homeOpt) {
        homeOpt.disabled = false;
        homeOpt.textContent = "Home Sample Collection (+ ₹150 Bangalore Doorstep Fee)";
      }
      if (imagingWarning) imagingWarning.style.display = "none";
    }
  }

  let subtotal = cart.reduce((sum, item) => sum + item.price, 0);
  let homeCharge = (serviceTypeSelect && serviceTypeSelect.value === "HOME_COLLECTION") ? 150.0 : 0.0;

  if (addressGroup) {
    addressGroup.style.display = (homeCharge > 0) ? "block" : "none";
  }

  if (checkoutItemsList) {
    checkoutItemsList.innerHTML = cart.map((i) => `<li>${i.name} — <strong>₹${i.price.toFixed(2)}</strong></li>`).join("");
    if (homeCharge > 0) {
      checkoutItemsList.innerHTML += `<li style="color: var(--primary);">Home Sample Collection Charge — <strong>₹150.00</strong></li>`;
    }
  }

  const finalTotal = subtotal + homeCharge;
  if (checkoutTotalDisplay) checkoutTotalDisplay.textContent = `₹${finalTotal.toFixed(2)}`;
}

window.handleServiceTypeChange = function () {
  updateCheckoutSummary();
};

window.viewTestDetails = function (type, id) {
  const modal = document.getElementById("testDetailsModal");
  const body = document.getElementById("testDetailsBody");
  if (!modal || !body) return;

  let item = (type === "PACKAGE") ? (globalCatalog.packages || []).find((p) => p.packageId === id) : (globalCatalog.tests || []).find((t) => t.testId === id);
  if (!item) return;

  body.innerHTML = `
    <div style="display: flex; gap: 1.5rem; margin-bottom: 1.5rem; align-items: center;">
      <img src="${item.image}" style="width: 120px; height: 120px; object-fit: cover; border-radius: var(--radius-md);" alt="${item.name}">
      <div>
        <span class="card-category-tag" style="position: static; margin-bottom: 0.5rem; display: inline-block;">${item.category}</span>
        <h3 style="font-size: 1.35rem; color: var(--slate-900);">${item.name}</h3>
        <div style="font-size: 1.4rem; font-weight: 800; color: var(--primary); margin-top: 0.25rem;">₹${item.price.toFixed(2)}</div>
      </div>
    </div>
    <div style="background: var(--slate-50); border: 1px solid var(--border); padding: 1.25rem; border-radius: var(--radius-sm); margin-bottom: 1.5rem;">
      <p style="margin-bottom: 0.75rem;">${item.description}</p>
      ${item.parameters ? `<p style="margin-bottom: 0.5rem;"><strong>Parameters:</strong> ${item.parameters}</p>` : ""}
      ${item.includes ? `<p style="margin-bottom: 0.5rem;"><strong>Package Tests:</strong> ${item.includes}</p>` : ""}
      <p style="margin-bottom: 0.5rem;"><strong>Turnaround:</strong> ${item.turnaround || "4-8 Hours"}</p>
      <p><strong>Preparation:</strong> ${item.instructions}</p>
    </div>
    <button class="btn btn-primary btn-block" onclick="addToCart('${type}', ${id}, '${item.name.replace(/'/g, "\\'")}', ${item.price}, ${item.code === "XRY07"}); closeTestDetailsModal();">+ Add to Cart</button>
  `;
  modal.classList.add("active");
};

window.closeTestDetailsModal = function () {
  const modal = document.getElementById("testDetailsModal");
  if (modal) modal.classList.remove("active");
};

function initFAQAccordion() {
  document.querySelectorAll(".accordion-header").forEach((header) => {
    header.addEventListener("click", function () {
      const item = this.parentElement;
      const isActive = item.classList.contains("active");
      document.querySelectorAll(".accordion-item").forEach((i) => i.classList.remove("active"));
      if (!isActive) item.classList.add("active");
    });
  });
}