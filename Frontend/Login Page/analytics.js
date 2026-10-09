/**
 * IMPROV — analytics screen. Three tabs (Overview / Progress / Stats) over the
 * read-only analytics endpoints. All charts are plain CSS — no chart library.
 */
(function () {
  'use strict';

  var E = window.IMPROV.endpoints;
  var WEEKDAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  // ------------------------------------------------------------------- tabs

  document.querySelectorAll('.tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
      document.querySelectorAll('.tab').forEach(function (t) { t.classList.remove('active'); });
      document.querySelectorAll('.tab-panel').forEach(function (p) { p.classList.remove('active'); });
      tab.classList.add('active');
      var panel = document.getElementById('tab-' + tab.dataset.tab);
      if (panel) { panel.classList.add('active'); }
    });
  });

  // ------------------------------------------------------------------- load

  async function load() {
    try {
      var loaded = await Promise.all([
        window.improvApi.get(E.analyticsOverview),
        window.improvApi.get(E.progression)
      ]);
      renderOverview(loaded[0]);
      renderProgress(loaded[1]);
      renderStats(loaded[0]);
    } catch (err) {
      var main = document.querySelector('main');
      main.innerHTML = '<section class="card panel"><p class="muted">' +
        (err.message || 'Could not load analytics.') + '</p></section>';
    }
  }

  // -------------------------------------------------------------- overview

  function renderOverview(overview) {
    var weekXp = overview.weeklyXp.reduce(function (sum, d) { return sum + d.xp; }, 0);
    setText('ov-week-xp', weekXp + ' XP');
    setText('ov-quests', overview.questsCompleted);
    setText('ov-days', overview.activeDays);
    setText('ov-reps', overview.totalReps);

    var chart = document.getElementById('ov-weekly');
    chart.innerHTML = '';
    var max = Math.max(1, overview.weeklyXp.reduce(function (m, d) { return Math.max(m, d.xp); }, 0));
    overview.weeklyXp.forEach(function (day) {
      var bar = document.createElement('div');
      bar.className = 'bar';
      var value = document.createElement('b');
      value.textContent = day.xp;
      var fill = document.createElement('i');
      fill.style.height = Math.round(100 * day.xp / max) + '%';
      var label = document.createElement('span');
      label.textContent = WEEKDAYS[parseDate(day.date).getDay() === 0 ? 6 : parseDate(day.date).getDay() - 1];
      bar.appendChild(value);
      bar.appendChild(fill);
      bar.appendChild(label);
      chart.appendChild(bar);
    });

    var grid = document.getElementById('ov-consistency');
    grid.innerHTML = '';
    overview.consistency.forEach(function (day) {
      var square = document.createElement('i');
      if (day.active) {
        square.className = 'active';
        square.title = day.date + ' — ' + day.xp + ' XP, ' + day.quests + ' quests';
      } else {
        square.title = day.date + ' — rest';
      }
      grid.appendChild(square);
    });
  }

  // -------------------------------------------------------------- progress

  function renderProgress(progression) {
    setText('pg-level', progression.level);
    setText('pg-rank', progression.rank);
    setText('pg-streak', progression.currentStreak);
    setText('pg-best', progression.bestStreak);
    var pct = progression.xpForNextLevel > 0
      ? Math.min(100, Math.round(100 * progression.xpIntoLevel / progression.xpForNextLevel))
      : 100;
    document.getElementById('pg-bar').style.width = pct + '%';
    setText('pg-label', progression.xpIntoLevel + ' / ' + progression.xpForNextLevel +
      ' XP to level ' + (progression.level + 1) + ' · ' + progression.totalXp + ' XP total');
  }

  // ------------------------------------------------------------------ stats

  function renderStats(overview) {
    var stats = overview.stats;
    var total = Math.max(1, stats.str + stats.agi + stats.vit);
    setText('st-str', stats.str);
    setText('st-agi', stats.agi);
    setText('st-vit', stats.vit);
    document.getElementById('st-str-bar').style.width = Math.round(100 * stats.str / total) + '%';
    document.getElementById('st-agi-bar').style.width = Math.round(100 * stats.agi / total) + '%';
    document.getElementById('st-vit-bar').style.width = Math.round(100 * stats.vit / total) + '%';
  }

  // ---------------------------------------------------------------- helpers

  /** 'YYYY-MM-DD' -> local Date (avoids the UTC-midnight parsing trap). */
  function parseDate(value) {
    var parts = value.split('-');
    return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
  }

  function setText(id, value) {
    var el = document.getElementById(id);
    if (el) { el.textContent = value; }
  }

  load();
})();
