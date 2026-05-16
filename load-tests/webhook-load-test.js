import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import crypto from 'k6/crypto';

// ── Custom metrics ──
const errorRate   = new Rate('error_rate');
const webhookTrend = new Trend('webhook_duration');

// ── Test config ──
export const options = {
    stages: [
        { duration: '10s', target: 10  },  // ramp up to 10 users
        { duration: '30s', target: 50  },  // ramp up to 50 users
        { duration: '20s', target: 50  },  // hold at 50 users
        { duration: '10s', target: 0   },  // ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // p95 < 2s
        error_rate:        ['rate<0.01'],   // < 1% errors
        http_req_failed:   ['rate<0.01'],
    },
};

const SECRET = 'my-webhook-secret';
const BASE_URL = 'http://localhost:8081';

function computeHmac(payload) {
    const hmac = crypto.createHMAC('sha256', SECRET);
    hmac.update(payload);
    return 'sha256=' + hmac.digest('hex');
}

function buildPayload(prNumber) {
    return JSON.stringify({
        action: 'opened',
        number: prNumber,
        pull_request: {
            title: `Load Test PR ${prNumber}`,
            head: { sha: '45f67fbacfe1bf165078115d01f741ca29a4b4f5', ref: 'feature/load-test' },
            base: { sha: '87dc36c08702103f52db43e113dada0021babb01', ref: 'main' }
        },
        repository: {
            id: 1,
            name: 'web-servers',
            full_name: 'Nikhil-keshri2213/web-servers',
            private: false
        },
        sender: { login: 'Nikhil-keshri2213', id: 1 }
    });
}

export default function () {
    const prNumber = Math.floor(Math.random() * 1000) + 1;
    const payload  = buildPayload(prNumber);
    const signature = computeHmac(payload);

    const params = {
        headers: {
            'Content-Type':          'application/json',
            'X-GitHub-Event':        'pull_request',
            'X-Hub-Signature-256':   signature,
        },
        timeout: '10s',
    };

    const res = http.post(`${BASE_URL}/webhook/github`, payload, params);

    const success = check(res, {
        'status is 200':        (r) => r.status === 200,
        'response time < 2s':   (r) => r.timings.duration < 2000,
        'has success field':    (r) => r.body.includes('success'),
    });

    errorRate.add(!success);
    webhookTrend.add(res.timings.duration);

    sleep(0.5);
}

export function handleSummary(data) {
    return {
        'load-tests/results/summary.json': JSON.stringify(data, null, 2),
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
    };
}

function textSummary(data, opts) {
    const p95 = data.metrics.http_req_duration?.values?.['p(95)'] || 0;
    const errRate = (data.metrics.error_rate?.values?.rate || 0) * 100;
    const reqCount = data.metrics.http_reqs?.values?.count || 0;
    const rps = data.metrics.http_reqs?.values?.rate || 0;

    return `
╔══════════════════════════════════════════════╗
║      AI CODE REVIEW — LOAD TEST RESULTS      ║
╚══════════════════════════════════════════════╝

  Total Requests : ${reqCount}
  Req/sec        : ${rps.toFixed(2)}
  p95 Latency    : ${p95.toFixed(0)}ms  ${p95 < 2000 ? '✅' : '❌'} (target: <2000ms)
  Error Rate     : ${errRate.toFixed(2)}%  ${errRate < 1 ? '✅' : '❌'} (target: <1%)

  Thresholds     : ${Object.entries(data.metrics)
        .filter(([, v]) => v.thresholds)
        .map(([k, v]) => `\n    ${k}: ${Object.values(v.thresholds).every(t => !t.ok === false) ? '✅' : '❌'}`)
        .join('')}
`;
}