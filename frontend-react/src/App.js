import './App.css';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  deleteJob,
  downloadJobResult,
  getAnonymousAuth,
  getJobs,
  pushJob,
  uploadTextFile,
} from './api';
import {
  formatPitch,
  formatRate,
  formatVoice,
  formatVolume,
  PITCH_OPTIONS,
  RATE_OPTIONS,
  VOICE_OPTIONS,
  VOLUME_OPTIONS,
} from './ttsOptions';
import { createJobsSocket } from './websocket';

const TOKEN_KEY = 'tts.token';
const USER_ID_KEY = 'tts.userId';

function statusClass(status) {
  switch (status) {
    case 'DONE':
      return 'status-done';
    case 'ERROR':
      return 'status-error';
    case 'PROCESSING':
      return 'status-processing';
    case 'QUEUED':
      return 'status-queued';
    default:
      return 'status-created';
  }
}

function mergeJobUpdate(jobs, update) {
  if (!update || !update.jobId) {
    return jobs;
  }

  const index = jobs.findIndex((job) => String(job.id) === String(update.jobId));
  if (index < 0) {
    return jobs;
  }

  const nextJobs = [...jobs];
  nextJobs[index] = {
    ...nextJobs[index],
    status: update.status || nextJobs[index].status,
  };

  return nextJobs;
}

function getDownloadName(job) {
  const fallback = `${job.id}.mp3`;
  if (!job?.fileName) {
    return fallback;
  }

  const baseName = String(job.fileName).replace(/\.[^/.]+$/, '');
  return `${baseName || job.id}.mp3`;
}

function jobTtsSummary(job) {
  return [
    `Голос: ${formatVoice(job.voiceId)}`,
    `Швидкість: ${formatRate(job.rate)}`,
    `Висота: ${formatPitch(job.pitch)}`,
    `Гучність: ${formatVolume(job.volume)}`,
  ].join(' · ');
}

