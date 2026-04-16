# 🤖 Nithi — My Assistant

Personal AI Assistant — PWA + Cloud Deploy ready!

---

## Run Locally

```cmd
cd nithi-web
mvn clean package -q
set ANTHROPIC_API_KEY=your_key
set EMAIL_ADDRESS=your@gmail.com
set EMAIL_PASSWORD=your_app_password
java -jar target\nithi-assistant-1.0.0.jar
```
Open → http://localhost:8080

---

## Install on Phone (PWA)

1. Run the server
2. Open Chrome on phone → go to `http://YOUR_IP:8080`
3. Tap **3 dots menu** → **Add to Home screen**
4. Name it **Nithi** → tap Add
5. Nithi icon appears on home screen like a real app! 📱

---

## Deploy to Railway (Use anywhere!)

### Step 1 — Push to GitHub
```cmd
cd nithi-web
git init
git add .
git commit -m "Nithi Assistant"
git remote add origin https://github.com/YOUR_USERNAME/nithi-web.git
git push -u origin main
```

### Step 2 — Deploy on Railway
1. Go to https://railway.app
2. Sign up with GitHub
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your nithi-web repo
5. Add Variables:
   - ANTHROPIC_API_KEY = your key
   - EMAIL_ADDRESS = your@gmail.com
   - EMAIL_PASSWORD = your app password
6. Click Deploy
7. Railway gives you a public URL!
8. Open on phone from anywhere 🌍

---

## Chat Commands
```
Send email to john@gmail.com and say meeting at 3pm
Order biryani on Swiggy
Call +911234567890 and say I will be late
What is the weather today?
```
