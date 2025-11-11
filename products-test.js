import http from 'k6/http';
import { check, sleep } from 'k6';


export const options = {
  stages: [
    { duration: '30s', target: 100 }, 
    { duration: '1m', target: 100 }, 
    { duration: '10s', target: 0 },   
  ],
  thresholds: {
    // API lấy sản phẩm phải nhanh hơn
    http_req_duration: ['p(95)<500'], // 95% request dưới 500ms

    'checks': ['rate>0.99'],
  },
};

export default function () {

  const res = http.get('http://localhost:8080/api/products');

  check(res, {
    'Get Products API status is 200': (r) => r.status === 200,
    'Get Products response time is fast': (r) => r.timings.duration < 500,
  });

  sleep(1);
}