import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 50,
  duration: '2m',
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
    http_req_failed: ['rate<0.01'],
    checkout_success: ['rate>0.98'],
  },
};

const BASE_URL = 'https://staging.acme-corp.com';

export default function () {
  const cartRes = http.post(`${BASE_URL}/api/cart`, JSON.stringify({ sku: 'ACME-BRG-4402', quantity: 2 }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(cartRes, { 'cart created': (r) => r.status === 201 });

  const checkoutRes = http.post(`${BASE_URL}/api/checkout`, JSON.stringify({ cartId: cartRes.json('id') }), {
    headers: { 'Content-Type': 'application/json' },
  });
  check(checkoutRes, { 'checkout succeeded': (r) => r.status === 200 });

  sleep(1);
}
