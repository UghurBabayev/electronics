async function loadSales() {
    const sales = await API.get('/sales');

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Satışlar</div>
            <div class="page-subtitle">Bütün satış əməliyyatları</div>
        </div>
        <div class="card">
            <div class="toolbar">
                <div class="toolbar-left">
                    <div class="date-range">
                        <input type="date" id="sale-from" value="${firstOfMonth()}" onchange="filterSalesByDate()">
                        <span>—</span>
                        <input type="date" id="sale-to" value="${today()}" onchange="filterSalesByDate()">
                    </div>
                </div>
                <button class="btn btn-primary" onclick="showSaleForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Yeni satış
                </button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr>
                        <th>Məhsul</th><th>Müştəri</th><th>Satış qiyməti</th>
                        <th>Ödəniş</th><th>Miqdar</th><th>Tarix</th>
                    </tr></thead>
                    <tbody id="sale-body"></tbody>
                </table>
            </div>
        </div>`;

    window._sales = sales;
    renderSaleRows(sales);
}

function renderSaleRows(list) {
    const tbody = document.getElementById('sale-body');
    if (!list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="6">Satış tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(s => `
        <tr>
            <td><strong>${s.productName}</strong></td>
            <td>${s.customerName || '—'}</td>
            <td>${fmt(s.salePrice)}</td>
            <td><span class="badge ${s.paymentType === 'CASH' ? 'badge-green' : 'badge-orange'}">
                ${s.paymentType === 'CASH' ? 'Nağd' : 'Nisiyə'}</span></td>
            <td>${s.quantity}</td>
            <td>${fmtDate(s.saleDate)}</td>
        </tr>`).join('');
}

async function filterSalesByDate() {
    const from = document.getElementById('sale-from').value;
    const to   = document.getElementById('sale-to').value;
    if (!from || !to) return;
    try {
        const sales = await API.get(`/sales?from=${from}&to=${to}`);
        window._sales = sales;
        renderSaleRows(sales);
    } catch (e) { showToast(e.message, 'error'); }
}

async function showSaleForm() {
    const [products, customers] = await Promise.all([
        API.get('/products?inStock=true'),
        API.get('/customers')
    ]);

    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">Yeni satış</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="sale-form-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group"><label>Məhsul *</label>
                    <select id="sf-product">
                        <option value="">Seçin</option>
                        ${products.map(p => `<option value="${p.id}">${p.name} (${p.quantity} ədəd qaldı)</option>`).join('')}
                    </select></div>
                <div class="form-group"><label>Müştəri</label>
                    <select id="sf-customer">
                        <option value="">Seçin (istəğe bağlı)</option>
                        ${customers.map(c => `<option value="${c.id}">${c.fullName}</option>`).join('')}
                    </select></div>
                <div class="form-row">
                    <div class="form-group"><label>Satış qiyməti (₼) *</label>
                        <input id="sf-price" type="number" step="0.01" min="0"></div>
                    <div class="form-group"><label>Miqdar *</label>
                        <input id="sf-qty" type="number" min="1" value="1"></div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Ödəniş növü *</label>
                        <select id="sf-type" onchange="toggleInstallmentFields()">
                            <option value="CASH">Nağd</option>
                            <option value="CREDIT">Nisiyə</option>
                        </select></div>
                    <div class="form-group"><label>Satış tarixi *</label>
                        <input id="sf-date" type="date" value="${today()}"></div>
                </div>
                <div id="installment-fields" style="display:none; border-top:1px solid var(--border); padding-top:16px; margin-top:4px;">
                    <div class="form-row">
                        <div class="form-group"><label>Aylıq ödəniş (₼) *</label>
                            <input id="sf-monthly" type="number" step="0.01" min="0"></div>
                        <div class="form-group"><label>Müddət (ay) *</label>
                            <input id="sf-duration" type="number" min="1" value="12"></div>
                    </div>
                </div>
                <div class="form-group"><label>Qeyd</label>
                    <textarea id="sf-note" rows="2"></textarea></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveSale()">Satışı qeyd et</button>
            </div>
        </div>`);
}

function toggleInstallmentFields() {
    const isCredit = document.getElementById('sf-type').value === 'CREDIT';
    document.getElementById('installment-fields').style.display = isCredit ? 'block' : 'none';
}

async function saveSale() {
    const isCredit = document.getElementById('sf-type').value === 'CREDIT';
    const body = {
        productId:      document.getElementById('sf-product').value || null,
        customerId:     document.getElementById('sf-customer').value || null,
        salePrice:      document.getElementById('sf-price').value,
        saleDate:       document.getElementById('sf-date').value,
        paymentType:    document.getElementById('sf-type').value,
        quantity:       parseInt(document.getElementById('sf-qty').value),
        note:           document.getElementById('sf-note').value || null,
        monthlyPayment: isCredit ? document.getElementById('sf-monthly').value : null,
        durationMonths: isCredit ? parseInt(document.getElementById('sf-duration').value) : null
    };
    try {
        await API.post('/sales', body);
        closeModal();
        showToast('Satış qeyd edildi');
        loadSales();
    } catch (e) {
        document.getElementById('sale-form-err').textContent = e.message;
        document.getElementById('sale-form-err').style.display = 'block';
    }
}

function firstOfMonth() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-01`;
}