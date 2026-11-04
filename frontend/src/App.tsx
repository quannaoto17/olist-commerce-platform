import { useEffect, useState } from 'react';
import { fetchBackendStatus } from './api/disputeApi';

export function App() {
  const [backendStatus, setBackendStatus] = useState<string>('Checking connection...');

  useEffect(() => {
    fetchBackendStatus()
      .then((data) => setBackendStatus(`${data.service} (${data.status})`))
      .catch(() => setBackendStatus('Offline - Start Spring Boot Backend on port 8080'));
  }, []);

  return (
    <div style={{ maxWidth: 1100, margin: '40px auto', padding: '0 24px' }}>
      <header style={{ marginBottom: 32 }}>
        <h1 style={{ margin: 0, fontSize: 28 }}>
          Hệ Thống Tự Động Đối Soát Vi Phạm SLA & Bóc Tách Trách Nhiệm Đơn Hàng
        </h1>
        <p style={{ color: '#475569', marginTop: 8 }}>
          Backend Status: <strong>{backendStatus}</strong>
        </p>
      </header>
    </div>
  );
}
