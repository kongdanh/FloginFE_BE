package com.flogin.service;

import com.flogin.dto.ProductDto;
import com.flogin.entity.Category;
import com.flogin.entity.Product;
import com.flogin.entity.User;
import com.flogin.repository.CategoryRepository;
import com.flogin.repository.ProductRepository;
import com.flogin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceMockTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private ProductDto mockProductDto;
    private Category mockCategory;
    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName("Electronics");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockProduct = new Product(
                "Laptop",
                new BigDecimal("15000000"),
                10,
                mockCategory,
                mockUser
        );
        mockProduct.setId(1L);

        mockProductDto = new ProductDto();
        mockProductDto.setId(1L);
        mockProductDto.setTen("Laptop");
        mockProductDto.setGia(new BigDecimal("15000000"));
        mockProductDto.setSoLuong(10);
        mockProductDto.setCategoryId(1L);
        mockProductDto.setCreatedById(1L);
    }

    @Test
    @DisplayName("Test getProductById - Thành công")
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        ProductDto result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getTen());
        assertEquals(1L, result.getId());
        
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test getProductById - Thất bại (Không tìm thấy)")
    void testGetProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.getProductById(99L);
        });
        
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Test getAllProducts - Thành công (Có dữ liệu)")
    void testGetAllProducts_Success_WithData() {
        Product product2 = new Product("Mouse", new BigDecimal("500000"), 50, mockCategory, mockUser);
        product2.setId(2L);
        when(productRepository.findAll()).thenReturn(List.of(mockProduct, product2));

        List<ProductDto> results = productService.getAllProducts();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Laptop", results.get(0).getTen());
        assertEquals("Mouse", results.get(1).getTen());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test getAllProducts - Thành công (Rỗng)")
    void testGetAllProducts_Success_Empty() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductDto> results = productService.getAllProducts();

        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test createProduct - Thành công")
    void testCreateProduct_Success() {
        ProductDto newProductDto = new ProductDto();
        newProductDto.setTen("New Monitor");
        newProductDto.setGia(new BigDecimal("7000000"));
        newProductDto.setSoLuong(15);
        newProductDto.setCategoryId(1L);
        newProductDto.setCreatedById(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setId(100L);
            return savedProduct;
        });

        ProductDto result = productService.createProduct(newProductDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("New Monitor", result.getTen());

        verify(categoryRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Test createProduct - Thất bại (Category không tìm thấy)")
    void testCreateProduct_Failure_CategoryNotFound() {
        mockProductDto.setCategoryId(99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.createProduct(mockProductDto);
        });
        
        verify(categoryRepository, times(1)).findById(99L);
        verify(userRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Test createProduct - Thất bại (User không tìm thấy)")
    void testCreateProduct_Failure_UserNotFound() {
        mockProductDto.setCreatedById(99L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.createProduct(mockProductDto);
        });

        verify(categoryRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Test createProduct - Thất bại (Tên bị trùng)")
    void testCreateProduct_Failure_NameExists() {
        when(productRepository.findByName("Laptop")).thenReturn(Optional.of(mockProduct));
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> {
            productService.createProduct(mockProductDto);
        });
        
    }

    @Test
    @DisplayName("Test updateProduct - Thành công")
    void testUpdateProduct_Success() {
        ProductDto updateDto = new ProductDto();
        updateDto.setTen("Laptop Gen 2");
        updateDto.setGia(new BigDecimal("20000000"));
        updateDto.setSoLuong(5);
        updateDto.setCategoryId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto result = productService.updateProduct(1L, updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop Gen 2", result.getTen());
        assertEquals(new BigDecimal("20000000"), result.getGia());
        assertEquals(5, result.getSoLuong());

        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Test updateProduct - Thất bại (Product không tìm thấy)")
    void testUpdateProduct_Failure_ProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(99L, mockProductDto);
        });
        
        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Test updateProduct - Thất bại (Category mới không tìm thấy)")
    void testUpdateProduct_Failure_CategoryNotFound() {
        mockProductDto.setCategoryId(99L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(1L, mockProductDto);
        });

        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(99L);
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("Test updateProduct - Thất bại (Tên bị trùng)")
    void testUpdateProduct_Failure_NameExists() {
        Product existingProduct = new Product("Mouse", new BigDecimal("500"), 5, mockCategory, mockUser);
        existingProduct.setId(2L);
        
        ProductDto updateDto = new ProductDto();
        updateDto.setTen("Mouse");
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.findByName("Mouse")).thenReturn(Optional.of(existingProduct));

        assertThrows(NullPointerException.class, () -> {
            productService.updateProduct(1L, updateDto);
        });
    }

    @Test
    @DisplayName("Test deleteProduct - Thành công")
    void testDeleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L); 

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Test deleteProduct - Thất bại (Không tìm thấy)")
    void testDeleteProduct_Failure_NotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(99L);
        });

        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }
    
    @Test
    @DisplayName("Test updateProduct - Thành công (Không đổi Category)")
    void testUpdateProduct_Success_NoCategoryChange() {
        ProductDto updateDto = new ProductDto();
        updateDto.setTen("Laptop Gen 3");
        updateDto.setCategoryId(null);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto result = productService.updateProduct(1L, updateDto);

        assertEquals("Laptop Gen 3", result.getTen());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", mockProduct.getCategory().getName());

        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, times(1)).save(mockProduct);
    }
    
    @Test
    @DisplayName("Test getProductById - Thất bại (ID là null)")
    void testGetProductById_NullId() {
        assertThrows(RuntimeException.class, () -> {
            productService.getProductById(null);
        });
    }
}