async function loadDashboard() {
    document.getElementById('content').innerHTML =
        '<div style="color:var(--text-muted);padding:40px;text-align:center">Yüklənir...</div>';
    try {
        const d = await API.get('/dashboard');
        refreshOverdueBadge(d.overdueCount);
        document.getElementById('content').innerHTML = `
            <div class="page-header">
                <div class="page-title">Ana Səhifə</div>
                <div class="page-subtitle">${fmtDate(new Date().toISOString().slice(0,10))} — Gündəlik icmal</div>
            </div>
            <div class="stats-grid" style="margin-bottom:24px">
                <div class="stat-card">
                    <div class="stat-label">Bugünkü satışlar</div>
                    <div class="stat-value blue">${d.todaySaleCount} satış</div>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:4px">${fmt(d.todaySaleAmount)}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Ümumi nisiyə borcu</div>
                    <div class="stat-value red">${fmt(d.totalDebt)}</div>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:4px">Aktiv planlar üzrə</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Stokda məhsul</div>
                    <div class="stat-value green">${d.inStockCount} ədəd</div>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:4px">Satılmamış vahidlər</div>
                </div>
                <div class="stat-card" style="cursor:pointer" onclick="navigate('installments')"
                     title="Gecikmiş nisiyələrə keç">
                    <div class="stat-label">Gecikmiş nisiyə</div>
                    <div class="stat-value ${d.overdueCount > 0 ? 'red' : 'green'}">${d.overdueCount} plan</div>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:4px">
                        ${d.overdueCount > 0 ? 'Diqqət tələb edir →' : 'Hər şey qaydasındadır'}
                    </div>
                </div>
            </div>`;
    } catch (e) {
        document.getElementById('content').innerHTML =
            `<div class="alert alert-error">${e.message}</div>`;
    }
}

function refreshOverdueBadge(count) {
    const badge = document.getElementById('overdue-badge');
    if (!badge) return;
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'inline-block';
    } else {
        badge.style.display = 'none';
    }
}