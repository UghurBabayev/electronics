async function loadReports() {
    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Hesabatlar</div>
            <div class="page-subtitle">Maliyyə xülasəsi</div>
        </div>
        <div class="card" style="margin-bottom:20px">
            <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
                <div class="date-range">
                    <input type="date" id="rep-from" value="${firstOfMonth()}">
                    <span>—</span>
                    <input type="date" id="rep-to" value="${today()}">
                </div>
                <button class="btn btn-primary" onclick="fetchReport()">Hesabla</button>
                <button class="btn btn-ghost" onclick="exportReportExcel()">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:4px">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    Excel ixrac
                </button>
            </div>
        </div>
        <div id="report-result"></div>`;

    fetchReport();
}

async function fetchReport() {
    const from = document.getElementById('rep-from').value;
    const to   = document.getElementById('rep-to').value;
    const el   = document.getElementById('report-result');
    el.innerHTML = '<div style="color:var(--text-muted);padding:20px">Yüklənir...</div>';
    try {
        const r = await API.get(`/reports?from=${from}&to=${to}`);
        el.innerHTML = `
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Ümumi gəlir</div>
                    <div class="stat-value blue">${fmt(r.totalRevenue)}</div>
                    <div style="font-size:12px;color:var(--text-muted);margin-top:4px">${fmtDate(r.from)} — ${fmtDate(r.to)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Ümumi xərc (alış)</div>
                    <div class="stat-value">${fmt(r.totalCost)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Mənfəət</div>
                    <div class="stat-value ${r.profit >= 0 ? 'green' : 'red'}">${fmt(r.profit)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Ümumi borc (nisiyə)</div>
                    <div class="stat-value red">${fmt(r.totalDebt)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Anbar dəyəri</div>
                    <div class="stat-value orange">${fmt(r.inventoryValue)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Başlanğıc məbləğ</div>
                    <div class="stat-value">${fmt(r.initialBalance)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Kassada nağd</div>
                    <div class="stat-value green">${fmt(r.cashOnHand)}</div>
                </div>
            </div>`;
    } catch (e) {
        el.innerHTML = `<div class="alert alert-error">${e.message}</div>`;
    }
}

async function exportReportExcel() {
    const from = document.getElementById('rep-from')?.value || firstOfMonth();
    const to   = document.getElementById('rep-to')?.value  || today();
    try {
        const res = await fetch(`/api/reports/export/excel?from=${from}&to=${to}`, {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) { showToast('Export xətası', 'error'); return; }
        const blob = await res.blob();
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement('a');
        a.href     = url;
        a.download = `hesabat-${from}-${to}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
    } catch (e) {
        showToast(e.message, 'error');
    }
}