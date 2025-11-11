// src/components/Product.mock.test.js

import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';

// Import các component chúng ta sẽ test
import ProductManagement from '../components/ProductManagement';
import ProductDetail from '../components/ProductDetail';
// ProductForm sẽ được test thông qua ProductManagement

// Yêu cầu a) Mock CRUD operations
// Mock toàn bộ module 'productService'
import * as productService from '../service/productService';
jest.mock('../service/productService');

// --- Dữ liệu Mock cho các test cases ---
const MOCK_PRODUCTS = [
  { id: 1, ten: 'Laptop Pro X1', gia: 35000000, soLuong: 50, categoryId: 1 },
  { id: 2, ten: 'Bàn phím cơ K10', gia: 1800000, soLuong: 120, categoryId: 1 },
];

const MOCK_CATEGORIES = [
  { id: 1, name: 'Electronics' },
  { id: 2, name: 'Accessories' },
];

const NEW_PRODUCT_DATA = { 
  ten: 'Chuột Gaming', 
  gia: 750000, 
  soLuong: 200, 
  categoryId: 1, 
  createdById: 1 
};
const CREATED_PRODUCT = { id: 3, ...NEW_PRODUCT_DATA };
// ------------------------------------------

describe('Product Mock Tests (5.2.1)', () => {

  // Reset tất cả mock trước mỗi test
  beforeEach(() => {
    jest.resetAllMocks();
  });

  // --- 1. Test cho READ (All) - (getAllProducts) ---
  describe('Mock: Read (All) - ProductManagement', () => {

    // Yêu cầu b) Test success scenario
    test('TC1: Hiển thị danh sách sản phẩm khi API mock thành công', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockResolvedValue(MOCK_PRODUCTS);
      productService.getAllCategories.mockResolvedValue(MOCK_CATEGORIES); // Cần cho form

      render(<ProductManagement />);
      
      // Chờ và kiểm tra kết quả
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();
      expect(screen.getByText('Bàn phím cơ K10')).toBeInTheDocument();
      expect(screen.getByText('35,000,000 VNĐ')).toBeInTheDocument();

      // Yêu cầu c) Verify mock call
      expect(productService.getAllProducts).toHaveBeenCalledTimes(1);
    });

    // Yêu cầu b) Test failure scenario
    test('TC2: Hiển thị lỗi khi API mock getAllProducts thất bại', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockRejectedValue(new Error('API Error'));
      
      render(<ProductManagement />);

      // Chờ và kiểm tra lỗi (dựa theo code ProductManagement.jsx)
      expect(await screen.findByText('Không thể tải danh sách sản phẩm.')).toBeInTheDocument();
      
      // Yêu cầu c) Verify mock call
      expect(productService.getAllProducts).toHaveBeenCalledTimes(1);
    });
  });

  // --- 2. Test cho READ (One) - (getProductById) ---
  describe('Mock: Read (One) - ProductDetail', () => {

    // Yêu cầu b) Test success scenario
    test('TC3: Hiển thị chi tiết sản phẩm khi API mock thành công', async () => {
      // Chuẩn bị mock
      productService.getProductById.mockResolvedValue(MOCK_PRODUCTS[0]);
      
      render(<ProductDetail id={1} />);

      // Chờ và kiểm tra (dựa theo code ProductDetail.jsx)
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();
      expect(screen.getByText('Giá: 35000000')).toBeInTheDocument();

      // Yêu cầu c) Verify mock call
      expect(productService.getProductById).toHaveBeenCalledWith(1);
    });

    // Yêu cầu b) Test failure scenario
    test('TC4: Hiển thị lỗi khi API mock getProductById thất bại', async () => {
      // Chuẩn bị mock
      productService.getProductById.mockRejectedValue(new Error('Not Found'));
      
      render(<ProductDetail id={999} />);

      // Chờ và kiểm tra (dựa theo code ProductDetail.jsx)
      expect(await screen.findByText('Khong tim thay san pham')).toBeInTheDocument();
      
      // Yêu cầu c) Verify mock call
      expect(productService.getProductById).toHaveBeenCalledWith(999);
    });
  });

  // --- 3. Test cho CREATE - (createProduct) ---
  describe('Mock: Create - ProductManagement Form', () => {
    
    // Yêu cầu b) Test success scenario
    test('TC5: Mock Create product thành công và tải lại danh sách', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockResolvedValue(MOCK_PRODUCTS);
      productService.getAllCategories.mockResolvedValue(MOCK_CATEGORIES);
      productService.createProduct.mockResolvedValue(CREATED_PRODUCT); // Mock CREATE

      render(<ProductManagement />);
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();

      // Mở form
      await userEvent.click(screen.getByRole('button', { name: /Thêm sản phẩm mới/i }));
      const modal = await screen.findByRole('dialog');
      expect(within(modal).getByText(/Thêm Sản Phẩm Mới/i)).toBeInTheDocument();  

      // Điền form (dựa theo ProductForm.jsx)
      await userEvent.type(within(modal).getByLabelText(/Tên sản phẩm/i), NEW_PRODUCT_DATA.ten);
      await userEvent.type(within(modal).getByLabelText(/Giá/i), NEW_PRODUCT_DATA.gia.toString());
      await userEvent.type(within(modal).getByLabelText(/Số lượng/i), NEW_PRODUCT_DATA.soLuong.toString());
      await userEvent.selectOptions(within(modal).getByLabelText(/Category/i), screen.getByRole('option', { name: /Electronics/i }));

      // Mock cho lần gọi loadProducts() thứ 2 (sau khi save)
      productService.getAllProducts.mockResolvedValue([...MOCK_PRODUCTS, CREATED_PRODUCT]);

      // Lưu
      await userEvent.click(within(modal).getByRole('button', { name: /Lưu/i }));

      // Yêu cầu c) Verify mock call
      await waitFor(() => {
        expect(productService.createProduct).toHaveBeenCalledWith(NEW_PRODUCT_DATA);
      });
      
      // Verify UI
      expect(await screen.findByText(CREATED_PRODUCT.ten)).toBeInTheDocument();
      expect(modal).not.toBeInTheDocument();
    });
  });

  // --- 4. Test cho UPDATE - (updateProduct) ---
  describe('Mock: Update - ProductManagement Form', () => {

    // Yêu cầu b) Test success scenario
    test('TC6: Mock Update product thành công và tải lại danh sách', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockResolvedValue(MOCK_PRODUCTS);
      productService.getAllCategories.mockResolvedValue(MOCK_CATEGORIES);
      const UPDATED_PRODUCT = { ...MOCK_PRODUCTS[0], ten: 'Laptop Siêu Cấp' };
      productService.updateProduct.mockResolvedValue(UPDATED_PRODUCT); // Mock UPDATE

      render(<ProductManagement />);
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();

      // Mở form Edit
      await userEvent.click(screen.getAllByRole('button', { name: /Sửa/i })[0]);
      const modal = await screen.findByRole('dialog');
        expect(within(modal).getByText(/Sửa Sản Phẩm/i)).toBeInTheDocument();

      // Sửa tên
      const tenInput = within(modal).getByLabelText(/Tên sản phẩm/i);
      await userEvent.clear(tenInput);
      await userEvent.type(tenInput, 'Laptop Siêu Cấp');

      // Mock loadProducts (lần 2)
      productService.getAllProducts.mockResolvedValue([UPDATED_PRODUCT, MOCK_PRODUCTS[1]]);

      // Lưu
      await userEvent.click(within(modal).getByRole('button', { name: /Lưu/i }));

      // Yêu cầu c) Verify mock call
      await waitFor(() => {
        // Kiểm tra id và data đã thay đổi
        expect(productService.updateProduct).toHaveBeenCalledWith(
          MOCK_PRODUCTS[0].id, 
          expect.objectContaining({ ten: 'Laptop Siêu Cấp' })
        );
      });
      
      // Verify UI
      expect(await screen.findByText('Laptop Siêu Cấp')).toBeInTheDocument();
      expect(screen.queryByText('Laptop Pro X1')).not.toBeInTheDocument();
    });
  });

  // --- 5. Test cho DELETE - (deleteProduct) ---
  describe('Mock: Delete - ProductManagement', () => {

    // Yêu cầu b) Test success scenario
    test('TC7: Mock Delete product thành công và tải lại danh sách', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockResolvedValueOnce(MOCK_PRODUCTS); // Lần load đầu
      productService.deleteProduct.mockResolvedValue({}); // Mock DELETE
      
      render(<ProductManagement />);
      
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();

      // Bấm nút Xoá (của item đầu tiên)
      await userEvent.click(screen.getAllByRole('button', { name: /Xoá/i })[0]); 
      
      // Tìm modal xác nhận
      const modal = await screen.findByRole('dialog');
        expect(within(modal).getByText(/Xác nhận xoá/i)).toBeInTheDocument();
      expect(within(modal).getByText('Laptop Pro X1')).toBeInTheDocument();

      // Mock loadProducts (lần 2) - chỉ còn 1 item
      productService.getAllProducts.mockResolvedValueOnce([MOCK_PRODUCTS[1]]); 
    
      // Bấm nút "Xoá" trong modal
      await userEvent.click(within(modal).getByRole('button', { name: 'Xoá' }));
    
      // Yêu cầu c) Verify mock call
      await waitFor(() => {
        expect(productService.deleteProduct).toHaveBeenCalledWith(MOCK_PRODUCTS[0].id);
      });
    
      // Verify UI
      await waitFor(() => {
        expect(modal).not.toBeInTheDocument();
      });
      expect(screen.queryByText('Laptop Pro X1')).not.toBeInTheDocument();
      expect(screen.getByText('Bàn phím cơ K10')).toBeInTheDocument(); 
    });

    // Yêu cầu b) Test failure scenario
    test('TC8: Mock Delete product thất bại và hiển thị lỗi', async () => {
      // Chuẩn bị mock
      productService.getAllProducts.mockResolvedValue(MOCK_PRODUCTS);
      productService.deleteProduct.mockRejectedValue(new Error('Delete API Error')); // Mock FAIl

      render(<ProductManagement />);
      expect(await screen.findByText('Laptop Pro X1')).toBeInTheDocument();
      
      // Mở modal
      await userEvent.click(screen.getAllByRole('button', { name: /Xoá/i })[0]);
      const modal = await screen.findByRole('dialog');+
      expect(within(modal).getByText('Xác nhận xoá')).toBeInTheDocument();

      expect(within(modal).getByText('Laptop Pro X1')).toBeInTheDocument();
      
      // Xác nhận xoá
      await userEvent.click(within(modal).getByRole('button', { name: 'Xoá' }));
      
      // Yêu cầu c) Verify mock call
      await waitFor(() => {
        expect(productService.deleteProduct).toHaveBeenCalledWith(MOCK_PRODUCTS[0].id);
      });
      
      // Verify UI (dựa theo code ProductManagement.jsx)
      expect(await screen.findByText('Xoá sản phẩm thất bại.')).toBeInTheDocument();
      expect(modal).not.toBeInTheDocument(); // Modal xoá tự đóng
      expect(screen.getByText('Laptop Pro X1')).toBeInTheDocument(); // Item vẫn còn
    });

  });
});