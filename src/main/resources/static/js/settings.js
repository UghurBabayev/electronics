async function loadSettings() {
    const [cats, brands, models, balances] = await Promise.all([
        API.get('/categories'),
        API.get('/brands'),
        API.get('/models'),
        API.get('/balances')
    ]);

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Ayarlar</div>
        </div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-bottom:20px">

            <!-- Kateqoriyalar -->
            <div class="card">
                <div class="toolbar" style="margin-bottom:12px">
                    <strong>Kateqoriyalar</strong>
                    <button class="btn btn-primary btn-sm" onclick="showCatForm()">+ Əlavə et</button>
                </div>
                <div id="cat-list">
                    ${cats.map(c => `
                    <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border)">
                        <span>${c.name}</span>
                        <button class="btn btn-danger btn-sm" onclick="deleteCat(${c.id})">Sil</button>
                    </div>`).join('')}
                </div>
            </div>

            <!-- Markalar -->
            <div class="card">
                <div class="toolbar" style="margin-bottom:12px">
                    <strong>Markalar</strong>
                    <button class="btn btn-primary btn-sm" onclick="showBrandForm()">+ Əlavə et</button>
                </div>
                <div id="brand-list">
                    ${brands.map(b => `
                    <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border)">
                        <span>${b.name}</span>
                        <button class="btn btn-danger btn-sm" onclick="deleteBrand(${b.id})">Sil</button>
                    </div>`).join('')}
                </div>
            </div>
        </div>

        <!-- Modellər -->
        <div class="card" style="margin-bottom:20px">
            <div class="toolbar" style="margin-bottom:12px">
                <div>
                    <strong>Modellər</strong>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:2px">Məhsul modelləri</div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="showModelForm()">+ Əlavə et</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Ad</th><th>Marka</th><th>Kateqoriya</th><th>Satış qiyməti</th><th></th></tr></thead>
                    <tbody>
                        ${models.map(m => `
                        <tr>
                            <td><strong>${m.name}</strong></td>
                            <td>${m.brand || '—'}</td>
                            <td>${m.category || '—'}</td>
                            <td>${m.salePrice ? fmt(m.salePrice) : '—'}</td>
                            <td>
                                <button class="btn btn-ghost btn-sm" onclick="showModelForm(${m.id})">Düzəlt</button>
                                <button class="btn btn-danger btn-sm" onclick="deleteModel(${m.id})">Sil</button>
                            </td>
                        </tr>`).join('') || '<tr class="empty-row"><td colspan="5">Model yoxdur</td></tr>'}
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Başlanğıc məbləğ -->
        <div class="card">
            <div class="toolbar" style="margin-bottom:12px">
                <div>
                    <strong>Başlanğıc məbləğ</strong>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:2px">Kassaya əlavə edilən məbləğlər</div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="showBalanceForm()">+ Əlavə et</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Tarix</th><th>Məbləğ</th><th>Qeyd</th><th></th></tr></thead>
                    <tbody id="balance-body">
                        ${balances.map(b => `
                        <tr>
                            <td>${fmtDate(b.balanceDate)}</td>
                            <td><strong>${fmt(b.amount)}</strong></td>
                            <td>${b.note || '—'}</td>
                            <td><button class="btn btn-danger btn-sm" onclick="deleteBalance(${b.id})">Sil</button></td>
                        </tr>`).join('') || '<tr class="empty-row"><td colspan="4">Məbləğ yoxdur</td></tr>'}
                    </tbody>
                </table>
            </div>
        </div>`;
}

function showCatForm() {
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Yeni kateqoriya</span>
                <button class="modal-close" onclick="closeModal()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label><input id="cat-name" required></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveCat()">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveCat() {
    try {
        await API.post('/categories', { name: document.getElementById('cat-name').value });
        closeModal(); showToast('Kateqoriya əlavə edildi'); loadSettings();
    } catch (e) { showToast(e.message, 'error'); }
}

async function deleteCat(id) {
    if (!confirm('Silinsin?')) return;
    try { await API.delete('/categories/' + id); showToast('Silindi'); loadSettings(); }
    catch (e) { showToast(e.message, 'error'); }
}

async function showModelForm(id = null) {
    const [cats, brands] = await Promise.all([API.get('/categories'), API.get('/brands')]);
    const m = id ? (await API.get('/models')).find(x => x.id === id) : {};
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">${id ? 'Modeli düzəlt' : 'Yeni model'}</span>
                <button class="modal-close" onclick="closeModal()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label><input id="mod-name" value="${m.name || ''}" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Marka</label>
                        <select id="mod-brand">
                            <option value="">Seçin</option>
                            ${brands.map(b => `<option value="${b.id}" ${m.brandId==b.id?'selected':''}>${b.name}</option>`).join('')}
                        </select></div>
                    <div class="form-group"><label>Kateqoriya</label>
                        <select id="mod-cat">
                            <option value="">Seçin</option>
                            ${cats.map(c => `<option value="${c.id}" ${m.categoryId==c.id?'selected':''}>${c.name}</option>`).join('')}
                        </select></div>
                </div>
                <div class="form-group"><label>Satış qiyməti (₼)</label>
                    <input id="mod-price" type="number" step="0.01" min="0" value="${m.salePrice || ''}"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveModel(${id || 'null'})">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveModel(id) {
    const body = {
        name:       document.getElementById('mod-name').value,
        brandId:    document.getElementById('mod-brand').value || null,
        categoryId: document.getElementById('mod-cat').value || null,
        salePrice:  document.getElementById('mod-price').value || null
    };
    try {
        if (id) await API.put('/models/' + id, body);
        else    await API.post('/models', body);
        closeModal(); showToast(id ? 'Model yeniləndi' : 'Model əlavə edildi'); loadSettings();
    } catch (e) { showToast(e.message, 'error'); }
}

async function deleteModel(id) {
    if (!confirm('Silinsin?')) return;
    try { await API.delete('/models/' + id); showToast('Silindi'); loadSettings(); }
    catch (e) { showToast(e.message, 'error'); }
}

function showBrandForm() {
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Yeni marka</span>
                <button class="modal-close" onclick="closeModal()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label><input id="brand-name" required></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveBrand()">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveBrand() {
    try {
        await API.post('/brands', { name: document.getElementById('brand-name').value });
        closeModal(); showToast('Marka əlavə edildi'); loadSettings();
    } catch (e) { showToast(e.message, 'error'); }
}

async function deleteBrand(id) {
    if (!confirm('Silinsin?')) return;
    try { await API.delete('/brands/' + id); showToast('Silindi'); loadSettings(); }
    catch (e) { showToast(e.message, 'error'); }
}

function showBalanceForm() {
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Məbləğ əlavə et</span>
                <button class="modal-close" onclick="closeModal()">×</button></div>
            <div class="modal-body">
                <div class="form-row">
                    <div class="form-group"><label>Məbləğ (₼) *</label>
                        <input id="bal-amount" type="number" step="0.01" min="0"></div>
                    <div class="form-group"><label>Tarix *</label>
                        <input id="bal-date" type="date" value="${today()}"></div>
                </div>
                <div class="form-group"><label>Qeyd</label>
                    <textarea id="bal-note" rows="2"></textarea></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveBalance()">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveBalance() {
    try {
        await API.post('/balances', {
            amount:      document.getElementById('bal-amount').value,
            balanceDate: document.getElementById('bal-date').value,
            note:        document.getElementById('bal-note').value || null
        });
        closeModal(); showToast('Məbləğ əlavə edildi'); loadSettings();
    } catch (e) { showToast(e.message, 'error'); }
}

async function deleteBalance(id) {
    if (!confirm('Silinsin?')) return;
    try { await API.delete('/balances/' + id); showToast('Silindi'); loadSettings(); }
    catch (e) { showToast(e.message, 'error'); }
}