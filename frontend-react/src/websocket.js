import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const WS_PATH = process.env.REACT_APP_WS_URL || "/ws";

export function createJobsSocket({ token, onEvent, onStateChange, onError }) {
  const client = new Client({
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    webSocketFactory: () => new SockJS(WS_PATH),
  });

  client.onConnect = () => {
    onStateChange("connected");
    client.subscribe("/user/queue/jobs", (message) => {
      try {
        const payload = JSON.parse(message.body);
        onEvent(payload);
      } catch (error) {
        onError(error);
      }
    });
  };

  client.onWebSocketClose = () => {
    onStateChange("reconnecting");
  };

  client.onStompError = (frame) => {
    onStateChange("error");
    onError(new Error(frame.headers.message || "WebSocket/STOMP error"));
  };

  client.onWebSocketError = (event) => {
    onStateChange("error");
    onError(new Error(event.type || "WebSocket transport error"));
  };

  return {
    connect() {
      onStateChange("connecting");
      client.activate();
    },
    disconnect() {
      return client.deactivate();
    },
  };
}

