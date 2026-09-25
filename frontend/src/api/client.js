const BASE_URL = "http://localhost:8080";

export class ApiError extends Error {
  constructor(code, message) {
    super(message);
    this.code = code;
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (response.status === 204) {
    return null;
  }

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiError(body?.code ?? "ERREUR_INCONNUE", body?.message ?? "Une erreur est survenue.");
  }

  return body;
}

export function get(path) {
  return request(path, { method: "GET" });
}

export function post(path, payload) {
  return request(path, { method: "POST", body: JSON.stringify(payload) });
}

export function put(path, payload) {
  return request(path, { method: "PUT", body: JSON.stringify(payload) });
}

export function patch(path, payload) {
  return request(path, { method: "PATCH", body: payload !== undefined ? JSON.stringify(payload) : undefined });
}
