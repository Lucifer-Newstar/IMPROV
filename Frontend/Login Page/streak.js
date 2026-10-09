/**
 * IMPROV — streak screen: current/best streak, a month calendar (filled day =
 * at least one quest completed) and a timeline of every active day.
 */
(function () {
  'use strict';

  var E = window.IMPROV.endpoints;
  var WEEKDAYS = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
  var currentMonth = null; // 'YYYY-MM'

  // ------------------------------------------------------------------- load

  async function load() {
    try {
      var streak = await window.improvApi.get(E.analyticsStreak);
      setText('sk-current', streak.currentStreak);
      setText('sk-best', streak.bestStreak);
      renderTimeline(streak.days);
      currentMonth = null; // null = this month, computed from the calendar response
      var calendar = await window.improvApi.get(E.analyticsCalendar);
      currentMonth = calendar.month;
      renderCalendar(calendar);
    } catch (err) {
      var main = document.querySelector('main');
      main.innerHTML = '<section class="card panel"><p class="muted">' +
        (err.message || 'Could not load your streak.') + '</p></section>';
    }
  }

  // --------------------------------------------------------------- calendar

  async function loadCalendar(month) {
    try {
      var calendar = await window.improvApi.get(E.analyticsCalendar + '?month=' + encodeURIComponent(month));
      currentMonth = calendar.month;
      renderCalendar(calendar);
    } catch (err) {
      alert(err.message || 'Could not load that month.');
    }
  }

  function renderCalendar(calendar) {
    document.getElementById('cal-title').textContent = 'Calendar — ' + calendar.month;
    var grid = document.getElementById('cal-grid');
    grid.innerHTML = '';

    WEEKDAYS.forEach(function (day) {
      var head = document.createElement('span');
      head.className = 'cal-head';
      head.textContent = day;
      grid.appendChild(head);
    });

    var byDate = {};
    calendar.days.forEach(function (d) { byDate[d.date] = d; });

    var parts = calendar.month.split('-');
    var year = Number(parts[0]);
    var month = Number(parts[1]);
    var first = new Date(year, month - 1, 1);
    var daysInMonth = new Date(year, month, 0).getDate();
    var today = new Date();
    var todayStr = today.getFullYear() + '-' +
      String(today.getMonth() + 1).padStart(2, '0') + '-' +
      String(today.getDate()).padStart(2, '0');

    // Monday-first leading blanks.
    var offset = (first.getDay() + 6) % 7;
    for (var i = 0; i < offset; i++) {
      grid.appendChild(document.createElement('i'));
    }
    for (var day = 1; day <= daysInMonth; day++) {
      var dateStr = calendar.month + '-' + String(day).padStart(2, '0');
      var cell = document.createElement('i');
      cell.textContent = day;
      var info = byDate[dateStr];
      if (info && info.active) {
        cell.className = 'active';
        cell.title = dateStr + ' — ' + info.xp + ' XP, ' + info.quests + ' quests';
      }
      if (dateStr === todayStr) {
        cell.classList.add('today');
      }
      grid.appendChild(cell);
    }
  }

  document.getElementById('cal-prev').addEventListener('click', function () {
    if (currentMonth) loadCalendar(shiftMonth(currentMonth, -1));
  });
  document.getElementById('cal-next').addEventListener('click', function () {
    if (currentMonth) loadCalendar(shiftMonth(currentMonth, 1));
  });

  /** 'YYYY-MM' shifted by n months. */
  function shiftMonth(month, n) {
    var parts = month.split('-');
    var date = new Date(Number(parts[0]), Number(parts[1]) - 1 + n, 1);
    return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0');
  }

  // --------------------------------------------------------------- timeline

  function renderTimeline(days) {
    var list = document.getElementById('timeline');
    list.innerHTML = '';
    if (!days.length) {
      var empty = document.createElement('li');
      empty.className = 'muted';
      empty.textContent = 'No active days yet — complete a quest to start your streak.';
      list.appendChild(empty);
      return;
    }
    days.forEach(function (day) {
      var li = document.createElement('li');
      var date = document.createElement('span');
      date.textContent = day.date;
      var detail = document.createElement('b');
      detail.textContent = day.xp + ' XP · ' + day.quests + (day.quests === 1 ? ' quest' : ' quests');
      li.appendChild(date);
      li.appendChild(detail);
      list.appendChild(li);
    });
  }

  // ---------------------------------------------------------------- helpers

  function setText(id, value) {
    var el = document.getElementById(id);
    if (el) { el.textContent = value; }
  }

  load();
})();