function App() {
  const [token, setToken] = useState('');
  const [userId, setUserId] = useState('');
  const [jobs, setJobs] = useState([]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [liveState, setLiveState] = useState('offline');
  const [editingJobId, setEditingJobId] = useState(null);
  const [editingParams, setEditingParams] = useState({
    voiceId: '',
    rate: '',
    pitch: '',
    volume: '',
  });

  const fallbackPollingEnabled = useMemo(
    () => liveState === 'offline' || liveState === 'error' || liveState === 'reconnecting',
    [liveState]
  );

  const refreshJobs = useCallback(
    async (authToken) => {
      if (!authToken) {
        return;
      }

      const allJobs = await getJobs(authToken);
      setJobs(Array.isArray(allJobs) ? allJobs : []);
    },
    []
  );

  const bootstrapAuth = useCallback(async () => {
    const savedToken = localStorage.getItem(TOKEN_KEY);
    const savedUserId = localStorage.getItem(USER_ID_KEY);

    if (savedToken && savedUserId) {
      setToken(savedToken);
      setUserId(savedUserId);
      return;
    }

    const auth = await getAnonymousAuth();
    localStorage.setItem(TOKEN_KEY, auth.token);
    localStorage.setItem(USER_ID_KEY, auth.userId);
    setToken(auth.token);
    setUserId(auth.userId);
  }, []);

  useEffect(() => {
    bootstrapAuth().catch((requestError) => {
      setError(`Auth failed: ${requestError.message}`);
    });
  }, [bootstrapAuth]);

  useEffect(() => {
    if (!token) {
      return;
    }

    refreshJobs(token).catch((requestError) => {
      setError(`Failed to load jobs: ${requestError.message}`);
    });
  }, [token, refreshJobs]);

  useEffect(() => {
    if (!token) {
      return;
    }

    const socket = createJobsSocket({
      token,
      onStateChange: setLiveState,
      onError: (socketError) => {
        setError(`Live updates issue: ${socketError.message}`);
      },
      onEvent: (event) => {
        setJobs((prev) => mergeJobUpdate(prev, event));
      },
    });

    socket.connect();

    return () => {
      socket.disconnect();
    };
  }, [token]);

  useEffect(() => {
    if (!token || !fallbackPollingEnabled) {
      return;
    }

    const intervalId = setInterval(() => {
      refreshJobs(token).catch(() => {
        // Keep fallback silent; UI already shows WS state.
      });
    }, 5000);

    return () => clearInterval(intervalId);
  }, [token, fallbackPollingEnabled, refreshJobs]);

  async function handleUpload(event) {
    event.preventDefault();
    setError('');

    if (!token || !selectedFile) {
      setError('Select a text file first.');
      return;
    }

    setIsLoading(true);
    try {
      const createdJob = await uploadTextFile(selectedFile, token);
      setJobs((prev) => [createdJob, ...prev]);
      setSelectedFile(null);
      event.target.reset();
    } catch (requestError) {
      setError(`Upload failed: ${requestError.message}`);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleStart(jobId) {
    setError('');
    try {
      await pushJob(jobId, token, editingJobId === jobId ? editingParams : {});
      setEditingJobId(null);
      setEditingParams({ voiceId: '', rate: '', pitch: '', volume: '' });
      await refreshJobs(token);
    } catch (requestError) {
      setError(`Start failed: ${requestError.message}`);
    }
  }

  function openEditParams(job) {
    setEditingJobId(job.id);
    setEditingParams({
      voiceId: job.voiceId || '',
      rate: job.rate || '',
      pitch: job.pitch || '',
      volume: job.volume || '',
    });
  }

  function closeEditParams() {
    setEditingJobId(null);
    setEditingParams({ voiceId: '', rate: '', pitch: '', volume: '' });
  }

  async function handleDelete(jobId) {
    setError('');
    try {
      await deleteJob(jobId, token);
      setJobs((prev) => prev.filter((job) => String(job.id) !== String(jobId)));
    } catch (requestError) {
      setError(`Delete failed: ${requestError.message}`);
    }
  }

  async function handleDownload(jobId, fileName) {
    setError('');
    try {
      const blob = await downloadJobResult(jobId, token);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName || `${jobId}.mp3`;
      link.click();
      URL.revokeObjectURL(url);
    } catch (requestError) {
      setError(`Download failed: ${requestError.message}`);
    }
  }

  return (
    <div className="App">
      <main className="container">
        <h1>TTS Jobs Dashboard</h1>
        <p className="meta">User: <code>{userId || 'loading...'}</code></p>
        <p className={`live-state ${liveState}`}>Live: {liveState}</p>

        <form className="upload-form" onSubmit={handleUpload}>
          <div className="form-row">
            <input
              type="file"
              accept=".txt,text/plain"
              onChange={(event) => setSelectedFile(event.target.files?.[0] || null)}
            />
            <button type="submit" disabled={isLoading || !selectedFile}>
              {isLoading ? 'Uploading...' : 'Create Job'}
            </button>
            <button type="button" onClick={() => refreshJobs(token)}>
              Refresh
            </button>
          </div>
        </form>

        {error ? <p className="error">{error}</p> : null}

        <table>
          <thead>
            <tr>
              <th>File</th>
              <th>TTS params</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {jobs.map((job) => (
              <>
                <tr key={job.id}>
                  <td>{job.fileName || '-'}</td>
                  <td className="tts-cell">{jobTtsSummary(job)}</td>
                  <td>
                    <span className={`status ${statusClass(job.status)}`}>{job.status}</span>
                  </td>
                  <td className="actions">
                    <button
                      type="button"
                      disabled={(job.status !== 'CREATED' && job.status !== 'ERROR') || editingJobId === job.id}
                      onClick={() => openEditParams(job)}
                    >
                      {editingJobId === job.id ? '⚙️ Editing' : 'Settings'}
                    </button>
                    <button
                      type="button"
                      disabled={job.status !== 'DONE'}
                      onClick={() => handleDownload(job.id, getDownloadName(job))}
                    >
                      Download
                    </button>
                    <button type="button" onClick={() => handleDelete(job.id)}>Delete</button>
                  </td>
                </tr>
                {editingJobId === job.id && (
                  <tr className="editor-row">
                    <td colSpan="4">
                      <div className="params-editor">
                        <div className="editor-grid">
                          <label>
                            Голос
                            <select
                              value={editingParams.voiceId}
                              onChange={(event) => setEditingParams((prev) => ({ ...prev, voiceId: event.target.value }))}
                            >
                              {VOICE_OPTIONS.map((option) => (
                                <option key={option.value || 'default-voice'} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </label>

                          <label>
                            Швидкість
                            <select
                              value={editingParams.rate}
                              onChange={(event) => setEditingParams((prev) => ({ ...prev, rate: event.target.value }))}
                            >
                              {RATE_OPTIONS.map((option) => (
                                <option key={option.value || 'default-rate'} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </label>

                          <label>
                            Висота тону
                            <select
                              value={editingParams.pitch}
                              onChange={(event) => setEditingParams((prev) => ({ ...prev, pitch: event.target.value }))}
                            >
                              {PITCH_OPTIONS.map((option) => (
                                <option key={option.value || 'default-pitch'} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </label>

                          <label>
                            Гучність
                            <select
                              value={editingParams.volume}
                              onChange={(event) => setEditingParams((prev) => ({ ...prev, volume: event.target.value }))}
                            >
                              {VOLUME_OPTIONS.map((option) => (
                                <option key={option.value || 'default-volume'} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                          </label>
                        </div>
                        <div className="editor-actions">
                          <button
                            type="button"
                            onClick={() => handleStart(job.id)}
                          >
                            Start
                          </button>
                          <button
                            type="button"
                            onClick={closeEditParams}
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    </td>
                  </tr>
                )}
              </>
            ))}
            {jobs.length === 0 && (
              <tr>
                <td colSpan="4" className="empty">No jobs yet</td>
              </tr>
            )}
          </tbody>
        </table>
      </main>
    </div>
  );
}

export default App;
