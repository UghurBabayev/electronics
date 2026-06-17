const API = {
    async fetch(url, options = {}) {
        const token = localStorage.getItem('token');
        const res = await fetch('/api' + url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': 'Bearer ' + token } : {}),
                ...options.headers
            }
        });
        if (res.status === 401) {
            localStorage.clear();
            window.location.href = '/login.html';
            return;
        }
        if (res.status === 402) {
            showExpiredScreen();
            return;
        }
        if (res.status === 204) return null;
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Xəta baş verdi');
        return data;
    },
    get:    (url)       => API.fetch(url),
    post:   (url, body) => API.fetch(url, { method: 'POST',   body: JSON.stringify(body) }),
    put:    (url, body) => API.fetch(url, { method: 'PUT',    body: JSON.stringify(body) }),
    delete: (url)       => API.fetch(url, { method: 'DELETE' })
};

function fmt(amount) {
    if (amount == null) return '—';
    return Number(amount).toLocaleString('az-AZ', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' ₼';
}

function fmtDate(d) {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('az-AZ');
}

function showToast(msg, type = 'success') {
    const t = document.createElement('div');
    t.className = 'alert alert-' + type;
    t.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:999;min-width:260px;box-shadow:0 4px 12px rgba(0,0,0,.15)';
    t.textContent = msg;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 3000);
}

function openModal(html) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = html;
    overlay.addEventListener('click', e => { if (e.target === overlay) overlay.remove(); });
    document.body.appendChild(overlay);
    return overlay;
}

function closeModal() {
    document.querySelector('.modal-overlay')?.remove();
}

function showExpiredScreen() {
    document.body.innerHTML = `
        <div style="display:flex;align-items:center;justify-content:center;height:100vh;
                    background:#0f172a;font-family:system-ui,sans-serif">
            <div style="text-align:center;color:#f1f5f9;max-width:400px;padding:40px">
                <div style="font-size:48px;margin-bottom:16px">🔒</div>
                <h2 style="font-size:22px;font-weight:700;margin-bottom:12px">
                    Lisenziya müddəti bitib
                </h2>
                <p style="color:#94a3b8;font-size:15px;line-height:1.6">
                    Bu proqramın istifadə müddəti başa çatıb.<br>
                    Davam etmək üçün əlaqə saxlayın.
                </p>
            </div>
        </div>`;
}