const express = require('express');
const pool = require('../db');
const { authRequired } = require('../middleware/auth');
const { notifyUser } = require('../ws/hub');

const router = express.Router();

// ارسال پیام در یک درخواست (بیمار یا متخصص طرف همان درخواست)
router.post('/:requestId', authRequired, async (req, res) => {
  const { body } = req.body;
  const requestId = req.params.requestId;

  const reqRow = await pool.query('SELECT * FROM requests WHERE id=$1', [requestId]);
  const request = reqRow.rows[0];
  if (!request) return res.status(404).json({ error: 'درخواست یافت نشد' });
  if (![request.patient_id, request.specialist_id].includes(req.user.id)) {
    return res.status(403).json({ error: 'دسترسی مجاز نیست' });
  }

  const result = await pool.query(
    `INSERT INTO messages (request_id, sender_id, body) VALUES ($1,$2,$3) RETURNING *`,
    [requestId, req.user.id, body]
  );
  const message = result.rows[0];
  const otherUserId = request.patient_id === req.user.id ? request.specialist_id : request.patient_id;
  notifyUser(otherUserId, { type: 'new_message', message });
  res.status(201).json(message);
});

// دریافت تاریخچه پیام‌های یک درخواست
router.get('/:requestId', authRequired, async (req, res) => {
  const requestId = req.params.requestId;
  const result = await pool.query(
    'SELECT * FROM messages WHERE request_id=$1 ORDER BY created_at ASC',
    [requestId]
  );
  res.json(result.rows);
});

module.exports = router;
