/**
 * IMPROV — rank screen: the E -> S ladder as hexagons, with the user's current
 * rank highlighted and locked tiers dimmed.
 */
(function () {
  'use strict';

  var E = window.IMPROV.endpoints;

  async function load() {
    try {
      var loaded = await Promise.all([
        window.improvApi.get(E.ranks),
        window.improvApi.get(E.progression)
      ]);
      render(loaded[0], loaded[1]);
    } catch (err) {
      var main = document.querySelector('main');
      main.innerHTML = '<section class="card panel"><p class="muted">' +
        (err.message || 'Could not load the rank ladder.') + '</p></section>';
    }
  }

  function render(ranks, progression) {
    var badge = document.getElementById('current-rank');
    badge.textContent = progression.rank;
    badge.dataset.rank = progression.rank;
    badge.style.color = progression.rankColor;

    setText('rank-summary', 'Level ' + progression.level + ' · Rank ' + progression.rank +
      ' · ' + progression.xpIntoLevel + ' / ' + progression.xpForNextLevel +
      ' XP to level ' + (progression.level + 1));

    var ladder = document.getElementById('rank-ladder');
    ladder.innerHTML = '';
    ranks.forEach(function (rank) {
      ladder.appendChild(rankRow(rank, progression));
    });
  }

  function rankRow(rank, progression) {
    var unlocked = progression.level >= rank.minLevel;
    var isCurrent = progression.rank === rank.rank;

    var row = document.createElement('div');
    row.className = 'card rank-row';
    row.dataset.rank = rank.rank;
    row.classList.add(unlocked ? 'unlocked' : 'locked');
    if (isCurrent) {
      row.classList.add('current');
    }

    var hex = document.createElement('div');
    hex.className = 'rank-hex';
    hex.textContent = rank.rank;

    var info = document.createElement('div');
    info.className = 'rank-info';
    var title = document.createElement('h3');
    title.textContent = 'Rank ' + rank.rank;
    var sub = document.createElement('p');
    sub.textContent = 'Level ' + rank.minLevel +
      (rank.maxLevel ? '–' + rank.maxLevel : '+') +
      ' · quest XP ×' + rank.xpMultiplier;
    info.appendChild(title);
    info.appendChild(sub);

    var status = document.createElement('span');
    if (isCurrent) {
      status.className = 'chip done';
      status.textContent = 'Current';
    } else if (unlocked) {
      status.className = 'chip xp';
      status.textContent = 'Unlocked';
    } else {
      status.className = 'chip';
      status.textContent = 'Locked';
    }

    row.appendChild(hex);
    row.appendChild(info);
    row.appendChild(status);
    return row;
  }

  function setText(id, value) {
    var el = document.getElementById(id);
    if (el) { el.textContent = value; }
  }

  load();
})();
