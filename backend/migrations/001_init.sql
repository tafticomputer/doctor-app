-- نقش‌ها: patient, admin, specialist
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  full_name VARCHAR(150) NOT NULL,
  phone VARCHAR(20) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL CHECK (role IN ('patient','admin','specialist')),
  created_at TIMESTAMP DEFAULT NOW()
);

-- پروفایل تکمیلی پزشک/متخصص تغذیه
CREATE TABLE IF NOT EXISTS specialist_profiles (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  specialty VARCHAR(100) NOT NULL, -- 'doctor' یا 'nutritionist' یا زیرتخصص
  bio TEXT,
  is_active BOOLEAN DEFAULT TRUE
);

-- درخواست ویزیت بیمار از یک متخصص -> کارتابل متخصص
CREATE TABLE IF NOT EXISTS requests (
  id SERIAL PRIMARY KEY,
  patient_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  specialist_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending','accepted','rejected','completed')),
  note TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- پیام‌های چت بین بیمار و متخصص، درون یک درخواست
CREATE TABLE IF NOT EXISTS messages (
  id SERIAL PRIMARY KEY,
  request_id INTEGER REFERENCES requests(id) ON DELETE CASCADE,
  sender_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  body TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

-- نسخه/توصیه ثبت شده توسط متخصص
CREATE TABLE IF NOT EXISTS prescriptions (
  id SERIAL PRIMARY KEY,
  request_id INTEGER REFERENCES requests(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_requests_specialist ON requests(specialist_id, status);
CREATE INDEX IF NOT EXISTS idx_messages_request ON messages(request_id);
