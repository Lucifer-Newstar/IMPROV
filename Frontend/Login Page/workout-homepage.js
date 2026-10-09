/**
 * IMPROV — home screen: the hunter card (level, rank, XP, streak) and today's
 * quest board with tap-to-log (5 / 10 / 20), exact entry and one-shot
 * completion. Logging a quest refreshes the card from the server's progression.
 */
(function () {
  'use strict';

  var E = window.IMPROV.endpoints;
  var state = { profile: null, progression: null, board: null };

  // ------------------------------------------------------------------ load

  async function load() {
    try {
      var loaded = await Promise.all([
        window.improvApi.get(E.me),
        window.improvApi.get(E.progression),
        window.improvApi.get(E.questsToday)
      ]);
      state.profile = loaded[0];
      state.progression = loaded[1];
      state.board = loaded[2];
      renderHunter();
      renderBoard();
      startCountdown();
    } catch (err) {
      var list = document.getElementById('quest-list');
      list.innerHTML = '';
      var p = document.createElement('p');
      p.className = 'muted';
      p.textContent = err.message || 'Could not load your quests.';
      list.appendChild(p);
    }
  }

  // -------------------------------------------------------------- hunter card

  function renderHunter() {
    var p = state.progression;
    setText('hunter-name', state.profile.firstname || state.profile.username);
    setText('hunter-level', 'Level ' + p.level + ' · Rank ' + p.rank);

    var badge = document.getElementById('rank-badge');
    badge.textContent = p.rank;
    badge.dataset.rank = p.rank;
    badge.style.color = p.rankColor;

    setText('stat-streak', p.currentStreak + (p.currentStreak === 1 ? ' day' : ' days'));
    setText('stat-total-xp', p.totalXp + ' XP');
    setText('stat-today-xp', p.xpToday + ' / ' + p.dailySoftCap);

    var pct = p.xpForNextLevel > 0
      ? Math.min(100, Math.round(100 * p.xpIntoLevel / p.xpForNextLevel))
      : 100;
    document.getElementById('xp-bar').style.width = pct + '%';
    setText('xp-label', p.xpIntoLevel + ' / ' + p.xpForNextLevel + ' XP to level ' + (p.level + 1));
  }

  // ------------------------------------------------------------- quest board

  function renderBoard() {
    var list = document.getElementById('quest-list');
    list.innerHTML = '';
    if (!state.board.quests.length) {
      var empty = document.createElement('p');
      empty.className = 'muted';
      empty.textContent = 'No quests today — check back after the daily reset.';
      list.appendChild(empty);
      return;
    }
    state.board.quests.forEach(function (quest) {
      list.appendChild(questCard(quest));
    });
  }

  function questCard(quest) {
    var card = document.createElement('article');
    card.className = 'card quest-card' + (quest.completed ? ' completed' : '');
    card.dataset.questId = quest.id;

    var head = document.createElement('div');
    head.className = 'quest-head';
    var headText = document.createElement('div');
    var name = document.createElement('h3');
    name.className = 'quest-name';
    name.textContent = quest.name;
    var desc = document.createElement('p');
    desc.className = 'quest-desc';
    desc.textContent = quest.description || '';
    headText.appendChild(name);
    headText.appendChild(desc);
    head.appendChild(headText);
    card.appendChild(head);

    var meta = document.createElement('div');
    meta.className = 'quest-meta';
    if (quest.statCode) {
      meta.appendChild(chip(quest.statCode + ' +' + quest.statAmount, 'chip stat'));
    }
    meta.appendChild(chip('+' + quest.xpReward + ' XP', 'chip xp'));
    if (quest.completed) {
      meta.appendChild(chip('+' + (quest.awardedXp || 0) + ' XP earned', 'chip done'));
    }
    card.appendChild(meta);

    var progress = document.createElement('div');
    progress.className = 'quest-progress';
    var pct = quest.target > 0 ? Math.min(100, Math.round(100 * quest.logged / quest.target)) : 100;
    var bar = document.createElement('div');
    bar.className = 'progress';
    var fill = document.createElement('span');
    fill.style.width = pct + '%';
    bar.appendChild(fill);
    var count = document.createElement('p');
    count.className = 'quest-count mono';
    count.textContent = quest.logged + ' / ' + quest.target + (quest.unit ? ' ' + quest.unit : '');
    progress.appendChild(bar);
    progress.appendChild(count);
    card.appendChild(progress);

    if (!quest.completed) {
      card.appendChild(questActions(quest));
    }
    return card;
  }

  function questActions(quest) {
    var actions = document.createElement('div');
    actions.className = 'quest-actions';

    if (quest.type === 'WORKOUT' || quest.type === 'CUSTOM') {
      var complete = document.createElement('button');
      complete.type = 'button';
      complete.className = 'btn btn-primary btn-sm';
      complete.textContent = 'Complete';
      complete.addEventListener('click', function () {
        runQuestAction(quest.id, complete, function () {
          return window.improvApi.post(window.IMPROV.url(E.questComplete, { id: quest.id }));
        });
      });
      actions.appendChild(complete);
      return actions;
    }

    [5, 10, 20].forEach(function (step) {
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'btn btn-ghost btn-sm';
      btn.textContent = '+' + step;
      btn.addEventListener('click', function () {
        runQuestAction(quest.id, btn, function () {
          return window.improvApi.post(
            window.IMPROV.url(E.questLog, { id: quest.id }), { amount: step });
        });
      });
      actions.appendChild(btn);
    });

    var exact = document.createElement('div');
    exact.className = 'quest-exact';
    var input = document.createElement('input');
    input.type = 'number';
    input.min = '1';
    input.placeholder = 'exact';
    input.setAttribute('aria-label', 'Exact amount');
    var go = document.createElement('button');
    go.type = 'button';
    go.className = 'btn btn-ghost btn-sm';
    go.textContent = 'Log';
    go.addEventListener('click', function () {
      var amount = parseInt(input.value, 10);
      if (Number.isFinite(amount) && amount > 0) {
        runQuestAction(quest.id, go, function () {
          return window.improvApi.post(
            window.IMPROV.url(E.questLog, { id: quest.id }), { amount: amount });
        });
      }
    });
    exact.appendChild(input);
    exact.appendChild(go);
    actions.appendChild(exact);
    return actions;
  }

  async function runQuestAction(questId, button, call) {
    setBusy(button, true);
    try {
      var updated = await call();
      replaceQuest(updated);
      state.progression = await window.improvApi.get(E.progression);
      renderHunter();
    } catch (err) {
      alert(err.message || 'Could not log that. Please try again.');
    } finally {
      setBusy(button, false);
    }
  }

  function replaceQuest(updated) {
    var list = document.getElementById('quest-list');
    var old = list.querySelector('[data-quest-id="' + updated.id + '"]');
    var card = questCard(updated);
    if (old) {
      old.replaceWith(card);
    } else {
      list.appendChild(card);
    }
    state.board.quests = state.board.quests.map(function (q) {
      return q.id === updated.id ? updated : q;
    });
  }

  // --------------------------------------------------------------- countdown

  function startCountdown() {
    var el = document.getElementById('reset-countdown');
    var resetAt = new Date(state.board.resetAt).getTime();
    function pad(n) { return (n < 10 ? '0' : '') + n; }
    function tick() {
      var ms = resetAt - Date.now();
      if (ms <= 0) {
        el.textContent = 'Daily reset — reloading';
        setTimeout(function () { window.location.reload(); }, 1500);
        return;
      }
      var h = Math.floor(ms / 3600000);
      var m = Math.floor((ms % 3600000) / 60000);
      var s = Math.floor((ms % 60000) / 1000);
      el.textContent = 'Resets in ' + pad(h) + ':' + pad(m) + ':' + pad(s);
    }
    tick();
    setInterval(tick, 1000);
  }

  // ----------------------------------------------------------------- helpers

  function chip(text, cls) {
    var el = document.createElement('span');
    el.className = cls;
    el.textContent = text;
    return el;
  }

  function setText(id, value) {
    var el = document.getElementById(id);
    if (el) { el.textContent = value; }
  }

  function setBusy(button, busy) {
    var card = button.closest('.quest-card');
    if (card) {
      card.querySelectorAll('button, input').forEach(function (el) {
        el.disabled = busy;
      });
    }
  }

  load();
})();
