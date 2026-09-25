import axios, { AxiosError } from "axios";

const http = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

export class ApiError extends Error {
  code: string;

  constructor(code: string, message: string) {
    super(message);
    this.code = code;
  }
}

interface ErreurBody {
  code?: string;
  message?: string;
}

function versApiError(erreur: unknown): ApiError {
  const axiosErreur = erreur as AxiosError<ErreurBody>;
  const corps = axiosErreur.response?.data;
  return new ApiError(corps?.code ?? "ERREUR_INCONNUE", corps?.message ?? "Une erreur est survenue.");
}

export async function get<T>(path: string): Promise<T> {
  try {
    const { data } = await http.get<T>(path);
    return data;
  } catch (erreur) {
    throw versApiError(erreur);
  }
}

export async function post<T>(path: string, payload: unknown): Promise<T> {
  try {
    const { data } = await http.post<T>(path, payload);
    return data;
  } catch (erreur) {
    throw versApiError(erreur);
  }
}

export async function put<T>(path: string, payload: unknown): Promise<T> {
  try {
    const { data } = await http.put<T>(path, payload);
    return data;
  } catch (erreur) {
    throw versApiError(erreur);
  }
}

export async function patch<T>(path: string, payload?: unknown): Promise<T> {
  try {
    const { data } = await http.patch<T>(path, payload);
    return data;
  } catch (erreur) {
    throw versApiError(erreur);
  }
}
