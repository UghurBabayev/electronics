async function loadCashLog() {
    const data = await API.get('/cash-entries');

    const total = data.total ?? 0;
    const isPos = Number(total) >= 0;

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Kassa</div>
            <div class="page-subtitle">Nağd pul qeydləri</div>
        </div>

        <div class="stats-grid" style="grid-template-columns:1fr 1fr;margin-bottom:20px">
            <div class="card">
                <div style="font-size:12px;color:var(--text-muted);margin-bottom:6px">Ümumi kassa balansı</div>
                <div style="font-size:28px;font-weight:700;color:${isPos ? 'var(--success)' : 'var(--danger)'}">
                    ${fmt(total)}
                </div>
            </div>
            <div class="card" style="display:flex;align-items:center;justify-content:center">
                <button class="btn btn-primary" style="width:100%;justify-content:center" onclick="showCashForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="margin-right:6px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Yeni qeyd əlavə et
                </button>
            </div>
        </div>

        <div class="card">
            <div class="table-wrap">
                <table>
                    <thead><tr>
                        <th>Tarix</th><th>Məbləğ</th><th>Qeyd</th><th></th>
                    </tr></thead>
                    <tbody id="cash-body"></tbody>
                </table>
            </div>
        </div>`;

    renderCashRows(data.entries || []);
}

function renderCashRows(list) {
    const tbody = document.getElementById('cash-body');
    if (!list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="4">Qeyd tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(e => {
        const pos = Number(e.amount) >= 0;
        return `<tr>
            <td>${fmtDate(e.entryDate)}</td>
            <td style="font-weight:700;color:${pos ? 'var(--success)' : 'var(--danger)'}">
                ${pos ? '+' : ''}${fmt(e.amount)}
            </td>
            <td>${e.note || '—'}</td>
            <td><button class="btn btn-danger btn-sm" onclick="deleteCashEntry(${e.id})">Sil</button></td>
        </tr>`;
    }).join('');
}

function showCashForm() {
    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">Kassa qeydi</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="cash-err" class="alert alert-error" style="display:none"></div>
                <div style="display:flex;gap:8px;margin-bottom:16px">
                    <button class="btn btn-success btn-sm" id="cash-type-pos" onclick="setCashType(1)"
                            style="flex:1;justify-content:center;background:var(--success);color:#fff">
                        + Gəlir
                    </button>
                    <button class="btn btn-ghost btn-sm" id="cash-type-neg" onclick="setCashType(-1)"
                            style="flex:1;justify-content:center">
                        − Xərc
                    </button>
                </div>
                <input type="hidden" id="cash-sign" value="1">
                <div class="form-row">
                    <div class="form-group"><label>Məbləğ (₼) *</label>
                        <input id="cash-amount" type="number" step="0.01" min="0" placeholder="0.00"></div>
                    <div class="form-group"><label>Tarix *</label>
                        <input id="cash-date" type="date" value="${today()}"></div>
                </div>
                <div class="form-group"><label>Qeyd</label>
                    <input id="cash-note" placeholder="Məs: Gündəlik satış kassası, xərc..."></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveCashEntry()">Qeyd et</button>
            </div>
        </div>`);
}

function setCashType(sign) {
    document.getElementById('cash-sign').value = sign;
    const posBtn = document.getElementById('cash-type-pos');
    const negBtn = document.getElementById('cash-type-neg');
    if (sign > 0) {
        posBtn.style.cssText = 'flex:1;justify-content:center;background:var(--success);color:#fff';
        negBtn.style.cssText = 'flex:1;justify-content:center';
        negBtn.className = 'btn btn-ghost btn-sm';
        posBtn.className = 'btn btn-sm';
    } else {
        negBtn.style.cssText = 'flex:1;justify-content:center;background:var(--danger);color:#fff';
        posBtn.style.cssText = 'flex:1;justify-content:center';
        posBtn.className = 'btn btn-ghost btn-sm';
        negBtn.className = 'btn btn-sm';
    }
}

async function saveCashEntry() {
    const sign   = parseInt(document.getElementById('cash-sign').value);
    const rawAmt = parseFloat(document.getElementById('cash-amount').value);
    const date   = document.getElementById('cash-date').value;
    const note   = document.getElementById('cash-note').value.trim() || null;
    const errEl  = document.getElementById('cash-err');

    if (!date || isNaN(rawAmt) || rawAmt <= 0) {
        errEl.textContent = 'Tarix və məbləğ daxil edin';
        errEl.style.display = 'block';
        return;
    }

    try {
        await API.post('/cash-entries', { entryDate: date, amount: sign * rawAmt, note });
        closeModal();
        showToast('Qeyd əlavə edildi');
        loadCashLog();
    } catch (e) {
        errEl.textContent = e.message;
        errEl.style.display = 'block';
    }
}

async function deleteCashEntry(id) {
    if (!confirm('Bu qeydi silmək istədiyinizə əminsiniz?')) return;
    try {
        await API.delete('/cash-entries/' + id);
        showToast('Qeyd silindi');
        loadCashLog();
    } catch (e) { showToast(e.message, 'error'); }
}