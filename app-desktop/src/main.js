import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import { WebLinksAddon } from '@xterm/addon-web-links';
import '@xterm/xterm/css/xterm.css';

const hosts = [
  { id: '1', name: 'acme-api-dev-us-west', host: '127.0.0.1', port: 22, user: 'stan', tags: ['dev', 'aws'] },
  { id: '2', name: 'Servidor Web-01', host: '192.168.16.21', port: 22, user: 'root', tags: ['prod'] },
  { id: '3', name: 'bastion-eu', host: '10.0.0.8', port: 22, user: 'ops', tags: ['bastion'] },
];

const vaultKeys = [
  { name: 'Clave AWS Producción', algo: 'ED25519', enc: 'AES-256' },
  { name: 'Lab personal', algo: 'RSA 4096', enc: 'AES-256' },
  { name: 'CI deploy', algo: 'ED25519', enc: 'Cifrado' },
];

const sftpFiles = [
  { name: '.ssh', size: '—', modified: '2026-08-12' },
  { name: 'bin', size: '—', modified: '2026-07-01' },
  { name: 'public', size: '12 MB', modified: '2026-09-05' },
  { name: 'deploy.sh', size: '4 KB', modified: '2026-09-01' },
];

let term = null;
let fitAddon = null;
let activeSession = null;
const sessions = [];
let showMotd = true;
let tauri = null;

async function loadTauri() {
  try {
    const core = await import('@tauri-apps/api/core');
    const event = await import('@tauri-apps/api/event');
    tauri = { invoke: core.invoke, listen: event.listen };
  } catch {
    tauri = null;
  }
}

function $(sel) { return document.querySelector(sel); }
function $all(sel) { return [...document.querySelectorAll(sel)]; }

function applyTheme(id) {
  document.body.dataset.theme = id;
  localStorage.setItem('ct-theme', id);
  $('#theme-select').value = id;
  if (term) {
    const themes = {
      'dark-neon': { background: '#0A1628', foreground: '#D6E4F0', cursor: '#39FF14', selectionBackground: '#1E3A5F' },
      dracula: { background: '#282A36', foreground: '#F8F8F2', cursor: '#50FA7B', selectionBackground: '#44475A' },
      solarized: { background: '#002B36', foreground: '#FDF6E3', cursor: '#2AA198', selectionBackground: '#073642' },
    };
    term.options.theme = themes[id] || themes['dark-neon'];
  }
}

function renderHosts(filter = '') {
  const q = filter.trim().toLowerCase();
  const list = $('#host-list');
  list.innerHTML = '';
  hosts.filter(h => !q || h.name.toLowerCase().includes(q) || h.host.includes(q)).forEach(h => {
    const li = document.createElement('li');
    li.className = 'host-item';
    li.innerHTML = `<div class="name"><span class="dot"></span>${escapeHtml(h.name)}</div>
      <div class="meta">${escapeHtml(h.user)}@${escapeHtml(h.host)}:${h.port}</div>
      <div class="meta">${h.tags.map(t => `#${t}`).join(' ')}</div>`;
    li.onclick = () => openConnectModal(h);
    list.appendChild(li);
  });
}

function renderVault() {
  const grid = $('#vault-grid');
  grid.innerHTML = vaultKeys.map(k => `
    <article class="card">
      <h3>🛡️ ${escapeHtml(k.name)}</h3>
      <div class="tags">
        <span class="tag">${escapeHtml(k.algo)}</span>
        <span class="tag">${escapeHtml(k.enc)}</span>
      </div>
    </article>`).join('');
}

