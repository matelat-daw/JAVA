window.MyIkeaApp = window.MyIkeaApp || {};

window.MyIkeaApp.init = async function initMyIkeaApp() {
  if (window.MyIkeaApp.initialized) {
    return;
  }
  window.MyIkeaApp.initialized = true;

  const API_BASE_URL = window.__API_BASE_URL__ || "/api";
  const AUTH_REFRESH_PATH = "/v1/auth/refresh";
  const AUTH_HINT_KEY = "myikea.auth.hint";

  const state = {
    user: null,
    products: [],
    cart: null,
    orders: [],
    users: [],
    currentView: "catalog"
  };

  const viewWelcome = document.getElementById("view-welcome");
  const viewApp = document.getElementById("view-app");
  const navAuth = document.getElementById("nav-auth");
  const navUser = document.getElementById("nav-user");
  const navUserName = document.getElementById("nav-user-name");
  const navLinks = document.getElementById("nav-links");
  const btnLogout = document.getElementById("btn-logout");

  const navItemCart = document.getElementById("nav-item-cart");
  const navItemOrders = document.getElementById("nav-item-orders");
  const navItemUsers = document.getElementById("nav-item-users");

  const productsContainer = document.getElementById("products-grid");
  const btnOpenCreateProduct = document.getElementById("btn-open-create-product");
  const cartContent = document.getElementById("cart-content");
  const ordersContent = document.getElementById("orders-content");
  const btnCompleteOrder = document.getElementById("btn-complete-order");
  const btnGoCatalogFromCart = document.getElementById("btn-go-catalog-from-cart");
  const usersTableBody = document.getElementById("users-table-body");

  const profilePictureForm = document.getElementById("profile-picture-form");
  const profilePictureInput = document.getElementById("profilePicture");
  const profilePicturePreview = document.getElementById("profile-picture-preview");
  const profileForm = document.getElementById("profile-form");
  const profilePasswordForm = document.getElementById("profile-password-form");

  const deleteProfileModalEl = document.getElementById("deleteProfileModal");
  const deleteProfilePasswordInput = document.getElementById("delete-profile-password");
  const btnConfirmDeleteProfile = document.getElementById("btn-confirm-delete-profile");

  const productDetailsModalEl = document.getElementById("productDetailsModal");
  const productDetailsTitle = document.getElementById("product-details-title");
  const productDetailsImage = document.getElementById("product-details-image");
  const productDetailsPrice = document.getElementById("product-details-price");
  const productDetailsStock = document.getElementById("product-details-stock");
  const productDetailsId = document.getElementById("product-details-id");

  const createProductModalEl = document.getElementById("createProductModal");
  const createProductForm = document.getElementById("create-product-form");
  const createProductProvince = document.getElementById("create-product-province");
  const createProductMunicipality = document.getElementById("create-product-municipality");

  const toastEl = document.getElementById("app-toast");
  const toastBody = document.getElementById("app-toast-body");

  const loginModal = document.getElementById("loginModal");
  const registerModal = document.getElementById("registerModal");
  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");

  let loginModalInstance;
  let registerModalInstance;
  let deleteProfileModalInstance;
  let productDetailsModalInstance;
  let createProductModalInstance;
  let toastInstance;

  const modalOptions = {
    backdrop: "static",
    keyboard: false
  };

  function initBootstrap() {
    loginModalInstance = new bootstrap.Modal(loginModal, modalOptions);
    registerModalInstance = new bootstrap.Modal(registerModal, modalOptions);
    deleteProfileModalInstance = new bootstrap.Modal(deleteProfileModalEl, modalOptions);
    productDetailsModalInstance = new bootstrap.Modal(productDetailsModalEl, modalOptions);
    createProductModalInstance = new bootstrap.Modal(createProductModalEl, modalOptions);
    toastInstance = new bootstrap.Toast(toastEl, { delay: 3500 });
  }

  function showToast(message, kind) {
    const classes = ["text-bg-success", "text-bg-danger", "text-bg-warning", "text-bg-info"];
    classes.forEach((cls) => toastEl.classList.remove(cls));
    toastEl.classList.add(kind || "text-bg-info");
    toastBody.textContent = message;
    toastInstance.show();
  }

  async function tryRefreshToken() {
    try {
      const refreshResponse = await fetch(`${API_BASE_URL}${AUTH_REFRESH_PATH}`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: "{}"
      });
      return refreshResponse.ok;
    } catch (error) {
      return false;
    }
  }

  async function api(path, options, canRetryOn401) {
    const requestOptions = options || {};
    const headers = { ...(requestOptions.headers || {}) };

    if (!(requestOptions.body instanceof FormData) && !headers["Content-Type"]) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
      credentials: "include",
      ...requestOptions,
      headers
    });

    const allowRetry =
      canRetryOn401 !== false &&
      response.status === 401 &&
      path !== AUTH_REFRESH_PATH &&
      path !== "/api/v1/auth/login" &&
      path !== "/api/v1/auth/register";

    if (allowRetry) {
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        return api(path, options, false);
      }
    }

    const isJson = (response.headers.get("content-type") || "").includes("application/json");
    const data = isJson ? await response.json() : null;

    if (!response.ok) {
      const message = data && (data.message || data.error) ? (data.message || data.error) : `HTTP ${response.status}`;
      const error = new Error(message);
      error.status = response.status;
      throw error;
    }

    return data;
  }

  function hasRole(role) {
    return !!(state.user && Array.isArray(state.user.roles) && state.user.roles.includes(role));
  }

  function isManagerOrAdmin() {
    return hasRole("MANAGER") || hasRole("ADMIN");
  }

  function isAdmin() {
    return hasRole("ADMIN");
  }

  function showView(viewName) {
    document.querySelectorAll(".app-view").forEach((section) => {
      section.classList.toggle("d-none", section.dataset.view !== viewName);
    });

    document.querySelectorAll("#nav-links .nav-link").forEach((link) => {
      link.classList.toggle("active", link.dataset.view === viewName);
    });

    state.currentView = viewName;
  }

  function setViewAuthenticated(isAuthenticated) {
    viewWelcome.classList.toggle("d-none", isAuthenticated);
    viewApp.classList.toggle("d-none", !isAuthenticated);
    navAuth.classList.toggle("d-none", isAuthenticated);
    navUser.classList.toggle("d-none", !isAuthenticated);
    navLinks.classList.toggle("d-none", !isAuthenticated);

    if (!isAuthenticated) {
      state.user = null;
      navUserName.textContent = "";
      return;
    }

    navUserName.textContent = `${state.user.username} (${state.user.roles.join(", ")})`;
  }

  function setRoleNav() {
    navItemCart.classList.toggle("d-none", !isManagerOrAdmin());
    navItemOrders.classList.toggle("d-none", !isManagerOrAdmin());
    navItemUsers.classList.toggle("d-none", !isAdmin());
    btnOpenCreateProduct.classList.toggle("d-none", !isManagerOrAdmin());
  }

  function renderProducts() {
    productsContainer.innerHTML = "";

    if (!state.products.length) {
      productsContainer.innerHTML = '<div class="alert alert-secondary mb-0">No hay productos disponibles.</div>';
      return;
    }

    const canBuy = isManagerOrAdmin();

    state.products.forEach((product) => {
      const productImageUrl = resolveProductImageUrl(product.picture);
      const card = document.createElement("div");
      card.className = "col-12 col-md-6 col-lg-4";
      card.innerHTML = `
        <div class="card h-100 shadow-sm border-0">
          <div class="ratio ratio-4x3 bg-body-tertiary rounded-top overflow-hidden d-flex align-items-center justify-content-center p-2">
            <img src="${productImageUrl}" alt="${escapeHtml(product.name || "Articulo")}" class="img-fluid object-fit-contain" />
          </div>
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">${product.name}</h5>
            <p class="text-muted mb-2">Precio: ${product.price ?? 0} EUR</p>
            <p class="text-muted mb-3">Stock: ${product.stock ?? 0}</p>
            <div class="mt-auto d-flex gap-2">
              <button class="btn btn-outline-secondary btn-sm btn-product-details" data-id="${product.id}">Detalles</button>
              ${canBuy ? `<button class="btn btn-primary btn-sm btn-add-cart" data-id="${product.id}">Comprar</button>` : ""}
            </div>
          </div>
        </div>
      `;
      productsContainer.appendChild(card);
    });

    document.querySelectorAll(".btn-product-details").forEach((btn) => {
      btn.addEventListener("click", async () => {
        try {
          const product = await api(`/api/v1/public/products/${btn.dataset.id}`, { method: "GET" });
          productDetailsTitle.textContent = product.name || "Detalles del articulo";
          productDetailsPrice.textContent = `${product.price ?? 0} EUR`;
          productDetailsStock.textContent = `${product.stock ?? 0}`;
          productDetailsId.textContent = `${product.id ?? "-"}`;

          productDetailsImage.src = resolveProductImageUrl(product.picture);
          productDetailsImage.classList.remove("d-none");

          productDetailsModalInstance.show();
        } catch (error) {
          showToast(error.message, "text-bg-danger");
        }
      });
    });

    document.querySelectorAll(".btn-add-cart").forEach((btn) => {
      btn.addEventListener("click", async () => {
        if (!isManagerOrAdmin()) {
          showToast("Solo MANAGER y ADMIN pueden comprar.", "text-bg-warning");
          return;
        }
        try {
          await api(`/api/v1/cart/items/${btn.dataset.id}`, { method: "POST" });
          showToast("Producto agregado al carrito.", "text-bg-success");
          await loadCart();
        } catch (error) {
          showToast(error.message, "text-bg-danger");
        }
      });
    });
  }

  function renderCart() {
    const products = state.cart && Array.isArray(state.cart.products) ? state.cart.products : [];

    if (!products.length) {
      cartContent.innerHTML = '<p class="text-muted mb-0">Tu carrito esta vacio.</p>';
      btnCompleteOrder.classList.add("d-none");
      return;
    }

    const rows = products
      .map(
        (product) => `
      <tr>
        <td>${escapeHtml(product.name || "")}</td>
        <td>${formatMoney(product.price)}</td>
        <td>1</td>
        <td>
          <button class="btn btn-outline-danger btn-sm btn-remove-cart-item" data-id="${product.id}">Eliminar</button>
        </td>
      </tr>
    `
      )
      .join("");

    cartContent.innerHTML = `
      <div class="table-responsive">
        <table class="table table-striped align-middle mb-2">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Precio</th>
              <th>Cantidad</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
      <div class="d-flex justify-content-end gap-2 mt-3">
        <span class="fw-semibold">Total:</span>
        <span>${formatMoney(state.cart.total)}</span>
      </div>
    `;

    btnCompleteOrder.classList.remove("d-none");

    document.querySelectorAll(".btn-remove-cart-item").forEach((btn) => {
      btn.addEventListener("click", async () => {
        try {
          await api(`/api/v1/cart/items/${btn.dataset.id}`, { method: "DELETE" });
          showToast("Producto eliminado del carrito.", "text-bg-success");
          await loadCart();
        } catch (error) {
          showToast(error.message, "text-bg-danger");
        }
      });
    });
  }

  function renderOrders() {
    if (!Array.isArray(state.orders) || !state.orders.length) {
      ordersContent.innerHTML = '<div class="alert alert-warning mb-0">No tienes pedidos completados.</div>';
      return;
    }

    const rows = state.orders
      .map((order) => {
        const productList = (order.products || [])
          .map((product) => `<li>${escapeHtml(product.name || "")}</li>`)
          .join("");

        return `
          <tr>
            <td>${formatDate(order.date)}</td>
            <td>${formatMoney(order.total)}</td>
            <td><ul class="mb-0 ps-3">${productList || "<li>-</li>"}</ul></td>
            <td><span class="badge text-bg-success">Completado</span></td>
          </tr>
        `;
      })
      .join("");

    ordersContent.innerHTML = `
      <div class="table-responsive">
        <table class="table table-striped align-middle mb-0">
          <thead>
            <tr>
              <th>Fecha del pedido</th>
              <th>Total</th>
              <th>Productos</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </div>
    `;
  }

  function formatMoney(value) {
    const amount = typeof value === "number" ? value : Number(value || 0);
    return `${amount.toFixed(2)} EUR`;
  }

  function formatDate(value) {
    if (!value) {
      return "-";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value);
    }
    return new Intl.DateTimeFormat("es-ES", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    }).format(date);
  }

  function escapeHtml(value) {
    return String(value || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function resolveProductImageUrl(picture) {
    if (picture && picture.startsWith("http")) {
      return picture;
    }
    if (picture && picture.startsWith("/")) {
      return `${API_BASE_URL}${picture}`;
    }
    if (picture) {
      return `${API_BASE_URL}/images/${picture}`;
    }
    return `${API_BASE_URL}/images/default.jpg`;
  }

  function renderUsers() {
    usersTableBody.innerHTML = "";
    state.users.forEach((user) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${user.id}</td>
        <td>${user.username}</td>
        <td>${user.email}</td>
        <td>${(user.roles || []).join(", ")}</td>
      `;
      usersTableBody.appendChild(row);
    });
  }

  function renderProfile() {
    if (!state.user) {
      profileForm.reset();
      profilePasswordForm.reset();
      profilePicturePreview.src = profilePictureUrl(null, null);
      return;
    }

    profileForm.username.value = state.user.username || "";
    profileForm.email.value = state.user.email || "";
    profileForm.firstName.value = state.user.firstName || "";
    profileForm.lastName.value = state.user.lastName || "";
    profileForm.phoneNumber.value = state.user.phoneNumber || "";
    profileForm.gender.value = state.user.gender || "female";

    profilePicturePreview.src = profilePictureUrl(state.user.profilePicture, state.user.gender);
  }

  function profilePictureUrl(profilePicture, gender) {
    if (profilePicture && profilePicture.startsWith("http")) {
      return profilePicture;
    }
    if (profilePicture && profilePicture.startsWith("/")) {
      return `${API_BASE_URL}${profilePicture}`;
    }
    if (profilePicture) {
      return `${API_BASE_URL}/images/${profilePicture}`;
    }
    const fallback = gender === "male" ? "male.png" : gender === "other" ? "other.png" : "female.png";
    return `${API_BASE_URL}/images/${fallback}`;
  }

  async function loadProducts() {
    state.products = await api("/api/v1/public/products", { method: "GET" });
    renderProducts();
  }

  async function loadCart() {
    if (!isManagerOrAdmin()) {
      state.cart = null;
      renderCart();
      return;
    }
    state.cart = await api("/api/v1/cart", { method: "GET" });
    renderCart();
  }

  async function loadOrders() {
    if (!isManagerOrAdmin()) {
      state.orders = [];
      renderOrders();
      return;
    }
    state.orders = await api("/api/v1/orders", { method: "GET" });
    renderOrders();
  }

  async function loadUsers() {
    if (!isAdmin()) {
      state.users = [];
      renderUsers();
      return;
    }
    state.users = await api("/api/v1/users", { method: "GET" });
    renderUsers();
  }

  async function loadProfile() {
    state.user = await api("/api/v1/profile", { method: "GET" });
    renderProfile();
  }

  async function loadProvinces() {
    return api("/api/v1/public/provinces", { method: "GET" });
  }

  async function loadMunicipalitiesByProvince(provinceId) {
    const query = provinceId ? `?provinceId=${encodeURIComponent(provinceId)}` : "";
    return api(`/api/v1/public/municipalities${query}`, { method: "GET" });
  }

  async function loadSession() {
    const hasAuthHint = window.localStorage.getItem(AUTH_HINT_KEY) === "1";

    if (!hasAuthHint) {
      state.user = null;
      setViewAuthenticated(false);
      await loadProducts();
      return;
    }

    try {
      state.user = await api("/api/v1/me", { method: "GET" });
      window.localStorage.setItem(AUTH_HINT_KEY, "1");
      setViewAuthenticated(true);
      setRoleNav();

      await loadProducts();
      await loadProfile();
      await loadCart();
      await loadOrders();
      await loadUsers();

      showView("catalog");
    } catch (error) {
      window.localStorage.removeItem(AUTH_HINT_KEY);
      state.user = null;
      setViewAuthenticated(false);
      await loadProducts();
    }
  }

  async function login(data) {
    await api("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify(data)
    });
  }

  async function register(data) {
    await api("/api/v1/auth/register", {
      method: "POST",
      body: data
    });
  }

  async function logout() {
    await api("/api/v1/auth/logout", { method: "POST", body: "{}" });
  }

  async function saveProfile(data) {
    state.user = await api("/api/v1/profile", {
      method: "PUT",
      body: JSON.stringify(data)
    });
    renderProfile();
  }

  async function saveProfilePassword(data) {
    await api("/api/v1/profile/password", {
      method: "PUT",
      body: JSON.stringify(data)
    });
  }

  async function uploadProfilePicture(file) {
    const formData = new FormData();
    formData.append("profilePicture", file);

    const result = await api("/api/v1/profile/picture", {
      method: "POST",
      body: formData
    });

    if (result && result.profile) {
      state.user = result.profile;
      renderProfile();
      navUserName.textContent = `${state.user.username} (${state.user.roles.join(", ")})`;
    }
  }

  async function deleteOwnProfile(password) {
    await api("/api/v1/profile/delete", {
      method: "POST",
      body: JSON.stringify({ password })
    });
  }

  async function createProduct(formData) {
    await api("/api/v1/products", {
      method: "POST",
      body: formData
    });
  }

  async function populateProvinceSelect() {
    const provinces = await loadProvinces();
    createProductProvince.innerHTML = '<option value="">Selecciona una provincia</option>';

    provinces.forEach((province) => {
      const option = document.createElement("option");
      option.value = province.id;
      option.textContent = province.name;
      createProductProvince.appendChild(option);
    });
  }

  async function populateMunicipalitySelect(provinceId) {
    createProductMunicipality.innerHTML = '<option value="">Selecciona un municipio</option>';

    if (!provinceId) {
      createProductMunicipality.disabled = true;
      return;
    }

    const municipalities = await loadMunicipalitiesByProvince(provinceId);
    municipalities.forEach((municipality) => {
      const option = document.createElement("option");
      option.value = municipality.id;
      option.textContent = municipality.name;
      createProductMunicipality.appendChild(option);
    });
    createProductMunicipality.disabled = false;
  }

  function wireEvents() {
    document.getElementById("nav-brand-home").addEventListener("click", (event) => {
      event.preventDefault();
      if (state.user) {
        showView("catalog");
      }
    });

    document.getElementById("nav-link-profile").addEventListener("click", (event) => {
      event.preventDefault();
      if (state.user) {
        showView("profile");
      }
    });

    document.querySelectorAll("#nav-links .nav-link").forEach((link) => {
      link.addEventListener("click", async (event) => {
        event.preventDefault();
        const view = link.dataset.view;
        if (!view) {
          return;
        }

        try {
          if (view === "cart") await loadCart();
          if (view === "orders") await loadOrders();
          if (view === "users") await loadUsers();
          if (view === "profile") await loadProfile();
          showView(view);
        } catch (error) {
          showToast(error.message, "text-bg-danger");
        }
      });
    });

    document.getElementById("btn-open-login").addEventListener("click", () => loginModalInstance.show());
    document.getElementById("btn-open-register").addEventListener("click", () => registerModalInstance.show());

    document.getElementById("btn-reload-catalog").addEventListener("click", async () => {
      try {
        await loadProducts();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    btnOpenCreateProduct.addEventListener("click", async () => {
      try {
        await populateProvinceSelect();
        await populateMunicipalitySelect("");
        createProductForm.reset();
        createProductModalInstance.show();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    createProductProvince.addEventListener("change", async () => {
      try {
        await populateMunicipalitySelect(createProductProvince.value);
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    createProductForm.addEventListener("submit", async (event) => {
      event.preventDefault();

      const formData = new FormData();
      formData.append("name", createProductForm.name.value.trim());
      formData.append("price", createProductForm.price.value);
      formData.append("stock", createProductForm.stock.value);
      formData.append("municipalityId", createProductForm.municipalityId.value);

      const file = createProductForm.productPictureFile.files && createProductForm.productPictureFile.files[0];
      if (file) {
        formData.append("productPictureFile", file);
      }

      try {
        await createProduct(formData);
        createProductModalInstance.hide();
        showToast("Articulo creado correctamente.", "text-bg-success");
        await loadProducts();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    document.getElementById("btn-reload-cart").addEventListener("click", async () => {
      try {
        await loadCart();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    btnGoCatalogFromCart.addEventListener("click", () => {
      showView("catalog");
    });

    document.getElementById("btn-reload-orders").addEventListener("click", async () => {
      try {
        await loadOrders();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    document.getElementById("btn-reload-users").addEventListener("click", async () => {
      try {
        await loadUsers();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    btnCompleteOrder.addEventListener("click", async () => {
      try {
        await api("/api/v1/orders/complete", { method: "POST", body: "{}" });
        showToast("Pedido completado.", "text-bg-success");
        await loadCart();
        await loadOrders();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    btnLogout.addEventListener("click", async () => {
      try {
        await logout();
        window.localStorage.removeItem(AUTH_HINT_KEY);
      } finally {
        await loadSession();
      }
    });

    loginForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = {
        usernameOrEmail: loginForm.usernameOrEmail.value.trim(),
        password: loginForm.password.value
      };

      try {
        await login(payload);
        window.localStorage.setItem(AUTH_HINT_KEY, "1");
        loginForm.reset();
        loginModalInstance.hide();
        showToast("Sesion iniciada correctamente.", "text-bg-success");
        await loadSession();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    registerForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = new FormData();
      payload.append("username", registerForm.username.value.trim());
      payload.append("email", registerForm.email.value.trim());
      payload.append("password", registerForm.password.value);
      payload.append("confirmPassword", registerForm.confirmPassword.value);
      payload.append("gender", registerForm.gender.value);

      const pictureFile = registerForm.profilePicture.files && registerForm.profilePicture.files[0];
      if (pictureFile) {
        payload.append("profilePicture", pictureFile);
      }

      try {
        await register(payload);
        registerForm.reset();
        registerModalInstance.hide();
        showToast("Registro completado. Ya puedes iniciar sesion.", "text-bg-success");
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    profileForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = {
        email: profileForm.email.value.trim(),
        firstName: profileForm.firstName.value.trim(),
        lastName: profileForm.lastName.value.trim(),
        phoneNumber: profileForm.phoneNumber.value.trim(),
        gender: profileForm.gender.value
      };

      try {
        await saveProfile(payload);
        navUserName.textContent = `${state.user.username} (${state.user.roles.join(", ")})`;
        showToast("Perfil actualizado.", "text-bg-success");
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    profilePictureForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const file = profilePictureInput.files && profilePictureInput.files[0];
      if (!file) {
        showToast("Selecciona una imagen para continuar.", "text-bg-warning");
        return;
      }

      try {
        await uploadProfilePicture(file);
        profilePictureForm.reset();
        showToast("Foto de perfil actualizada.", "text-bg-success");
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    profilePasswordForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const payload = {
        currentPassword: profilePasswordForm.currentPassword.value,
        newPassword: profilePasswordForm.newPassword.value,
        confirmPassword: profilePasswordForm.confirmPassword.value
      };

      try {
        await saveProfilePassword(payload);
        profilePasswordForm.reset();
        showToast("Contrasena actualizada.", "text-bg-success");
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      }
    });

    btnConfirmDeleteProfile.addEventListener("click", async () => {
      const password = (deleteProfilePasswordInput.value || "").trim();
      if (!password) {
        showToast("Debes ingresar tu contrasena.", "text-bg-warning");
        deleteProfilePasswordInput.focus();
        return;
      }

      const originalText = btnConfirmDeleteProfile.textContent;
      btnConfirmDeleteProfile.disabled = true;
      btnConfirmDeleteProfile.textContent = "Eliminando...";

      try {
        await deleteOwnProfile(password);
        window.localStorage.removeItem(AUTH_HINT_KEY);
        deleteProfilePasswordInput.value = "";
        deleteProfileModalInstance.hide();
        showToast("Cuenta eliminada correctamente.", "text-bg-success");
        await loadSession();
      } catch (error) {
        showToast(error.message, "text-bg-danger");
      } finally {
        btnConfirmDeleteProfile.disabled = false;
        btnConfirmDeleteProfile.textContent = originalText;
      }
    });

    deleteProfileModalEl.addEventListener("hidden.bs.modal", () => {
      deleteProfilePasswordInput.value = "";
    });
  }

  async function bootstrapApp() {
    initBootstrap();
    wireEvents();
    await loadSession();
  }

  await bootstrapApp();
};