const express = require('express');
const pool = require('../db');
const { authRequired, requireRole } = require('../middleware/auth');
const { notifyUser } = require('../ws/hub');

const router = express.Router();

// بیمار: ثبت درخواست ویزیت برای یک متخصص
router.post('/', authRequired, requireRole('patient'), async (req, res) => {
  const { specialist_id, note } = req.body;
  const result = await pool.query(
    `INSERT INTO requests (patient_id, specialist_id, note) VALUES ($1,$2,$3) RETURNING *`,
    [req.user.id, specialist_id, note || null]
  );
  const request = result.rows[0];
  notifyUser(specialist_id, { type: 'new_request', request });
  res.status(201).json(request);
});

// متخصص: مشاهده کارتابل (درخواست‌های وارده)
router.get('/inbox', authRequired, requireRole('specialist'), async (req, res) => {
  const result = await pool.query(
    `SELECT r.*, u.full_name AS patient_name
     FROM requests r JOIN users u ON u.id = r.patient_id
     WHERE r.specialist_id = $1
     ORDER BY r.created_at DESC`,
    [req.user.id]
  );
  res.json(result.rows);
});

// بیمار: مشاهده درخواست‌های خودش
router.get('/mine', authRequired, requireRole('patient'), async (req, res) => {
  const result = await pool.query(
    `SELECT r.*, u.full_name AS specialist_name
     FROM requests r JOIN users u ON u.id = r.specialist_id
     WHERE r.patient_id = $1
     ORDER BY r.created_at DESC`,
    [req.user.id]
  );
  res.json(result.rows);
});

// متخصص: تغییر وضعیت درخواست (accepted/rejected/completed)
router.patch('/:id/status', authRequired, requireRole('specialist'), async (req, res) => {
  const { status } = req.body;
  if (!['accepted', 'rejected', 'completed'].includes(status)) {
    return res.status(400).json({ error: 'وضعیت نامعتبر است' });
  }
  const result = await pool.query(
    `UPDATE requests SET status=$1, updated_at=NOW()
     WHERE id=$2 AND specialist_id=$3 RETURNING *`,
    [status, req.params.id, req.user.id]
  );
  if (result.rows.length === 0) return res.status(404).json({ error: 'یافت نشد' });
  const request = result.rows[0];
  notifyUser(request.patient_id, { type: 'request_status_changed', request });
  res.json(request);
});

// ادمین: مشاهده همه درخواست‌ها
router.get('/all', authRequired, requireRole('admin'), async (req, res) => {
  const result = await pool.query(
    `SELECT r.*, p.full_name AS patient_name, s.full_name AS specialist_name
     FROM requests r
     JOIN users p ON p.id = r.patient_id
     JOIN users s ON s.id = r.specialist_id
     ORDER BY r.created_at DESC`
  );
  res.json(result.rows);
});

module.exports = router;
