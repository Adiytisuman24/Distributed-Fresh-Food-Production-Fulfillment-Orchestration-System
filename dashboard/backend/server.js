const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const cors = require("cors");
const axios = require("axios");
const client = require("prom-client");

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: { origin: "*" },
});

app.use(cors());
app.use(express.json());

// Prometheus Metrics
const register = new client.Registry();
client.collectDefaultMetrics({ register });

app.get("/metrics", async (req, res) => {
  res.setHeader("Content-Type", register.contentType);
  res.end(await register.metrics());
});

// Simulation Control Endpoints
app.post("/api/simulate/failure", (req, res) => {
  const { type, kitchenId } = req.body;
  console.log(`Injecting failure: ${type} in kitchen ${kitchenId}`);
  io.emit("FAILURE_INJECTED", { type, kitchenId, timestamp: new Date() });
  res.json({ status: "Failure Injected" });
});

app.get("/api/health", (req, res) => {
  res.json({ status: "UP", service: "DFS-Dashboard-Backend" });
});

// Real-time Event Handling
io.on("connection", (socket) => {
  console.log("Client connected to Ops Dashboard");

  socket.on("ORDER_ACCEPTED", (data) => {
    io.emit("NEW_ORDER", data);
  });
});

const PORT = 5000;
server.listen(PORT, () => {
  console.log(`Dashboard Backend running on port ${PORT}`);
});


