require('dotenv').config();
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const db = require('./db/connection');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  }
});

const PORT = process.env.PORT || 3000;
const SECRET_KEY = process.env.JWT_SECRET || 'FARM_SOVEREIGN_KEY_0x777';

app.use(cors());
app.use(express.json());

// Auth Middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  if (!token) return res.sendStatus(401);

  jwt.verify(token, SECRET_KEY, (err, user) => {
    if (err) return res.sendStatus(403);
    req.user = user;
    next();
  });
};

// API Endpoints
app.get('/api/farmers', authenticateToken, (req, res) => {
  db.all("SELECT * FROM farmers", (err, rows) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(rows);
  });
});

app.post('/api/farmers', authenticateToken, (req, res) => {
  const { name, farmName, address, phone } = req.body;
  const id = require('uuid').v4();
  db.run("INSERT INTO farmers (id, name, farmName, address, phone) VALUES (?, ?, ?, ?, ?)",
    [id, name, farmName, address, phone],
    function(err) {
      if (err) return res.status(500).json({ error: err.message });
      res.status(201).json({ id, name, farmName });
    }
  );
});

// WebSocket Integration
io.use((socket, next) => {
  const token = socket.handshake.auth.token;
  if (token) {
    jwt.verify(token, SECRET_KEY, (err, user) => {
      if (err) return next(new Error("Authentication error"));
      socket.user = user;
      next();
    });
  } else {
    next(); // Allow unauth for now or block
  }
});

io.on('connection', (socket) => {
  console.log(`[0xAF1] Client Connected: ${socket.id}`);

  socket.on('join_farm', (data) => {
    socket.join(`farm_${data.farmId}`);
    console.log(`[0xAF1] Worker ${data.workerId} joined farm ${data.farmId}`);
    socket.to(`farm_${data.farmId}`).emit('worker_joined', { workerId: data.workerId });
  });

  socket.on('location_update', (data) => {
    console.log(`[0xAF1] Location Update from ${data.workerId}: ${data.latitude}, ${data.longitude}`);
    io.to(`farm_${data.farmId}`).emit('location_broadcast', data);
  });

  socket.on('disconnect', () => {
    console.log(`[0xAF1] Client Disconnected: ${socket.id}`);
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[0xAF1] Unified Grid Ignite: Server running on port ${PORT}`);
});
