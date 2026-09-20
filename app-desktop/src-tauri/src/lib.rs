use parking_lot::Mutex;
use portable_pty::{native_pty_system, CommandBuilder, PtySize};
use serde::Serialize;
use std::collections::HashMap;
use std::io::{Read, Write};
use std::thread;
use tauri::{AppHandle, Emitter, State};

struct Session {
    writer: Mutex<Box<dyn Write + Send>>,
    #[allow(dead_code)]
    child_killer: Mutex<Box<dyn portable_pty::ChildKiller + Send + Sync>>,
}

struct AppState {
    sessions: Mutex<HashMap<String, Session>>,
}

#[derive(Clone, Serialize)]
struct PtyOutput {
    id: String,
    data: String,
}

#[derive(Clone, Serialize)]
struct PtyExit {
    id: String,
    code: i32,
}

fn resolve_ssh() -> Result<String, String> {
    which::which("ssh")
        .or_else(|_| which::which("ssh.exe"))
        .map(|p| p.to_string_lossy().to_string())
        .map_err(|_| {
            "No se encontro cliente SSH en PATH. Instala OpenSSH Client.".to_string()
        })
}

#[tauri::command]
fn pty_open(
    app: AppHandle,
    state: State<'_, AppState>,
    id: String,
    host: String,
    port: u16,
    user: String,
    cols: u16,
    rows: u16,
) -> Result<(), String> {
    let ssh = resolve_ssh()?;
    let pty_system = native_pty_system();
    let pair = pty_system
        .openpty(PtySize {
            rows,
            cols,
            pixel_width: 0,
            pixel_height: 0,
        })
        .map_err(|e| format!("openpty: {e}"))?;

    let mut cmd = CommandBuilder::new(&ssh);
    cmd.arg("-tt");
    cmd.arg("-p");
    cmd.arg(port.to_string());
    cmd.arg(format!("{user}@{host}"));
    cmd.env("TERM", "xterm-256color");

    let child = pair
        .slave
        .spawn_command(cmd)
        .map_err(|e| format!("spawn: {e}"))?;

    let killer = child.clone_killer();
    let writer = pair
        .master
        .take_writer()
        .map_err(|e| format!("writer: {e}"))?;
    let mut reader = pair
        .master
        .try_clone_reader()
        .map_err(|e| format!("reader: {e}"))?;

    {
        let mut map = state.sessions.lock();
        map.insert(
            id.clone(),
            Session {
                writer: Mutex::new(writer),
                child_killer: Mutex::new(killer),
            },
        );
    }

    let app_out = app.clone();
    let id_out = id.clone();
    thread::spawn(move || {
        let mut buf = [0u8; 8192];
        loop {
            match reader.read(&mut buf) {
                Ok(0) => break,
                Ok(n) => {
                    let data = String::from_utf8_lossy(&buf[..n]).to_string();
                    let _ = app_out.emit(
                        "pty-output",
                        PtyOutput {
                            id: id_out.clone(),
                            data,
                        },
                    );
                }
                Err(_) => break,
            }
        }
        let _ = app_out.emit(
            "pty-exit",
            PtyExit {
                id: id_out,
                code: 0,
            },
        );
    });

    thread::spawn(move || {
        let mut child = child;
        let _ = child.wait();
    });

    Ok(())
}

#[tauri::command]
fn pty_write(state: State<'_, AppState>, id: String, data: String) -> Result<(), String> {
    let map = state.sessions.lock();
    let Some(session) = map.get(&id) else {
        return Err("sesion no encontrada".into());
    };
    let mut w = session.writer.lock();
    w.write_all(data.as_bytes())
        .map_err(|e| format!("write: {e}"))?;
    w.flush().map_err(|e| format!("flush: {e}"))?;
    Ok(())
}

#[tauri::command]
fn pty_resize(state: State<'_, AppState>, id: String, cols: u16, rows: u16) -> Result<(), String> {
    let _ = (state, id, cols, rows);
    Ok(())
}

#[tauri::command]
fn pty_close(state: State<'_, AppState>, id: String) -> Result<(), String> {
    let mut map = state.sessions.lock();
    if let Some(session) = map.remove(&id) {
        let mut killer = session.child_killer.lock();
        let _ = killer.kill();
    }
    Ok(())
}

#[tauri::command]
fn list_demo_hosts() -> Vec<serde_json::Value> {
    vec![
        serde_json::json!({"name":"acme-api-dev-us-west","host":"127.0.0.1","port":22,"user":"stan"}),
        serde_json::json!({"name":"Servidor Web-01","host":"192.168.16.21","port":22,"user":"root"}),
    ]
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .manage(AppState {
            sessions: Mutex::new(HashMap::new()),
        })
        .invoke_handler(tauri::generate_handler![
            pty_open,
            pty_write,
            pty_resize,
            pty_close,
            list_demo_hosts
        ])
        .run(tauri::generate_context!())
        .expect("error al iniciar CloudTerm Pro");
}
