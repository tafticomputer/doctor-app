require('dotenv').config();
const express = require('express');
const cors = require('cors');
const http = require('http');
const { WebSocketServer } = require('ws');

const authRoutes = require('./routes/auth');
const specialistRoutes = require('./routes/specialists');
const requestRoutes = require('./routes/requests');
const messageRoutes = require('./routes/messages');
const { initWebsocket } = require('./ws/hub');

const app = express();
app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => res.json({ ok: true }));

app.use('/api/auth', authRoutes);
app.use('/api/specialists', specialistRoutes);
app.use('/api/requests', requestRoutes);
app.use('/api/messages', messageRoutes);

const server = http.createServer(app);
const wss = new WebSocketServer({ server, path: '/ws' });
initWebsocket(wss);

const PORT = process.env.PORT || 4000;
server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
