// src/App.jsx

import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import './App.css'; 
<<<<<<< HEAD
=======
import ProductManagement from './components/ProductManagement';
import AppNavbar from './components/AppNavbar';
import ProtectedRoute from './components/ProtectedRoute';
>>>>>>> 773f153d2b8edfbf0fc232657f40c65fba79dc46

function App() {
  return (
    <div className="App">
      <Routes>
<<<<<<< HEAD
        <Route path="/" element={<Login />} />
        
        {/* <Route path="/products" element={<ProductPage />} /> */}
=======
        {/* Route 1: Trang Login (không có Navbar) */}
        <Route path="/" element={<Login />} />
        
        {/* Route 2: Trang Products (có Navbar) */}
        <Route 
          path="/products" 
          element={
            <> {/* 2. Dùng Fragment để render nhiều component */}
              <AppNavbar />
              <ProductManagement />
            </>
          } 
        />
>>>>>>> 773f153d2b8edfbf0fc232657f40c65fba79dc46
      </Routes>
    </div>
  );
}
<<<<<<< HEAD

=======
>>>>>>> 773f153d2b8edfbf0fc232657f40c65fba79dc46
export default App;