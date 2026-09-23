# ویزیت آنلاین پزشک و متخصص تغذیه (بدون Firebase)

## ساختار پروژه
```
doctor-app/
  backend/     -> Node.js/Express + PostgreSQL + WebSocket
  android/     -> اپ اندروید Kotlin + Jetpack Compose + Retrofit
  .github/workflows/ -> بیلد خودکار (بدون امضای APK)
```

## راه‌اندازی بک‌اند
```bash
cd backend
cp .env.example .env   # مقادیر JWT_SECRET را عوض کن
docker compose up -d   # دیتابیس و سرور بالا می‌آید
```
یا بدون Docker:
```bash
cd backend
npm install
npm run migrate   # ساخت جداول
npm run dev
```

## نقش‌ها
- **patient (بیمار):** ثبت‌نام، انتخاب متخصص، ارسال درخواست ویزیت، چت
- **specialist (پزشک/متخصص تغذیه):** مشاهده کارتابل، پذیرش/رد درخواست، چت، ثبت نسخه
- **admin (مدیریت):** مشاهده همه درخواست‌ها، مدیریت متخصصین

## نوتیفیکیشن (بدون FCM/Firebase)
از یک اتصال WebSocket ساده استفاده شده (`backend/src/ws/hub.js` و
`android/.../data/NotificationSocketService.kt`). وقتی درخواست جدید یا پیام
جدیدی ثبت می‌شود، اگر کاربر مقصد آنلاین باشد، آنی پیام دریافت می‌کند.
برای نسخه تولیدی بهتر است [UnifiedPush](https://unifiedpush.org) یا
Foreground Service پایدارتر اضافه شود.

## اندروید
پروژه در `android/` یک پروژه استاندارد Gradle/Kotlin است — با Android Studio باز کن.
آدرس بک‌اند را در `android/app/build.gradle` (فیلدهای `API_BASE_URL` و `WS_BASE_URL`) تنظیم کن.

## نکات باقی‌مانده برای توسعه
- صفحات Compose برای نقش پزشک، ادمین، کارتابل و چت هنوز باید اضافه شوند (فعلاً فقط صفحه ورود ساخته شده)
- ذخیره امن توکن (EncryptedSharedPreferences) به‌جای متغیر ساده در حافظه
- آپلود فایل/عکس نسخه با MinIO یا فضای دیسک سرور
- Workflow بیلد فعلی فقط APK دیباگ (بدون امضا) می‌سازد؛ هر زمان خواستی، بیلد امضاشده Release هم اضافه می‌کنم
