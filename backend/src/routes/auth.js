const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const pool = require('../db');

const router = express.Router();

// ثبت نام
router.post('/register', async (req, res) => {
  const { full_name, phone, password, role } = req.body;
  if (!full_name || !phone || !password || !role) {
    return res.status(400).json({ error: 'تمام فیلدها الزامی است' });
  }
  if (!['patient', 'admin', 'specialist'].includes(role)) {
    return res.status(400).json({ error: 'نقش نامعتبر است' });
  }
  try {
    const existing = await pool.query('SELECT id FROM users WHERE phone = $1', [phone]);
    if (existing.rows.length > 0) {
      return res.status(409).json({ error: 'این شماره قبلاً ثبت شده است' });
    }
    const password_hash = await bcrypt.hash(password, 10);
    const result = await pool.query(
      `INSERT INTO users (full_name, phone, password_hash, role)
       VALUES ($1,$2,$3,$4) RETURNING id, full_name, phone, role`,
      [full_name, phone, password_hash, role]
    );
    const user = result.rows[0];
    const tokens = issueTokens(user);
    res.status(201).json({ user, ...tokens });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'خطای سرور' });
  }
});

// ورود
router.post('/login', async (req, res) => {
  const { phone, password } = req.body;
  try {
    const result = await pool.query('SELECT * FROM users WHERE phone = $1', [phone]);
    const user = result.rows[0];
    if (!user) return res.status(401).json({ error: 'شماره یا رمز عبور اشتباه است' });
    const match = await bcrypt.compare(password, user.password_hash);
    if (!match) return res.status(401).json({ error: 'شماره یا رمز عبور اشتباه است' });
    const tokens = issueTokens(user);
    delete user.password_hash;
    res.json({ user, ...tokens });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'خطای سرور' });
  }
});

// دریافت اکسس توکن جدید از روی رفرش توکن
router.post('/refresh', (req, res) => {
  const { refresh_token } = req.body;
  if (!refresh_token) return res.status(400).json({ error: 'رفرش توکن الزامی است' });
  try {
    const payload = jwt.verify(refresh_token, process.env.JWT_REFRESH_SECRET);
    const access_token = jwt.sign(
      { id: payload.id, role: payload.role },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN }
    );
    res.json({ access_token });
  } catch (err) {
    res.status(401).json({ error: 'رفرش توکن نامعتبر است' });
  }
});

function issueTokens(user) {
  const payload = { id: user.id, role: user.role };
  const access_token = jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN
  });
  const refresh_token = jwt.sign(payload, process.env.JWT_REFRESH_SECRET, {
    expiresIn: process.env.JWT_REFRESH_EXPIRES_IN
  });
  return { access_token, refresh_token };
}

module.exports = router;
