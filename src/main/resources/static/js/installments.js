async function loadInstallments() {
    const plans = await API.get('/installments');

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Nisiyə</div>
            <div class="page-subtitle">Aktiv nisiyə planları və ödəniş cədvəli</div>
        </div>
        <div class="toolbar" style="margin-bottom:16px">
            <div class="toolbar-left">
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('ALL')" id="f-all">Hamısı</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('ACTIVE')" id="f-active">Aktiv</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('OVERDUE')" id="f-overdue">Gecikmiş</button>
                <button class="btn btn-ghost btn-sm" onclick="filterInstallments('COMPLETED')" id="f-completed">Tamamlanmış</button>
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
        <div class="card" style="margin-bottom:16px">
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
                ${plan.payments.map(pay => `
                    <div class="payment-row">
                        <div>
                            <span class="${isOverdue(pay) ? 'payment-overdue' : ''}">${fmtDate(pay.dueDate)}</span>
                            ${isOverdue(pay) ? '<span class="badge badge-red" style="margin-left:6px;font-size:10px">Gecikmiş</span>' : ''}
                        </div>
                        <div style="display:flex;align-items:center;gap:10px">
                            <span style="font-weight:600">${fmt(pay.amount)}</span>
                            ${pay.isPaid
                                ? `<span class="badge badge-green">Ödənilib ${fmtDate(pay.paidDate)}</span>`
                                : `<button class="btn btn-success btn-sm" onclick="markPaid(${pay.id})">Ödənildi</button>`}
                        </div>
                    </div>`).join('')}
            </div>
        </div>`).join('');
}

async function markPaid(paymentId) {
    try {
        await API.post(`/installments/payments/${paymentId}/pay`);
        showToast('Ödəniş qeyd edildi');
        loadInstallments();
    } catch (e) { showToast(e.message, 'error'); }
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