const express = require('express');
const pool = require('../db');
const { authRequired, requireRole } = require('../middleware/auth');

const router = express.Router();

// لیست پزشکان/متخصصین برای انتخاب بیمار
router.get('/', authRequired, async (req, res) => {
  const result = await pool.query(
    `SELECT u.id, u.full_name, sp.specialty, sp.bio
     FROM users u JOIN specialist_profiles sp ON sp.user_id = u.id
     WHERE u.role = 'specialist' AND sp.is_active = TRUE
     ORDER BY u.full_name`
  );
  res.json(result.rows);
});

// متخصص پروفایل خودش را تکمیل/به‌روزرسانی می‌کند
router.put('/me', authRequired, requireRole('specialist'), async (req, res) => {
  const { specialty, bio } = req.body;
  const existing = await pool.query('SELECT id FROM specialist_profiles WHERE user_id=$1', [req.user.id]);
  if (existing.rows.length > 0) {
    await pool.query('UPDATE specialist_profiles SET specialty=$1, bio=$2 WHERE user_id=$3', [specialty, bio, req.user.id]);
  } else {
    await pool.query('INSERT INTO specialist_profiles (user_id, specialty, bio) VALUES ($1,$2,$3)', [req.user.id, specialty, bio]);
  }
  res.json({ ok: true });
});

module.exports = router;
