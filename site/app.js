(() => {
  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
  const storageKey = "focus-garden-preview-v2";

  function read() {
    try { return JSON.parse(localStorage.getItem(storageKey) || "{}"); }
    catch { return {}; }
  }

  function save(patch) {
    const next = { ...read(), ...patch };
    try { localStorage.setItem(storageKey, JSON.stringify(next)); } catch {}
    return next;
  }

  let toastTimer;
  function toast(message) {
    const node = $("#toast");
    if (!node) return;
    node.textContent = message;
    node.classList.add("show");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => node.classList.remove("show"), 2300);
  }

  // Personalised first-run flow. The browser preview uses sample apps only.
  const welcomeSteps = [
    ["A LITTLE SPACE FOR YOU", "Your attention<br>deserves a garden.", "Make room for the things you care about. We'll help the noisy apps take a little step back.", "✿", "Let's begin"],
    ["GENTLE, NOT GUILTY", "Build habits<br>that feel like you.", "Choose your focus rhythm, set kind boundaries and make steady progress—one small session at a time.", "✧", "Continue"],
    ["PRIVATE BY NATURE", "Your progress<br>belongs to you.", "No ads. No paid tiers. No account required. Your personal focus choices stay on this device.", "❀", "Personalise my experience"]
  ];
  const previewApps = [
    { name: "Instagram", icon: "◎", category: "Social · Reels", suggested: true },
    { name: "Snapchat", icon: "👻", category: "Social · Stories", suggested: true },
    { name: "YouTube", icon: "▶", category: "Video · Shorts", suggested: true },
    { name: "TikTok", icon: "♪", category: "Short videos", suggested: true },
    { name: "Facebook", icon: "f", category: "Social feed", suggested: true },
    { name: "Reddit", icon: "◉", category: "Communities", suggested: true },
    { name: "Netflix", icon: "N", category: "Streaming", suggested: false },
    { name: "Discord", icon: "◈", category: "Communities", suggested: false }
  ];
  const setup = {
    studyHours: Number(read().studyHours) || 4,
    screenHours: Number(read().screenHours) || 5,
    focusMinutes: Number(read().focusMinutes) || 25,
    priorities: Array.isArray(read().priorities) ? read().priorities : ["Study & exams"],
    blockedApps: Array.isArray(read().blockedApps) ? read().blockedApps : ["Instagram", "Snapchat", "YouTube", "TikTok"]
  };
  let intro = 0;

  function renderScanner() {
    let html = "<div class='scan-summary'><span class='scan-pulse'>✦</span><div><strong>PREVIEW SCANNER</strong><small>8 sample apps · 6 suggested distractions</small></div><span class='preview-tag'>DEMO</span></div>";
    html += "<p class='question-note'>The web preview cannot inspect your phone. This is a sample scan so you can try the flow. The Android app will scan installed launcher apps locally.</p>";
    html += "<div class='scan-actions'><button type='button' class='text-button' id='selectSuggested'>Select suggestions</button><button type='button' class='text-button' id='clearSelected'>Clear</button></div><div class='scan-app-list'>";
    html += previewApps.map(function(app) {
      const selected = setup.blockedApps.includes(app.name);
      return "<button type='button' class='scan-app " + (selected ? "selected" : "") + "' data-scan-app='" + app.name + "'><span class='scan-app-icon'>" + app.icon + "</span><span class='scan-app-copy'><strong>" + app.name + "</strong><small>" + app.category + "</small></span><span class='scan-check'>" + (selected ? "✓" : "+") + "</span></button>";
    }).join("");
    html += "</div><p class='question-note'>Blocking Instagram pauses the whole app, not only Reels. Blocking YouTube pauses the whole app, not only Shorts.</p>";
    return html;
  }

  function renderStep() {
    const introStep = intro < 3;
    const extra = $("#onboardExtra");
    $("#stepCount").textContent = String(intro + 1).padStart(2, "0") + " / 08";
    $("#onboardProgress").style.width = ((intro + 1) / 8 * 100) + "%";
    $("#onboarding").classList.toggle("question-step", !introStep);
    $("#onboarding").dataset.stage = String(intro);
    $("#skipIntro").hidden = introStep || intro === 7;
    if (introStep) {
      const step = welcomeSteps[intro];
      $("#onboardEyebrow").textContent = step[0];
      $("#onboardTitle").innerHTML = step[1];
      $("#onboardText").textContent = step[2];
      $("#introSymbol").textContent = step[3];
      extra.innerHTML = "";
      $("#onboardNext").innerHTML = step[4] + " <span>→</span>";
      return;
    }

    let eyebrow = "", title = "", description = "", html = "", action = "";
    if (intro === 3) {
      eyebrow = "YOUR DAILY GOAL";
      title = "How many hours would you like to study each day?";
      description = "Choose a target that feels realistic. You can change it whenever you need.";
      html = "<div class='goal-card'><span>YOUR STUDY TARGET</span><strong id='goalValue'>" + setup.studyHours.toFixed(1) + "<small> hours / day</small></strong><input id='studyGoal' type='range' min='1' max='12' step='.5' value='" + setup.studyHours + "'><div class='range-ends'><span>1 hour</span><span>12 hours</span></div></div><p class='question-note'>This is a personal goal, not a judgement or a competition.</p>";
      action = "Continue";
    } else if (intro === 4) {
      eyebrow = "YOUR CURRENT ROUTINE";
      title = "How much time do you spend on your phone?";
      description = "An estimate is enough. Then choose a focus-session length that feels comfortable.";
      const screenChoices = [[2,"Under 3 hours"],[4,"3–4 hours"],[5,"4–6 hours"],[7,"6–8 hours"],[9,"More than 8 hours"]];
      html = "<div class='choice-grid'>" + screenChoices.map(function(choice) {
        return "<button class='choice-card " + (setup.screenHours === choice[0] ? "selected" : "") + "' data-screen-hours='" + choice[0] + "'>" + choice[1] + "</button>";
      }).join("") + "</div><h3 class='question-subtitle'>Your preferred focus session</h3><div class='choice-row'>" + [25,45,60].map(function(minutes) {
        return "<button class='choice-pill " + (setup.focusMinutes === minutes ? "selected" : "") + "' data-focus-minutes='" + minutes + "'>" + minutes + " min</button>";
      }).join("") + "</div>";
      action = "Choose my priorities";
    } else if (intro === 5) {
      eyebrow = "WHAT MATTERS MOST TO YOU?";
      title = "What would you like to improve?";
      description = "Pick the things that matter to you. We'll use them to personalise your focus space.";
      const choices = ["Study & exams","Less scrolling","Better sleep","Healthier routine","More time for hobbies"];
      html = "<div class='priority-list'>" + choices.map(function(value) {
        const selected = setup.priorities.includes(value);
        return "<button class='priority-choice " + (selected ? "selected" : "") + "' data-priority='" + value + "'><span>" + (selected ? "✓" : "＋") + "</span>" + value + "</button>";
      }).join("") + "</div>";
      action = "Scan my apps";
    } else if (intro === 6) {
      eyebrow = "YOUR PHONE, UNDER YOUR CONTROL";
      title = "Let's find your distractions.";
      description = "Review these suggested apps and choose which ones should pause during focus time.";
      html = renderScanner();
      action = "Create my focus plan";
    } else {
      eyebrow = "YOUR PLAN IS TAKING ROOT";
      title = "A little more room for what matters.";
      description = "Your starting plan is ready. You can update every choice later.";
      html = "<div class='summary-card'><div><span>Daily study goal</span><strong>" + setup.studyHours.toFixed(1) + " hours</strong></div><div><span>Daily phone use estimate</span><strong>" + setup.screenHours + " hours</strong></div><div><span>Focus session</span><strong>" + setup.focusMinutes + " minutes</strong></div><div><span>Apps on pause list</span><strong>" + setup.blockedApps.length + "</strong></div><div><span>Your priorities</span><strong>" + setup.priorities.length + " selected</strong></div></div><p class='question-note'>This browser preview only simulates app selection. Real Android blocking requires you to enable the app's Accessibility service yourself.</p>";
      action = "Enter my focus space";
    }

    $("#onboardEyebrow").textContent = eyebrow;
    $("#onboardTitle").textContent = title;
    $("#onboardText").textContent = description;
    $("#introSymbol").textContent = intro === 6 ? "◉" : intro === 7 ? "✿" : "✧";
    extra.innerHTML = html;
    $("#onboardNext").innerHTML = action + " <span>→</span>";

    const goal = $("#studyGoal");
    if (goal) goal.addEventListener("input", function() {
      setup.studyHours = Number(goal.value);
      $("#goalValue").innerHTML = setup.studyHours.toFixed(1) + "<small> hours / day</small>";
    });
    $$(".choice-card", extra).forEach(function(button) {
      button.addEventListener("click", function() { setup.screenHours = Number(button.dataset.screenHours); renderStep(); });
    });
    $$(".choice-pill", extra).forEach(function(button) {
      button.addEventListener("click", function() { setup.focusMinutes = Number(button.dataset.focusMinutes); renderStep(); });
    });
    $$(".priority-choice", extra).forEach(function(button) {
      button.addEventListener("click", function() {
        const value = button.dataset.priority;
        setup.priorities = setup.priorities.includes(value) ? setup.priorities.filter(function(item) { return item !== value; }) : setup.priorities.concat(value);
        renderStep();
      });
    });
    $$(".scan-app", extra).forEach(function(button) {
      button.addEventListener("click", function() {
        const value = button.dataset.scanApp;
        setup.blockedApps = setup.blockedApps.includes(value) ? setup.blockedApps.filter(function(item) { return item !== value; }) : setup.blockedApps.concat(value);
        renderStep();
      });
    });
    $("#selectSuggested")?.addEventListener("click", function() {
      setup.blockedApps = previewApps.filter(function(app) { return app.suggested; }).map(function(app) { return app.name; });
      renderStep();
    });
    $("#clearSelected")?.addEventListener("click", function() { setup.blockedApps = []; renderStep(); });
  }

  function enterApp() {
    save({
      onboarded: true,
      studyHours: setup.studyHours,
      screenHours: setup.screenHours,
      focusMinutes: setup.focusMinutes,
      priorities: setup.priorities,
      blockedApps: setup.blockedApps
    });
    $("#onboarding").classList.add("hidden");
    $("#appShell").classList.remove("hidden");
    blocked = setup.blockedApps.slice();
    if (!read().timerRunning) {
      mode = "focus";
      duration = setup.focusMinutes;
      remaining = duration * 60;
      endAt = null;
      persistTimer();
      paintTimer();
    }
    go("home");
    renderBlocked();
    updateDashboard();
    toast("Your personal focus space is ready");
  }

  if (read().onboarded) {
    $("#onboarding").classList.add("hidden");
    $("#appShell").classList.remove("hidden");
  } else {
    renderStep();
  }
  $("#onboardNext").addEventListener("click", function() {
    if (intro >= 7) enterApp();
    else { intro += 1; renderStep(); }
  });
  $("#skipIntro").addEventListener("click", function() {
    if (intro === 6) setup.blockedApps = [];
    intro += 1;
    renderStep();
  });

  // Single-page navigation.
  function go(page) {
    const target = $("#page-" + page);
    if (!target) return;
    $$(".page").forEach(node => node.classList.toggle("active", node === target));
    $$("[data-page]").forEach(node => node.classList.toggle("active", node.dataset.page === page));
    const labels = { home: "Overview", focus: "Focus room", block: "App garden", schedule: "Schedules", insights: "Insights", planner: "My planner", rewards: "Growth & rewards", settings: "Settings" };
    $("#pageCrumb").textContent = labels[page] || "Overview";
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
  document.addEventListener("click", event => {
    const nav = event.target.closest("[data-page]");
    if (nav) go(nav.dataset.page);
    const jump = event.target.closest("[data-go]");
    if (jump) go(jump.dataset.go);
  });

  // App block list — demo-only rules, saved in this browser.
  const appTiles = $$(".app-tile");
  let blocked = Array.isArray(read().blockedApps) ? read().blockedApps : ["Instagram", "Snapchat", "YouTube", "Facebook"];
  function renderBlocked() {
    appTiles.forEach(tile => {
      const selected = blocked.includes(tile.dataset.app);
      tile.classList.toggle("selected", selected);
      $(".tile-state", tile).textContent = selected ? "Blocked ✓" : "Allowing";
    });
    $("#blockedCount").textContent = blocked.length + " apps taking a pause";
    const count = $(".nav-count");
    if (count) count.textContent = blocked.length;
    save({ blockedApps: blocked });
  }
  appTiles.forEach(tile => tile.addEventListener("click", () => {
    const app = tile.dataset.app;
    blocked = blocked.includes(app) ? blocked.filter(value => value !== app) : [...blocked, app];
    renderBlocked();
    updateDashboard();
    toast(blocked.includes(app) ? app + " added to your block list" : app + " removed from your block list");
  }));
  renderBlocked();

  // Focus timer — end-time based so reloading the preview does not reset a running session.
  const modes = { focus: 25, short: 5, long: 15 };
  const savedTimer = read();
  let mode = modes[savedTimer.timerMode] ? savedTimer.timerMode : "focus";
  let duration = Number(savedTimer.timerDuration) || (mode === "focus" ? setup.focusMinutes : modes[mode]);
  duration = Math.max(1, Math.min(180, duration));
  let endAt = Number(savedTimer.timerEndAt) || null;
  let remaining = Number.isFinite(Number(savedTimer.timerRemaining)) ? Number(savedTimer.timerRemaining) : duration * 60;
  let running = Boolean(savedTimer.timerRunning && endAt);
  let interval = null;

  if (running) {
    remaining = Math.max(0, Math.ceil((endAt - Date.now()) / 1000));
    if (remaining === 0) running = false;
  }

  function persistTimer() {
    save({
      timerMode: mode,
      timerDuration: duration,
      timerRemaining: remaining,
      timerEndAt: running ? endAt : null,
      timerRunning: running
    });
  }
  function formatTime(seconds) {
    return String(Math.floor(seconds / 60)).padStart(2, "0") + ":" + String(seconds % 60).padStart(2, "0");
  }
  function paintTimer() {
    $("#timerValue").textContent = formatTime(remaining);
    $("#timerLabel").textContent = mode === "focus" ? "FOCUS SESSION" : mode === "short" ? "SHORT BREAK" : "LONG BREAK";
    $("#timerStatus").textContent = running ? "You're in your focus flow" : remaining === 0 ? "Session complete. Beautiful work." : "Ready when you are";
    $("#timerToggle").innerHTML = running ? "Pause session" : "Start session <span>→</span>";
    const homeDuration = $("#homeFocusDuration");
    if (homeDuration) homeDuration.textContent = (mode === "focus" ? duration : setup.focusMinutes) + " min";
    const progress = $("#timerProgress");
    if (progress) progress.style.strokeDashoffset = String(766.5 * (1 - remaining / (duration * 60)));
    $$(".mode").forEach(button => button.classList.toggle("active", button.dataset.mode === mode));
    $$(".duration").forEach(button => button.classList.toggle("active", Number(button.dataset.minutes) === duration));
  }
  function pauseTimer() {
    if (running && endAt) remaining = Math.max(0, Math.ceil((endAt - Date.now()) / 1000));
    running = false;
    endAt = null;
    clearInterval(interval);
    interval = null;
    persistTimer();
    paintTimer();
  }
  function finishTimer() {
    running = false;
    endAt = null;
    remaining = 0;
    clearInterval(interval);
    interval = null;
    persistTimer();
    if (mode === "focus") {
      const sessions = Array.isArray(read().previewSessions) ? read().previewSessions : [];
      sessions.push({ completedAt: Date.now(), durationMinutes: duration });
      save({ previewSessions: sessions.slice(-500) });
    }
    updateDashboard();
    paintTimer();
    toast("Session complete. Take a gentle break.");
  }
  function tickTimer() {
    if (!running || !endAt) return;
    remaining = Math.max(0, Math.ceil((endAt - Date.now()) / 1000));
    if (remaining === 0) finishTimer();
    else {
      paintTimer();
      persistTimer();
    }
  }
  function startTimer() {
    if (remaining <= 0) remaining = duration * 60;
    running = true;
    endAt = Date.now() + remaining * 1000;
    clearInterval(interval);
    interval = setInterval(tickTimer, 500);
    persistTimer();
    paintTimer();
  }
  function setMode(nextMode) {
    pauseTimer();
    mode = nextMode;
    duration = modes[nextMode];
    remaining = duration * 60;
    persistTimer();
    paintTimer();
  }
  $$(".mode").forEach(button => button.addEventListener("click", () => setMode(button.dataset.mode)));
  $$(".duration").forEach(button => button.addEventListener("click", () => {
    if (button.dataset.minutes === "custom") {
      const value = Number(prompt("Choose a focus length in minutes (1–180):", "30"));
      if (!Number.isInteger(value) || value < 1 || value > 180) {
        toast("Choose a duration from 1 to 180 minutes");
        return;
      }
      duration = value;
    } else {
      duration = Number(button.dataset.minutes);
    }
    pauseTimer();
    mode = "focus";
    remaining = duration * 60;
    persistTimer();
    paintTimer();
  }));
  $("#timerToggle").addEventListener("click", () => running ? pauseTimer() : startTimer());
  $("#timerReset").addEventListener("click", () => {
    pauseTimer();
    remaining = duration * 60;
    persistTimer();
    paintTimer();
    toast("Timer reset");
  });
  paintTimer();
  if (running) {
    interval = setInterval(tickTimer, 500);
    tickTimer();
  }

  // Personal planner — tasks remain in localStorage between preview visits.
  const taskList = $("#taskList");
  const seededTaskIds = new Set(["math", "science", "creative", "reading"]);
  let tasks = Array.isArray(read().tasks) ? read().tasks.filter(task => !seededTaskIds.has(task.id)) : [];
  function updateTaskCount() {
    const completed = tasks.filter(task => task.completed).length;
    $("#taskCount").textContent = completed + " of " + tasks.length + " done";
    save({ tasks });
  }
  function renderTasks() {
    taskList.replaceChildren();
    if (tasks.length === 0) {
      const empty = document.createElement("p");
      empty.className = "empty-note";
      empty.textContent = "No tasks yet. Add one small next step to begin.";
      taskList.append(empty);
    }
    tasks.forEach(task => {
      const row = document.createElement("div");
      row.className = "task-row";
      row.dataset.taskId = task.id;
      const check = document.createElement("button");
      check.type = "button";
      check.className = "check task-check" + (task.completed ? " checked" : "");
      check.textContent = task.completed ? "✓" : "";
      check.setAttribute("aria-label", (task.completed ? "Mark incomplete: " : "Complete: ") + task.title);
      const body = document.createElement("div");
      const title = document.createElement("strong");
      title.textContent = task.title;
      const detail = document.createElement("small");
      detail.textContent = task.detail || "Personal · No time set";
      body.append(title, detail);
      const time = document.createElement("span");
      time.className = "task-time";
      time.textContent = task.time || "Today";
      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "task-remove";
      remove.textContent = "×";
      remove.setAttribute("aria-label", "Remove " + task.title);
      row.append(check, body, time, remove);
      taskList.append(row);
    });
    updateTaskCount();
    renderTaskPreview();
    updateDashboard();
  }
  function renderTaskPreview() {
    const preview = $("#homeTaskPreview");
    if (!preview) return;
    preview.replaceChildren();
    if (tasks.length === 0) {
      const empty = document.createElement("p");
      empty.className = "empty-note";
      empty.textContent = "No tasks yet. Add one small next step in your planner.";
      preview.append(empty);
      return;
    }
    tasks.slice(0, 3).forEach(task => {
      const row = document.createElement("div");
      row.className = "task-row";
      const check = document.createElement("span");
      check.className = "check" + (task.completed ? " checked" : "");
      check.textContent = task.completed ? "✓" : "";
      const body = document.createElement("div");
      const title = document.createElement("strong");
      title.textContent = task.title;
      const detail = document.createElement("small");
      detail.textContent = task.detail || "Personal · No time set";
      body.append(title, detail);
      const time = document.createElement("span");
      time.className = "task-time";
      time.textContent = task.time || "Today";
      row.append(check, body, time);
      preview.append(row);
    });
  }
  function formatMinutes(totalMinutes) {
    const safe = Math.max(0, Math.floor(totalMinutes));
    const hours = Math.floor(safe / 60);
    const minutes = safe % 60;
    return hours > 0 ? hours + "h " + minutes + "m" : safe + "m";
  }
  function updateDashboard() {
    const state = read();
    const allSessions = Array.isArray(state.previewSessions) ? state.previewSessions.filter(session =>
      Number.isFinite(Number(session.completedAt)) && Number.isFinite(Number(session.durationMinutes)) && Number(session.durationMinutes) > 0
    ) : [];
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const tomorrowStartDate = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1);
    const todayEnd = tomorrowStartDate.getTime();
    const todaySessions = allSessions.filter(session => session.completedAt >= todayStart && session.completedAt < todayEnd);
    const todayMinutes = todaySessions.reduce((sum, session) => sum + session.durationMinutes, 0);
    const goalHours = Math.min(12, Math.max(1, Number(state.studyHours) || 4));
    const goalMinutes = Math.round(goalHours * 60);
    const percent = Math.min(100, Math.round(todayMinutes / goalMinutes * 100));
    const goalLabel = Number.isInteger(goalHours) ? String(goalHours) : goalHours.toFixed(1);

    const dateLabel = $("#homeDateLabel");
    if (dateLabel) dateLabel.textContent = now.toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" }).toUpperCase();
    const dateChip = $("#homeDateChip");
    if (dateChip) dateChip.textContent = now.toLocaleDateString(undefined, { month: "short", day: "numeric" }).toUpperCase();
    const goalValue = $("#homeGoalHours");
    if (goalValue) goalValue.innerHTML = goalLabel + "<span> hours</span>";
    const goalBar = $("#homeGoalProgress");
    if (goalBar) goalBar.style.width = percent + "%";
    const goalText = $("#homeGoalProgressText");
    if (goalText) goalText.textContent = formatMinutes(todayMinutes) + " of " + goalLabel + "h completed today";
    const goalTitle = $("#homeGoalTitle");
    if (goalTitle) goalTitle.textContent = todayMinutes >= goalMinutes ? "You reached today's target. Take a moment to appreciate that." : "A steady rhythm, built one session at a time.";

    const focusValue = $("#homeFocusTime");
    if (focusValue) focusValue.textContent = formatMinutes(todayMinutes);
    const focusCaption = $("#homeFocusCaption");
    if (focusCaption) focusCaption.textContent = todaySessions.length + " completed session" + (todaySessions.length === 1 ? "" : "s") + " today";
    const blockedCount = $("#homeBlockedCount");
    if (blockedCount) blockedCount.textContent = String(blocked.length);
    const taskCount = $("#homeTaskCount");
    const completedTasks = tasks.filter(task => task.completed).length;
    if (taskCount) taskCount.textContent = completedTasks + " / " + tasks.length;
    const taskCaption = $("#homeTaskCaption");
    if (taskCaption) taskCaption.textContent = tasks.length === 0 ? "No tasks added yet" : (tasks.length - completedTasks) + " remaining";
    const sessionCount = $("#homeSessionCount");
    if (sessionCount) sessionCount.textContent = String(todaySessions.length);

    const weekStart = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6);
    const dayTotals = [];
    for (let dayIndex = 0; dayIndex < 7; dayIndex++) {
      const day = new Date(weekStart.getFullYear(), weekStart.getMonth(), weekStart.getDate() + dayIndex);
      const start = day.getTime();
      const end = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1).getTime();
      const daySessions = allSessions.filter(session => session.completedAt >= start && session.completedAt < end);
      dayTotals.push({ date: day, minutes: daySessions.reduce((sum, session) => sum + session.durationMinutes, 0), count: daySessions.length });
    }
    const weekMinutes = dayTotals.reduce((sum, day) => sum + day.minutes, 0);
    const weekSessions = dayTotals.reduce((sum, day) => sum + day.count, 0);
    const activeDays = dayTotals.filter(day => day.count > 0).length;
    const bestDay = dayTotals.reduce((best, day) => day.minutes > (best?.minutes || 0) ? day : best, null);
    const weekFocus = $("#insightWeekFocus");
    if (weekFocus) weekFocus.textContent = formatMinutes(weekMinutes);
    const weekCaption = $("#insightWeekCaption");
    if (weekCaption) weekCaption.textContent = weekSessions + " completed sessions in the last 7 days";
    const bestDayNode = $("#insightBestDay");
    if (bestDayNode) bestDayNode.textContent = bestDay && bestDay.minutes > 0 ? bestDay.date.toLocaleDateString(undefined, { weekday: "long" }) : "—";
    const bestDayTime = $("#insightBestDayTime");
    if (bestDayTime) bestDayTime.textContent = bestDay && bestDay.minutes > 0 ? formatMinutes(bestDay.minutes) + " of focus" : "No sessions recorded yet";
    const insightSessions = $("#insightSessionCount");
    if (insightSessions) insightSessions.textContent = String(weekSessions);
    const activeDaysNode = $("#insightActiveDays");
    if (activeDaysNode) activeDaysNode.textContent = activeDays + " active days this week";

    const chart = $("#insightWeeklyBars");
    if (chart) {
      const bars = $(".bar-set", chart);
      dayTotals.forEach((day, index) => {
        const bar = bars[index];
        if (!bar) return;
        const height = Math.min(100, day.minutes / 240 * 100);
        const fill = bar.querySelector("div");
        const value = bar.querySelector("b");
        const label = bar.querySelector("span");
        if (fill) fill.style.height = height + "%";
        if (value) value.textContent = day.minutes > 0 ? formatMinutes(day.minutes) : "0m";
        if (label) label.textContent = day.date.toLocaleDateString(undefined, { weekday: "short" });
      });
    }

    const selectedApps = $("#insightSelectedApps");
    if (selectedApps) {
      selectedApps.replaceChildren();
      if (blocked.length === 0) {
        const empty = document.createElement("p");
        empty.className = "empty-note";
        empty.textContent = "Your pause list is empty. Choose apps in the app garden.";
        selectedApps.append(empty);
      } else {
        blocked.forEach(app => {
          const row = document.createElement("div");
          row.className = "usage-row";
          const symbol = document.createElement("span");
          const logoFiles = { Instagram: "instagram", Snapchat: "snapchat", YouTube: "youtube", Facebook: "facebook", TikTok: "tiktok", X: "x", Reddit: "reddit", Discord: "discord" };
          const logoFile = logoFiles[app];
          symbol.className = "app-logo" + (logoFile ? " " + (logoFile === "x" ? "x-logo" : logoFile) : "");
          if (logoFile) {
            const image = document.createElement("img");
            image.src = "./assets/brands/" + logoFile + ".svg";
            image.alt = "";
            image.setAttribute("aria-hidden", "true");
            symbol.append(image);
          } else {
            symbol.textContent = app.slice(0, 1).toUpperCase();
          }
          const body = document.createElement("div");
          const title = document.createElement("strong");
          title.textContent = app;
          const detail = document.createElement("small");
          detail.textContent = "Selected for your preview block list";
          body.append(title, detail);
          row.append(symbol, body);
          selectedApps.append(row);
        });
      }
    }

    const totalMinutes = allSessions.reduce((sum, session) => sum + session.durationMinutes, 0);
    const rewardFocus = $("#rewardsFocusTime");
    if (rewardFocus) rewardFocus.innerHTML = formatMinutes(totalMinutes) + " <small>TOTAL FOCUS TIME</small>";
    const focusedDays = new Set(allSessions.map(session => {
      const date = new Date(session.completedAt);
      return date.getFullYear() + "-" + date.getMonth() + "-" + date.getDate();
    })).size;
    const firstBadge = $("#badgeFirst");
    if (firstBadge) {
      firstBadge.classList.toggle("unlocked", allSessions.length > 0);
      firstBadge.querySelector("b").textContent = allSessions.length > 0 ? "Earned ✓" : "Not yet earned";
    }
    const tenHourBadge = $("#badgeTenHours");
    if (tenHourBadge) {
      tenHourBadge.classList.toggle("unlocked", totalMinutes >= 600);
      tenHourBadge.querySelector("b").textContent = totalMinutes >= 600 ? "Earned ✓" : formatMinutes(totalMinutes) + " / 10h";
    }
    const sevenDayBadge = $("#badgeSevenDays");
    if (sevenDayBadge) {
      sevenDayBadge.classList.toggle("unlocked", focusedDays >= 7);
      sevenDayBadge.querySelector("b").textContent = focusedDays >= 7 ? "Earned ✓" : focusedDays + " / 7 active days";
    }
  }
  taskList.addEventListener("click", event => {
    const row = event.target.closest(".task-row");
    if (!row) return;
    const task = tasks.find(item => item.id === row.dataset.taskId);
    if (!task) return;
    if (event.target.closest(".task-check")) task.completed = !task.completed;
    else if (event.target.closest(".task-remove")) tasks = tasks.filter(item => item.id !== task.id);
    else return;
    renderTasks();
  });
  $("#taskForm").addEventListener("submit", event => {
    event.preventDefault();
    const input = $("#newTask");
    const title = input.value.trim();
    if (!title) return;
    tasks.push({ id: String(Date.now()), title, detail: "Personal · No time set", time: "Today", completed: false });
    input.value = "";
    renderTasks();
    toast("A new step has been added");
  });
  renderTasks();
  updateDashboard();

  // Schedules are interactive preview rules; they do not schedule Android blocking.
  const scheduleMain = $(".schedule-main");
  let scheduleStates = read().scheduleStates || {};
  $$(".schedule-item", scheduleMain).forEach((row, index) => {
    const id = row.dataset.scheduleId || "default-" + index;
    row.dataset.scheduleId = id;
    const toggle = $("input[type=checkbox]", row);
    if (Object.prototype.hasOwnProperty.call(scheduleStates, id)) toggle.checked = scheduleStates[id];
    toggle.addEventListener("change", () => {
      scheduleStates[id] = toggle.checked;
      save({ scheduleStates });
    });
  });
  let customSchedules = Array.isArray(read().customSchedules) ? read().customSchedules : [];
  function renderCustomSchedule(schedule) {
    const row = document.createElement("div");
    row.className = "schedule-item";
    row.dataset.scheduleId = schedule.id;
    const symbol = document.createElement("div");
    symbol.className = "schedule-symbol mint";
    symbol.textContent = "✧";
    const info = document.createElement("div");
    info.className = "schedule-info";
    const title = document.createElement("strong");
    title.textContent = schedule.title;
    const details = document.createElement("small");
    details.textContent = schedule.details;
    const chips = document.createElement("div");
    chips.className = "day-chips";
    const chip = document.createElement("i");
    chip.textContent = "Custom";
    chips.append(chip);
    info.append(title, details, chips);
    const label = document.createElement("label");
    label.className = "switch";
    const input = document.createElement("input");
    input.type = "checkbox";
    input.checked = Boolean(schedule.enabled);
    const knob = document.createElement("span");
    label.append(input, knob);
    input.addEventListener("change", () => {
      schedule.enabled = input.checked;
      save({ customSchedules });
    });
    row.append(symbol, info, label);
    scheduleMain.append(row);
  }
  customSchedules.forEach(renderCustomSchedule);
  $("#addSchedule").addEventListener("click", () => {
    const title = prompt("Name this focus schedule:");
    if (!title || !title.trim()) return;
    const details = prompt("When should it run? Example: Weekdays · 4:00 PM – 6:00 PM", "Weekdays · 4:00 PM – 6:00 PM");
    if (!details || !details.trim()) return;
    const schedule = { id: "custom-" + Date.now(), title: title.trim(), details: details.trim(), enabled: true };
    customSchedules.push(schedule);
    renderCustomSchedule(schedule);
    save({ customSchedules });
    toast("Your preview schedule was added");
  });

  // Preferences and data export.
  const goalSlider = $("#settingsStudyGoal");
  if (goalSlider) {
    goalSlider.value = String(setup.studyHours);
    const goalValue = $("#settingsStudyGoalValue");
    const updateGoal = () => {
      setup.studyHours = Number(goalSlider.value);
      const label = Number.isInteger(setup.studyHours) ? String(setup.studyHours) : setup.studyHours.toFixed(1);
      if (goalValue) goalValue.textContent = label + "h";
      save({ studyHours: setup.studyHours });
      updateDashboard();
    };
    goalSlider.addEventListener("input", updateGoal);
    updateGoal();
  }
  const intent = $("#focusIntent");
  intent.value = read().focusIntent || "";
  intent.addEventListener("change", () => save({ focusIntent: intent.value }));
  $("#focusBlock").checked = read().focusBlock !== false;
  $("#focusBlock").addEventListener("change", event => save({ focusBlock: event.target.checked }));

  const settingsRows = $$("#page-settings .setting-row");
  settingsRows.forEach(row => {
    const label = $("strong", row)?.textContent;
    const input = $("input[type=checkbox]", row);
    if (!input) return;
    if (label === "Reduce motion") {
      input.checked = Boolean(read().reduceMotion);
      document.body.classList.toggle("reduce-motion", input.checked);
      input.addEventListener("change", () => {
        save({ reduceMotion: input.checked });
        document.body.classList.toggle("reduce-motion", input.checked);
      });
    } else if (label === "Gentle reminders") {
      input.checked = Boolean(read().gentleReminders);
      input.addEventListener("change", () => {
        save({ gentleReminders: input.checked });
        toast("Browser reminder delivery is not enabled in this preview");
      });
    } else if (label === "Dark appearance") {
      input.checked = true;
      input.addEventListener("change", () => {
        input.checked = true;
        toast("The native light theme is a separate design milestone");
      });
    }
  });

  $("#exportData").addEventListener("click", () => {
    const blob = new Blob([JSON.stringify(read(), null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "focus-garden-preview-data.json";
    link.click();
    URL.revokeObjectURL(url);
    toast("Your preview data export is ready");
  });
  $("#clearData").addEventListener("click", () => {
    if (!confirm("Clear your locally saved preview choices?")) return;
    localStorage.removeItem(storageKey);
    window.location.reload();
  });

  // Installable/offline shell for GitHub Pages.
  if ("serviceWorker" in navigator && window.location.protocol === "https:") {
    navigator.serviceWorker.register("./sw.js").catch(() => {});
  }
  window.addEventListener("beforeunload", () => clearInterval(interval));
})();