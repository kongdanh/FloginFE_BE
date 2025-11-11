// src/components/Login.mock.test.js

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router-dom'; // Cần thiết vì Login dùng useNavigate
import Login from '../components/Login';

// Yêu cầu a) Mock authService 
// (Trong file Login.jsx, bạn import { login as loginService }
// nên chúng ta sẽ mock file 'authService' và hàm 'login' của nó)
import * as authService from '../service/authService';
jest.mock('../service/authService');

// Mock 'react-router-dom' để giả lập hàm useNavigate
// Vì sau khi login thành công, component sẽ điều hướng
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // giữ lại các tính năng gốc
  useNavigate: () => mockNavigate,
}));

// Bắt đầu nhóm test
describe('Login Mock Tests', () => {
  // Reset mock trước mỗi test
  beforeEach(() => {
    jest.clearAllMocks();
    // Render component trong MemoryRouter
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
  });

  // Yêu cầu b) Test với mocked successful response 
  test('Mock: Login thành công (Successful Response)', async () => {
    // 1. Chuẩn bị mock data thành công
    authService.login.mockResolvedValue({ token: 'mock-token-123' });

    // 2. Điền form
    fireEvent.change(screen.getByTestId('username-input'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByTestId('password-input'), {
      target: { value: 'Test123' }, // Hợp lệ theo validation
    });

    // 3. Submit form
    fireEvent.click(screen.getByTestId('login-button'));

    // Yêu cầu c) Verify mock calls 
    await waitFor(() => {
      // 4. Xác nhận service đã được gọi đúng
      expect(authService.login).toHaveBeenCalledTimes(1);
      expect(authService.login).toHaveBeenCalledWith('testuser', 'Test123');
    });

    // 5. Xác nhận kết quả (Component Login.jsx điều hướng khi thành công)
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/products');
    });
  });

  // Yêu cầu b) Test với mocked failed response 
  test('Mock: Login thất bại (Failed Response)', async () => {
    // 1. Chuẩn bị mock data lỗi
    const errorMessage = 'Invalid credentials';
    authService.login.mockRejectedValue(new Error(errorMessage));

    // 2. Điền form
    fireEvent.change(screen.getByTestId('username-input'), {
      target: { value: 'wronguser' },
    });
    fireEvent.change(screen.getByTestId('password-input'), {
      target: { value: 'Wrongpass123' },
    });

    // 3. Submit form
    fireEvent.click(screen.getByTestId('login-button'));

    // Yêu cầu c) Verify mock calls 
    await waitFor(() => {
      // 4. Xác nhận service đã được gọi
      expect(authService.login).toHaveBeenCalledTimes(1);
      expect(authService.login).toHaveBeenCalledWith('wronguser', 'Wrongpass123');
    });

    // 5. Xác nhận kết quả (hiển thị lỗi API)
    await waitFor(() => {
      // (Trong Login.jsx, lỗi được hiển thị trong 'login-message')
      const errorAlert = screen.getByTestId('login-message');
      expect(errorAlert).toBeInTheDocument();
      expect(errorAlert).toHaveTextContent(errorMessage);
    });

    // 6. Đảm bảo không điều hướng khi lỗi
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});