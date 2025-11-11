// frontend/cypress.config.js

import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    // Port 5173 (Vite) hoặc 3000 (React)
    baseUrl: "http://localhost:5173", 
    testIsolation: false,

    // === THÊM DÒNG NÀY VÀO ===
    // Chỉ cho Cypress biết tìm các file .js trong cypress/e2e
    specPattern: "cypress/e2e/**/*.e2e.spec.js", 
    // ==========================

    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});