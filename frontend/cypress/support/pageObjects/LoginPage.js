// frontend/cypress/support/pageObjects/LoginPage.js

export class LoginPage {
  // Phương thức điều hướng đến trang login
  visit() {
    cy.visit("/"); // Truy cập vào baseUrl đã cấu hình
  }

  // Lấy các phần tử
  getUsernameInput() {
    return cy.get('[data-testid="username-input"]');
  }

  getPasswordInput() {
    return cy.get('[data-testid="password-input"]');
  }

  getLoginButton() {
    return cy.get('[data-testid="login-button"]');
  }

  // Lấy các thông báo lỗi
  getUsernameError() {
    return cy.get('[data-testid="username-error"]');
  }

  getPasswordError() {
    return cy.get('[data-testid="password-error"]');
  }

  // Lỗi API (login-message)
  getApiError() {
    return cy.get('[data-testid="login-message"]');
  }

  // Một hàm tiện ích để điền form và submit
  login(username, password) {
    if (username) {
      this.getUsernameInput().type(username);
    }
    if (password) {
      this.getPasswordInput().type(password);
    }
    this.getLoginButton().click();
  }
}