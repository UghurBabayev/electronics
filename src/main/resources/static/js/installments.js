async function loadInstallments() {
    const plans = await API.get('/installments');

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Nisiyə</div>
            <div class="page-subtitle">Aktiv nisiyə planları və ödəniş cədvəli</div>
        </div>
        <div class="toolbar" style="margin-bottom:16px">
            <div class="toolbar-left" style="flex-wrap:wrap;gap:8px">
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('ALL')" id="f-all">Hamısı</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('ACTIVE')" id="f-active">Aktiv</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('OVERDUE')" id="f-overdue">Gecikmiş</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('COMPLETED')" id="f-completed">Tamamlanmış</button>
                <button class="btn btn-ghost btn-sm" onclick="exportInstallmentsExcel()" style="margin-left:auto">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="vertical-align:middle;margin-right:4px">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    Excel ixrac
                </button>
            </div>
        </div>
        <div id="installment-list"></div>`;

    window._plans = plans;
    renderInstallments(plans);
}

function filterInstallments(status) {
    const filtered = status === 'ALL'
        ? window._plans
        : window._plans.filter(p => p.status === status);
    renderInstallments(filtered);
}

function renderInstallments(list) {
    const el = document.getElementById('installment-list');
    if (!list.length) {
        el.innerHTML = '<div class="card" style="text-align:center;color:var(--text-muted);padding:40px">Nisiyə tapılmadı</div>';
        return;
    }
    el.innerHTML = list.map(plan => `
        <div class="card" style="margin-bottom:16px;${plan.status === 'OVERDUE' ? 'border-left:4px solid var(--danger)' : ''}">
            <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:12px">
                <div>
                    <div style="font-weight:700;font-size:16px">${plan.productName}</div>
                    <div style="color:var(--text-muted);font-size:13px;margin-top:2px">${plan.customerName || 'Müştəri göstərilməyib'}</div>
                </div>
                <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
                    <span class="badge ${statusBadge(plan.status)}">${statusLabel(plan.status)}</span>
                    <span style="font-size:13px;color:var(--text-muted)">Başlanğıc: ${fmtDate(plan.startDate)}</span>
                </div>
            </div>
            <div class="stats-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:16px">
                <div><div style="font-size:11px;color:var(--text-muted);margin-bottom:4px">Ümumi</div>
                     <div style="font-weight:700">${fmt(plan.totalAmount)}</div></div>
                <div><div style="font-size:11px;color:var(--text-muted);margin-bottom:4px">Aylıq</div>
                     <div style="font-weight:700">${fmt(plan.monthlyPayment)}</div></div>
                <div><div style="font-size:11px;color:var(--text-muted);margin-bottom:4px">Ödənilib</div>
                     <div style="font-weight:700;color:var(--success)">${fmt(plan.paidAmount)}</div></div>
                <div><div style="font-size:11px;color:var(--text-muted);margin-bottom:4px">Qalıq</div>
                     <div style="font-weight:700;color:var(--danger)">${fmt(plan.remainingAmount)}</div></div>
            </div>
            <div style="border-top:1px solid var(--border);padding-top:12px">
                <div style="font-size:12px;font-weight:600;color:var(--text-muted);margin-bottom:8px;text-transform:uppercase;letter-spacing:.5px">Ödəniş cədvəli</div>
                ${plan.payments.map(pay => {
                    const remaining = (pay.amount - (pay.paidAmount || 0)).toFixed(2);
                    const isPartial = !pay.isPaid && pay.paidAmount > 0;
                    return `
                    <div class="payment-row">
                        <div>
                            <span class="${isOverdue(pay) ? 'payment-overdue' : ''}">${fmtDate(pay.dueDate)}</span>
                            ${isOverdue(pay) ? '<span class="badge badge-red" style="margin-left:6px;font-size:10px">Gecikmiş</span>' : ''}
                        </div>
                        <div style="display:flex;align-items:center;gap:10px;flex-wrap:wrap">
                            <span style="font-weight:600">${fmt(pay.amount)}</span>
                            ${pay.isPaid
                                ? `<span class="badge badge-green">Ödənilib ${fmtDate(pay.paidDate)}</span>`
                                : isPartial
                                    ? `<span class="badge badge-orange" style="background:#f59e0b;color:#fff">Qismən: ${fmt(pay.paidAmount)}</span>
                                       <button class="btn btn-success btn-sm" onclick="showPayDialog(${pay.id}, ${remaining})">Qalanı ödə (${fmt(remaining)})</button>`
                                    : `<button class="btn btn-success btn-sm" onclick="showPayDialog(${pay.id}, ${pay.amount})">Ödənildi</button>`}
                        </div>
                    </div>`;
                }).join('')}
            </div>
        </div>`).join('');
}

function showPayDialog(paymentId, maxAmount) {
    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">Ödəniş qeyd et</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="pay-dialog-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group">
                    <label>Ödənilən məbləğ (₼) — maksimum ${fmt(maxAmount)}</label>
                    <input id="pay-amount" type="number" step="0.01" min="0.01"
                           max="${maxAmount}" value="${maxAmount}">
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-success" onclick="submitPayment(${paymentId})">Qeyd et</button>
            </div>
        </div>`);
    setTimeout(() => document.getElementById('pay-amount')?.select(), 50);
}

async function submitPayment(paymentId) {
    const amount = parseFloat(document.getElementById('pay-amount').value);
    if (!amount || amount <= 0) {
        document.getElementById('pay-dialog-err').textContent = 'Düzgün məbləğ daxil edin';
        document.getElementById('pay-dialog-err').style.display = 'block';
        return;
    }
    try {
        await API.post(`/installments/payments/${paymentId}/pay`, { amount });
        closeModal();
        showToast('Ödəniş qeyd edildi');
        loadInstallments();
    } catch (e) {
        document.getElementById('pay-dialog-err').textContent = e.message;
        document.getElementById('pay-dialog-err').style.display = 'block';
    }
}

async function exportInstallmentsExcel() {
    try {
        const res = await fetch('/api/installments/export/excel', {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        if (!res.ok) { showToast('Export xətası', 'error'); return; }
        const blob = await res.blob();
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement('a');
        a.href     = url;
        a.download = `nisiye-${new Date().toISOString().slice(0,10)}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
    } catch (e) {
        showToast(e.message, 'error');
    }
}

function statusBadge(s) {
    return { ACTIVE: 'badge-blue', COMPLETED: 'badge-green', OVERDUE: 'badge-red' }[s] || 'badge-blue';
}
function statusLabel(s) {
    return { ACTIVE: 'Aktiv', COMPLETED: 'Tamamlandı', OVERDUE: 'Gecikmiş' }[s] || s;
}
function isOverdue(pay) {
    return !pay.isPaid && pay.dueDate < new Date().toISOString().slice(0, 10);
}