import http from 'k6/http';
import { check, sleep } from 'k6';

// -----------------------------------------------------------------
// YÊU CẦU: Load test (100, 500, 1000 users)
// Chúng ta sẽ làm một ví dụ cho 100 users.
// Bạn chỉ cần thay số 100 -> 500 hoặc 1000 để chạy các test khác.
// -----------------------------------------------------------------
export const options = {
  stages: [
    { duration: '30s', target: 100 }, // Ramp-up: Tăng dần lên 100 users trong 30 giây
    { duration: '1m', target: 100 },  // Load test: Giữ 100 users trong 1 phút
    { duration: '10s', target: 0 },   // Ramp-down: Giảm về 0 users
  ],
  thresholds: {
    // Yêu cầu: 95% request phải hoàn thành dưới 800ms
    http_req_duration: ['p(95)<800'], 
    'checks': ['rate>0.99'],
  },
};

// -----------------------------------------------------------------
// YÊU CẦU: Stress test (Tìm breaking point)
// Để chạy STRESS TEST, bạn hãy comment (//) options ở trên
// và bỏ comment (//) options ở dưới:
// -----------------------------------------------------------------
/*
export const options = {
  stages: [
    // Ramp-up liên tục để xem server "chết" ở mức nào
    { duration: '2m', target: 100 },
    { duration: '2m', target: 200 },
    { duration: '2m', target: 300 },
    { duration: '2m', target: 400 },
    { duration: '2m', target: 500 },
    // Bạn có thể thêm nhiều stage hơn
  ],
};
*/

// -----------------------------------------------------------------
// HÀNH ĐỘNG CỦA MỖI USER
// -----------------------------------------------------------------
export default function () {
  const url = 'http://localhost:8080/api/auth/login';
  
  // Test với tài khoản sai để không làm "rác" DB
  // nhưng vẫn test được hiệu năng của API
  const payload = JSON.stringify({
    username: `user${__VU}@test.com`, // Mỗi user ảo sẽ có 1 username khác nhau
    password: 'wrongpassword',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    expectedStatuses: [401],
  };

  // Gửi request POST
  const res = http.post(url, payload, params);

  // Kiểm tra kết quả (Response time analysis)
  check(res, {
    // Chúng ta mong đợi 401 (Unauthorized) vì pass sai
    'Login API status is 401': (r) => r.status === 401,
  });

  // Chờ 1 giây trước khi lặp lại (giống người dùng thật)
  sleep(1);
}