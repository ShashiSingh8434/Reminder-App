# Reminder App ⏰

A fully functional **Reminder App** for Android, built with **Java** and **Android Studio**, that allows users to set multiple reminders with customizable audio notifications. The app uses **AlarmManager**, **Room Database**, and **Notification System** to ensure alarms ring at the exact scheduled time.

---

## Features

- ✅ **Set Multiple Reminders**  
  Users can create, edit, and manage multiple reminders.

- ✅ **Audio Alerts**  
  Each reminder can play a selected audio track when triggered. Music loops until stopped manually.

- ✅ **Exact Alarm Scheduling**  
  Reminders ring at the exact chosen time using **AlarmManager** with `setExactAndAllowWhileIdle()`.

- ✅ **Stop Button on Notification**  
  Users can stop the alarm directly from the notification without opening the app.

- ✅ **Persistent Storage**  
  All reminders are saved in **Room Database** and persist even after the app is closed.

- ✅ **Custom Notification Channel**  
  Compatible with Android O+ with proper notification channel handling.

---

## Screenshots


https://github.com/user-attachments/assets/1b9c1977-b789-41ea-aa12-e04f4d7ce1c7


<img width="1080" height="2400" alt="a" src="https://github.com/user-attachments/assets/103ef11a-a88a-4ae1-82bc-c69cf1cf4603" />
<img width="1080" height="2400" alt="b" src="https://github.com/user-attachments/assets/5377e22d-7e4b-428e-8491-7243e57dc356" />
<img width="1080" height="2400" alt="c" src="https://github.com/user-attachments/assets/2a99ddf8-f85e-420e-b470-d261119edfd3" />


---

## Tech Stack

- **Language:** Java  
- **IDE:** Android Studio  
- **Database:** RoomDB 
- **Notifications & Alarms:** AlarmManager, NotificationCompat  
- **Media:** MediaPlayer for looping audio alerts  

---

## How It Works

1. User opens the app and clicks **Add Reminder**.  
2. Selects **time**, **title**, **detail**, and **audio track**.  
3. Reminder is saved in **Room Database**.  
4. AlarmManager schedules the reminder at the exact time.  
5. When triggered:  
   - Notification appears with title, detail, and **Stop button**.  
   - Selected audio plays in a loop until stopped.  

---

## Installation

1. Clone the repository:

```bash
git clone https://github.com/ShashiSingh8434/Reminder-App.git
