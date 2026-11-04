const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export async function fetchBackendStatus(): Promise<{ service: string; status: string }> {
  const res = await fetch(`${API_BASE_URL}/disputes/status`);
  if (!res.ok) {
    throw new Error(`HTTP error! status: ${res.status}`);
  }
  return res.json();
}
