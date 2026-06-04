async function loadCustomers() {
    const customers = await API.get('/customers');

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Müştərilər</div>
            <div class="page-subtitle">Müştəri bazası</div>
        </div>
        <div class="card">
            <div class="toolbar">
                <input class="search-input" id="cust-search" placeholder="Müştəri axtar..."
                    oninput="filterCustomers()">
                <button class="btn btn-primary" onclick="showCustomerForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Müştəri əlavə et
                </button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr>
                        <th>Ad Soyad</th><th>Telefon</th><th>Ünvan</th><th>Qeyd</th><th></th>
                    </tr></thead>
                    <tbody id="cust-body"></tbody>
                </table>
            </div>
        </div>`;

    window._customers = customers;
    renderCustomerRows(customers);
}

function renderCustomerRows(list) {
    const tbody = document.getElementById('cust-body');
    if (!list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="5">Müştəri tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(c => `
        <tr>
            <td><strong>${c.fullName}</strong></td>
            <td>${c.phone || '—'}</td>
            <td>${c.address || '—'}</td>
            <td>${c.note || '—'}</td>
            <td>
                <button class="btn btn-ghost btn-sm" onclick="showCustomerForm(${c.id})">Düzəlt</button>
                <button class="btn btn-danger btn-sm" onclick="deleteCustomer(${c.id})">Sil</button>
            </td>
        </tr>`).join('');
}

function filterCustomers() {
    const q = document.getElementById('cust-search').value.toLowerCase();
    renderCustomerRows(window._customers.filter(c => c.fullName.toLowerCase().includes(q)));
}

function showCustomerForm(id = null) {
    const c = id ? window._customers.find(x => x.id === id) : {};
    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">${id ? 'Müştərini düzəlt' : 'Yeni müştəri'}</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="cust-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group"><label>Ad Soyad *</label>
                    <input id="cf-name" value="${c.fullName || ''}" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Telefon</label>
                        <input id="cf-phone" value="${c.phone || ''}"></div>
                    <div class="form-group"><label>Ünvan</label>
                        <input id="cf-address" value="${c.address || ''}"></div>
                </div>
                <div class="form-group"><label>Qeyd</label>
                    <textarea id="cf-note" rows="2">${c.note || ''}</textarea></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveCustomer(${id || 'null'})">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveCustomer(id) {
    const body = {
        fullName: document.getElementById('cf-name').value,
        phone:    document.getElementById('cf-phone').value || null,
        address:  document.getElementById('cf-address').value || null,
        note:     document.getElementById('cf-note').value || null
    };
    try {
        if (id) await API.put('/customers/' + id, body);
        else     await API.post('/customers', body);
        closeModal();
        showToast(id ? 'Müştəri yeniləndi' : 'Müştəri əlavə edildi');
        loadCustomers();
    } catch (e) {
        document.getElementById('cust-err').textContent = e.message;
        document.getElementById('cust-err').style.display = 'block';
    }
}

async function deleteCustomer(id) {
    if (!confirm('Bu müştərini silmək istədiyinizə əminsiniz?')) return;
    try {
        await API.delete('/customers/' + id);
        showToast('Müştəri silindi');
        loadCustomers();
    } catch (e) { showToast(e.message, 'error'); }
}