// جایگزین ساده و بدون Firebase برای پوش نوتیفیکیشن:
// هر کاربر پس از اتصال WebSocket، توکن JWT خودش را ارسال می‌کند تا شناسایی شود.
// وقتی رویدادی رخ می‌دهد (درخواست جدید، پیام جدید، ...) پیام از طریق سوکت باز کاربر ارسال می‌شود.
// اگر کاربر آنلاین نباشد، پیام را می‌توان در جدولی برای دریافت بعدی (polling) ذخیره کرد.

const jwt = require('jsonwebtoken');

const connections = new Map(); // userId -> Set(ws)

function initWebsocket(wss) {
  wss.on('connection', (ws) => {
    let userId = null;

    ws.on('message', (raw) => {
      try {
        const data = JSON.parse(raw);
        if (data.type === 'auth') {
          const payload = jwt.verify(data.token, process.env.JWT_SECRET);
          userId = payload.id;
          if (!connections.has(userId)) connections.set(userId, new Set());
          connections.get(userId).add(ws);
          ws.send(JSON.stringify({ type: 'auth_ok' }));
        }
      } catch (err) {
        ws.send(JSON.stringify({ type: 'error', error: 'احراز هویت سوکت ناموفق بود' }));
      }
    });

    ws.on('close', () => {
      if (userId && connections.has(userId)) {
        connections.get(userId).delete(ws);
      }
    });
  });
}

function notifyUser(userId, payload) {
  const sockets = connections.get(userId);
  if (!sockets) return; // کاربر آفلاین است؛ در نسخه بعدی می‌توان در جدول notifications ذخیره کرد
  const msg = JSON.stringify(payload);
  for (const ws of sockets) {
    if (ws.readyState === 1) ws.send(msg);
  }
}

module.exports = { initWebsocket, notifyUser };
