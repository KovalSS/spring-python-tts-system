const API_BASE = process.env.REACT_APP_API_BASE_URL || "";

async function request(path, options = {}, token) {
  const headers = {
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed: ${response.status}`);
  }

  return response;
}

export async function getAnonymousAuth() {
  const response = await request("/api/v1/auth/anonymous", { method: "POST" });
  return response.json();
}


export async function uploadTextFile(file, token) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await request(
    "/api/v1/file/upload",
    {
      method: "POST",
      body: formData,
    },
    token
  );

  return response.json();
}

export async function getJobs(token) {
  const response = await request("/api/v1/jobs", { method: "GET" }, token);
  return response.json();
}

export async function pushJob(jobId, token, settings = {}) {
  const body = {};
  if (settings.voiceId) body.voiceId = settings.voiceId;
  if (settings.rate) body.rate = settings.rate;
  if (settings.pitch) body.pitch = settings.pitch;
  if (settings.volume) body.volume = settings.volume;

  await request(
    `/api/v1/jobs/${jobId}/push`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: Object.keys(body).length > 0 ? JSON.stringify(body) : undefined,
    },
    token
  );
}

export async function deleteJob(jobId, token) {
  await request(`/api/v1/jobs/${jobId}`, { method: "DELETE" }, token);
}

export async function downloadJobResult(jobId, token) {
  const response = await request(`/api/v1/file/download/${jobId}`, { method: "GET" }, token);
  return response.blob();
}

