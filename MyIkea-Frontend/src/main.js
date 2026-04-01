const COMPONENT_PATHS = {
  head: new URL("./app/layout/head.component.html", import.meta.url).toString(),
  header: new URL("./app/layout/header.component.html", import.meta.url).toString(),
  main: new URL("./app/views/main-content.component.html", import.meta.url).toString(),
  modals: new URL("./app/overlays/modals.component.html", import.meta.url).toString(),
  toast: new URL("./app/overlays/toast.component.html", import.meta.url).toString(),
  footer: new URL("./app/layout/footer.component.html", import.meta.url).toString()
};

async function loadHtmlFragment(path) {
  const response = await fetch(path, { cache: "no-cache" });
  if (!response.ok) {
    throw new Error(`No se pudo cargar ${path}`);
  }
  return response.text();
}

function injectHead(headHtml) {
  document.head.insertAdjacentHTML("beforeend", headHtml);
}

function mountSpaShell(parts) {
  const root = document.getElementById("app-root");
  if (!root) {
    throw new Error("No existe #app-root para montar la SPA.");
  }

  root.innerHTML = [parts.header, parts.main, parts.modals, parts.toast, parts.footer].join("\n");
}

async function ensureBootstrapBundle() {
  if (window.bootstrap) {
    return;
  }

  await new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = "https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.3/js/bootstrap.bundle.min.js";
    script.crossOrigin = "anonymous";
    script.referrerPolicy = "no-referrer";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("No se pudo cargar Bootstrap JS."));
    document.body.appendChild(script);
  });
}

async function loadLegacyAppScript() {
  await new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = "./app.js";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("No se pudo cargar app.js"));
    document.body.appendChild(script);
  });
}

async function initLayoutScripts() {
  const footerModule = await import("./app/layout/footer.component.js");
  if (typeof footerModule.initFooterComponent === "function") {
    footerModule.initFooterComponent();
  }
}

async function bootstrapSpa() {
  const [head, header, main, modals, toast, footer] = await Promise.all([
    loadHtmlFragment(COMPONENT_PATHS.head),
    loadHtmlFragment(COMPONENT_PATHS.header),
    loadHtmlFragment(COMPONENT_PATHS.main),
    loadHtmlFragment(COMPONENT_PATHS.modals),
    loadHtmlFragment(COMPONENT_PATHS.toast),
    loadHtmlFragment(COMPONENT_PATHS.footer)
  ]);

  injectHead(head);
  mountSpaShell({ header, main, modals, toast, footer });
  await initLayoutScripts();
  await ensureBootstrapBundle();
  await loadLegacyAppScript();

  if (!window.MyIkeaApp || typeof window.MyIkeaApp.init !== "function") {
    throw new Error("No se pudo inicializar MyIkeaApp.");
  }

  await window.MyIkeaApp.init();
}

bootstrapSpa().catch((error) => {
  const root = document.getElementById("app-root");
  if (root) {
    root.innerHTML = `<div class="container py-5"><div class="alert alert-danger">${error.message}</div></div>`;
  }
});