function renderSftp() {
  $('#sftp-rows').innerHTML = sftpFiles.map(f => `
    <tr><td>📄 ${escapeHtml(f.name)}</td><td>${escapeHtml(f.size)}</td><td>${escapeHtml(f.modified)}</td></tr>
  `).join('');
  $('#transfer-list').innerHTML = `
    <li class="card"><div>deploy.sh ↑</div><div class="meta">2.59 MB/s</div><div class="progress"><span style="width:75%"></span></div></li>
    <li class="card" style="margin-top:8px"><div>backup.tgz ↓</div><div class="meta">1.10 MB/s</div><div class="progress"><span style="width:40%"></span></div></li>`;
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

function setView(name) {
  $all('.main-tab').forEach(b => b.classList.toggle('active', b.dataset.view === name));
  $all('.view').forEach(v => v.classList.toggle('active', v.id === `view-${name}`));
}

function ensureTerminal() {
  if (term) return term;
  term = new Terminal({
    cursorBlink: true,
    fontFamily: '"Cascadia Code", "JetBrains Mono", Consolas, monospace',
    fontSize: 13,
    theme: {
      background: '#0A1628',
      foreground: '#D6E4F0',
      cursor: '#39FF14',
      selectionBackground: '#1E3A5F',
      black: '#0A1628',
      red: '#FF4D6D',
      green: '#39FF14',
      yellow: '#F5D90A',
      blue: '#00E5FF',
      magenta: '#FF6B9D',
      cyan: '#00E5FF',
      white: '#D6E4F0',
    },
  });
  fitAddon = new FitAddon();
  term.loadAddon(fitAddon);
  term.loadAddon(new WebLinksAddon());
  term.open($('#terminal'));
  fitAddon.fit();
  window.addEventListener('resize', () => fitAddon && fitAddon.fit());
  term.onData(async (data) => {
    if (!activeSession || !tauri) return;
    try {
      await tauri.invoke('pty_write', { id: activeSession.id, data });
    } catch (e) {
      term.writeln(`\r\n\x1b[31mError escritura PTY: ${e}\x1b[0m`);
    }
  });
  return term;
}

function motdHtml(session) {
  return `<span class="k">Logged as:</span>  <span class="v">${escapeHtml(session.user)}@${escapeHtml(session.name)}</span>
<span class="k">OS:</span>          <span class="v">Ubuntu 22.04.4 LTS (demo MOTD)</span>
<span class="k">IP addresses:</span> <span class="ip">10.0.1.24</span>
<span class="k">Public IP:</span>    <span class="ip">203.0.113.10</span>
<span class="k">Uptime:</span>      <span class="v">14 days</span>
<span class="k">Memory:</span>      <span class="v">[████████░░] 78%</span>
<span class="k">Disk space:</span>  <span class="v">[██████░░░░] 61%</span>
<span class="k">Services:</span>    <span class="v">▲ UFW  ▲ Nginx  ▲ SSH</span>
<span class="k">Tmux sessions:</span> <span class="v">0</span>`;
}

function renderSessionTabs() {
  const el = $('#session-tabs');
  el.innerHTML = sessions.map(s => `
    <div class="session-tab ${activeSession && activeSession.id === s.id ? 'active' : ''}" data-id="${s.id}">
      <span>${escapeHtml(s.name)}</span>
      <button class="x" data-close="${s.id}">×</button>
    </div>`).join('');
  el.querySelectorAll('.session-tab').forEach(tab => {
    tab.onclick = (ev) => {
      if (ev.target.dataset.close) {
        closeSession(ev.target.dataset.close);
        return;
      }
      focusSession(tab.dataset.id);
    };
  });
}

async function closeSession(id) {
  const idx = sessions.findIndex(s => s.id === id);
  if (idx < 0) return;
  if (tauri) {
    try { await tauri.invoke('pty_close', { id }); } catch {}
  }
  sessions.splice(idx, 1);
  if (activeSession && activeSession.id === id) {
    activeSession = sessions[0] || null;
  }
  renderSessionTabs();
  if (!activeSession) {
    $('#term-wrap').classList.add('hidden');
    $('#term-empty').classList.remove('hidden');
  }
}

function focusSession(id) {
  activeSession = sessions.find(s => s.id === id) || null;
  renderSessionTabs();
}

function openConnectModal(host) {
  $('#f-name').value = host?.name || 'nueva-sesion';
  $('#f-host').value = host?.host || '127.0.0.1';
  $('#f-port').value = host?.port || 22;
  $('#f-user').value = host?.user || 'stan';
  $('#modal-host').classList.remove('hidden');
}

function hideModal() { $('#modal-host').classList.add('hidden'); }

async function startSession(opts) {
  ensureTerminal();
  $('#term-empty').classList.add('hidden');
  $('#term-wrap').classList.remove('hidden');
  showMotd = !!opts.motd;
  const motd = $('#motd');
  if (showMotd) {
    motd.classList.remove('hidden');
    motd.innerHTML = motdHtml(opts);
  } else {
    motd.classList.add('hidden');
  }

  const id = `s-${Date.now()}`;
  const session = { id, name: opts.name, host: opts.host, port: opts.port, user: opts.user };
  sessions.push(session);
  activeSession = session;
  renderSessionTabs();
  term.reset();
  fitAddon.fit();

  if (!tauri) {
    term.writeln('\x1b[36mCloudTerm Pro\x1b[0m — modo UI (sin backend Tauri).');
    term.writeln(`Sesión \x1b[32m${opts.user}@${opts.host}:${opts.port}\x1b[0m`);
    term.writeln('Abre con `tauri dev` / EXE para PTY SSH real.\r\n');
    term.write(`\x1b[32m${opts.user}@${opts.name}\x1b[0m:\x1b[34m~\x1b[0m# `);
    let buf = '';
    const disp = term.onData((d) => {
      if (d === '\r') {
        term.write('\r\n');
        if (buf.trim() === 'exit') { term.writeln('logout'); buf=''; return; }
        if (buf.trim()) term.writeln(`\x1b[33m[demo]\x1b[0m comando: ${buf}`);
        buf = '';
        term.write(`\x1b[32m${opts.user}@${opts.name}\x1b[0m:\x1b[34m~\x1b[0m# `);
      } else if (d === '\u007f') {
        if (buf.length) { buf = buf.slice(0, -1); term.write('\b \b'); }
      } else {
        buf += d; term.write(d);
      }
    });
    session._demo = disp;
    return;
  }

  try {
    const cols = term.cols;
    const rows = term.rows;
    await tauri.invoke('pty_open', {
      id,
      host: opts.host,
      port: Number(opts.port),
      user: opts.user,
      cols,
      rows,
    });
    term.writeln(`\x1b[36mConectando\x1b[0m ${opts.user}@${opts.host}:${opts.port} …`);
  } catch (e) {
    term.writeln(`\x1b[31mNo se pudo abrir PTY/SSH: ${e}\x1b[0m`);
    term.writeln('Comprueba que `ssh` esté en PATH (OpenSSH).');
  }
}

async function bindPtyEvents() {
  if (!tauri) return;
  await tauri.listen('pty-output', (ev) => {
    const { id, data } = ev.payload;
    if (!activeSession || activeSession.id !== id || !term) return;
    term.write(data);
  });
  await tauri.listen('pty-exit', (ev) => {
    const { id, code } = ev.payload;
    if (term && activeSession && activeSession.id === id) {
      term.writeln(`\r\n\x1b[33m[sesión terminada code=${code}]\x1b[0m`);
    }
  });
}

function wireUi() {
  $all('.main-tab').forEach(btn => btn.addEventListener('click', () => setView(btn.dataset.view)));
  $('#host-search').addEventListener('input', (e) => renderHosts(e.target.value));
  $('#btn-add-host').onclick = () => openConnectModal(null);
  $('#btn-new-session').onclick = () => openConnectModal(hosts[0]);
  $('#btn-connect-demo').onclick = () => startSession({
    name: 'acme-api-dev-us-west', host: '127.0.0.1', port: 22, user: 'stan', motd: true,
  });
  $('#modal-cancel').onclick = hideModal;
  $('#modal-connect').onclick = () => {
    const opts = {
      name: $('#f-name').value.trim() || 'sesion',
      host: $('#f-host').value.trim() || '127.0.0.1',
      port: Number($('#f-port').value || 22),
      user: $('#f-user').value.trim() || 'stan',
      motd: $('#f-motd').checked,
    };
    hideModal();
    startSession(opts);
    setView('workspace');
  };
  $('#theme-select').onchange = (e) => applyTheme(e.target.value);
  $('#btn-theme').onclick = () => {
    const order = ['dark-neon', 'dracula', 'solarized'];
    const cur = document.body.dataset.theme;
    applyTheme(order[(order.indexOf(cur) + 1) % order.length]);
  };
}

async function main() {
  await loadTauri();
  applyTheme(localStorage.getItem('ct-theme') || 'dark-neon');
  renderHosts();
  renderVault();
  renderSftp();
  wireUi();
  await bindPtyEvents();
}

main();
