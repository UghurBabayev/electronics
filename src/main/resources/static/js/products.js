let _prodSearchTimer = null;

async function loadProducts() {
    const [models, brands, categories] = await Promise.all([
        API.get('/models'),
        API.get('/brands'),
        API.get('/categories')
    ]);
    window._models = models;

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Məhsullar</div>
            <div class="page-subtitle">Anbar inventarı</div>
        </div>
        <div class="card">
            <div class="toolbar">
                <div class="toolbar-left">
                    <input class="search-input" id="prod-search" placeholder="Model axtar..." oninput="onProdSearch()">
                </div>
                <button class="btn btn-primary" onclick="showProductForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Məhsul əlavə et
                </button>
            </div>
            <div class="filter-bar">
                <div class="filter-group">
                    <label>Marka</label>
                    <select id="prod-brand-filter" onchange="loadProductsPage(0)">
                        <option value="">Hamısı</option>
                        ${brands.map(b => `<option value="${b.id}">${b.name}</option>`).join('')}
                    </select>
                </div>
                <div class="filter-group">
                    <label>Kateqoriya</label>
                    <select id="prod-cat-filter" onchange="loadProductsPage(0)">
                        <option value="">Hamısı</option>
                        ${categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('')}
                    </select>
                </div>
                <div class="filter-group">
                    <label>Status</label>
                    <select id="prod-status-filter" onchange="loadProductsPage(0)">
                        <option value="">Hamısı</option>
                        <option value="IN_STOCK">Stokda</option>
                        <option value="SOLD">Satılıb</option>
                    </select>
                </div>
                <button class="btn btn-ghost btn-sm" onclick="clearProdFilters()">Sıfırla</button>
            </div>
            <div class="table-wrap">
                <table id="prod-table">
                    <thead><tr>
                        <th>Model</th><th>Kateqoriya</th><th>Marka</th>
                        <th>Alış qiyməti</th><th>Satış qiyməti</th><th>Status</th><th>Alış tarixi</th><th></th>
                    </tr></thead>
                    <tbody id="prod-body"></tbody>
                </table>
            </div>
            <div id="prod-pagination"></div>
        </div>`;

    await loadProductsPage(0);
}

function clearProdFilters() {
    const ids = ['prod-search', 'prod-brand-filter', 'prod-cat-filter', 'prod-status-filter'];
    ids.forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
    loadProductsPage(0);
}

async function loadProductsPage(page) {
    const search  = document.getElementById('prod-search')?.value?.trim() || '';
    const brandId = document.getElementById('prod-brand-filter')?.value || '';
    const catId   = document.getElementById('prod-cat-filter')?.value || '';
    const status  = document.getElementById('prod-status-filter')?.value || '';

    const params = new URLSearchParams({ page, size: 20 });
    if (search)  params.append('search', search);
    if (brandId) params.append('brandId', brandId);
    if (catId)   params.append('categoryId', catId);
    if (status)  params.append('status', status);

    try {
        const data = await API.get('/products?' + params);
        renderProductRows(data.content);
        renderPagination('prod-pagination', data.page, data.totalPages, data.totalElements, data.size, 'loadProductsPage');
    } catch (e) { showToast(e.message, 'error'); }
}

function onProdSearch() {
    clearTimeout(_prodSearchTimer);
    _prodSearchTimer = setTimeout(() => loadProductsPage(0), 300);
}

function renderProductRows(list) {
    const tbody = document.getElementById('prod-body');
    if (!list || !list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="8">Məhsul tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(p => `
        <tr>
            <td><strong>${p.modelName || '—'}</strong></td>
            <td>${p.category || '—'}</td>
            <td>${p.brand || '—'}</td>
            <td>${fmt(p.purchasePrice)}</td>
            <td>${p.salePrice ? fmt(p.salePrice) : '<span style="color:var(--text-muted)">Qoyulmayıb</span>'}</td>
            <td><span class="badge ${p.quantity > 0 ? 'badge-green' : 'badge-red'}">${p.quantity > 0 ? 'Stokda' : 'Satılıb'}</span></td>
            <td>${fmtDate(p.purchaseDate)}</td>
            <td>
                <button class="btn btn-ghost btn-sm" onclick="showProductForm(${p.id})">Düzəlt</button>
                <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id})">Sil</button>
            </td>
        </tr>`).join('');
}

function onModelSelect() {}

let _qmState = { name: '', brandId: '', catId: '' };

function _saveQmState() {
    _qmState = {
        name:    document.getElementById('qm-name')?.value || '',
        brandId: document.getElementById('qm-brand')?.value || '',
        catId:   document.getElementById('qm-cat')?.value || ''
    };
}

function _openModelForm(state, categories, brands) {
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Yeni model</span>
                <button class="modal-close" onclick="reloadProductForm()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label>
                    <input id="qm-name" value="${state.name}" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Marka</label>
                        <div style="display:flex;gap:6px">
                            <select id="qm-brand" style="flex:1">
                                <option value="">Seçin</option>
                                ${brands.map(b => `<option value="${b.id}" ${state.brandId==b.id?'selected':''}>${b.name}</option>`).join('')}
                            </select>
                            <button type="button" class="btn btn-ghost btn-sm" onclick="quickAddBrand()" title="Yeni marka">+</button>
                        </div></div>
                    <div class="form-group"><label>Kateqoriya</label>
                        <div style="display:flex;gap:6px">
                            <select id="qm-cat" style="flex:1">
                                <option value="">Seçin</option>
                                ${categories.map(c => `<option value="${c.id}" ${state.catId==c.id?'selected':''}>${c.name}</option>`).join('')}
                            </select>
                            <button type="button" class="btn btn-ghost btn-sm" onclick="quickAddCategory()" title="Yeni kateqoriya">+</button>
                        </div></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="reloadProductForm()">Geri</button>
                <button class="btn btn-primary" onclick="saveQuickModel()">Yarat</button>
            </div>
        </div>`);
}

async function quickAddModel() {
    const [categories, brands] = await Promise.all([
        API.get('/categories'),
        API.get('/brands')
    ]);
    _openModelForm({ name: '', brandId: '', catId: '' }, categories, brands);
}

async function reloadModelForm() {
    const state = { ..._qmState };
    const [categories, brands] = await Promise.all([
        API.get('/categories'),
        API.get('/brands')
    ]);
    _openModelForm(state, categories, brands);
}

async function quickAddBrand() {
    _saveQmState();
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Yeni marka</span>
                <button class="modal-close" onclick="reloadModelForm()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label><input id="qb-name" required></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="reloadModelForm()">Geri</button>
                <button class="btn btn-primary" onclick="saveQuickBrand()">Yarat</button>
            </div>
        </div>`);
}

async function saveQuickBrand() {
    const name = document.getElementById('qb-name').value.trim();
    if (!name) return showToast('Ad daxil edin', 'error');
    try {
        const brand = await API.post('/brands', { name });
        _qmState.brandId = String(brand.id);
        showToast('Marka əlavə edildi');
        reloadModelForm();
    } catch (e) { showToast(e.message, 'error'); }
}

async function quickAddCategory() {
    _saveQmState();
    openModal(`
        <div class="modal">
            <div class="modal-header"><span class="modal-title">Yeni kateqoriya</span>
                <button class="modal-close" onclick="reloadModelForm()">×</button></div>
            <div class="modal-body">
                <div class="form-group"><label>Ad *</label><input id="qc-name" required></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="reloadModelForm()">Geri</button>
                <button class="btn btn-primary" onclick="saveQuickCategory()">Yarat</button>
            </div>
        </div>`);
}

async function saveQuickCategory() {
    const name = document.getElementById('qc-name').value.trim();
    if (!name) return showToast('Ad daxil edin', 'error');
    try {
        const cat = await API.post('/categories', { name });
        _qmState.catId = String(cat.id);
        showToast('Kateqoriya əlavə edildi');
        reloadModelForm();
    } catch (e) { showToast(e.message, 'error'); }
}

async function showProductForm(id = null) {
    const [models, p] = await Promise.all([
        Promise.resolve(window._models || API.get('/models')),
        id ? API.get('/products/' + id) : Promise.resolve(null)
    ]);
    const isEdit = id !== null;

    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">${isEdit ? 'Məhsulu düzəlt' : 'Yeni məhsul (alış)'}</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="prod-form-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group"><label>Model *</label>
                    <div style="display:flex;gap:6px">
                        <select id="pf-model" style="flex:1" onchange="onModelSelect()">
                            <option value="">Seçin</option>
                            ${models.map(m => `<option value="${m.id}" ${p?.modelId==m.id?'selected':''}>${m.name}${m.brand ? ' — '+m.brand : ''}</option>`).join('')}
                        </select>
                        <button type="button" class="btn btn-ghost btn-sm" onclick="quickAddModel()" title="Yeni model">+</button>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Alış qiyməti (₼) *</label>
                        <input id="pf-price" type="number" step="0.01" min="0" value="${p?.purchasePrice || ''}"></div>
                    <div class="form-group"><label>Satış qiyməti (₼)</label>
                        <input id="pf-sale-price" type="number" step="0.01" min="0" value="${p?.salePrice || ''}" placeholder="İstəyə bağlı"></div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Alış tarixi *</label>
                        <input id="pf-date" type="date" value="${p?.purchaseDate || today()}"></div>
                    ${!isEdit ? `<div class="form-group"><label>Miqdar *</label>
                        <input id="pf-qty" type="number" min="1" value="1"
                            title="Hər unit ayrı sıra kimi əlavə olunur"></div>` : ''}
                </div>
                <div class="form-group"><label>Açıqlama</label>
                    <textarea id="pf-desc" rows="2">${p?.description || ''}</textarea></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveProduct(${id || 'null'})">${isEdit ? 'Yadda saxla' : 'Əlavə et'}</button>
            </div>
        </div>`);
}

async function saveQuickModel() {
    try {
        const model = await API.post('/models', {
            name:       document.getElementById('qm-name').value,
            brandId:    document.getElementById('qm-brand').value || null,
            categoryId: document.getElementById('qm-cat').value || null
        });
        window._models = window._models || [];
        window._models.push(model);
        showToast('Model əlavə edildi');
        closeModal();
        showProductForm();
        setTimeout(() => {
            const sel = document.getElementById('pf-model');
            if (sel) sel.value = model.id;
        }, 50);
    } catch (e) { showToast(e.message, 'error'); }
}

function reloadProductForm() {
    closeModal();
    showProductForm();
}

async function saveProduct(id) {
    const qtyEl = document.getElementById('pf-qty');
    const body = {
        modelId:       document.getElementById('pf-model').value || null,
        purchasePrice: document.getElementById('pf-price').value,
        salePrice:     document.getElementById('pf-sale-price').value || null,
        purchaseDate:  document.getElementById('pf-date').value,
        quantity:      qtyEl ? parseInt(qtyEl.value) : 1,
        description:   document.getElementById('pf-desc').value || null
    };
    try {
        if (id) await API.put('/products/' + id, body);
        else     await API.post('/products', body);
        closeModal();
        showToast(id ? 'Məhsul yeniləndi' : (body.quantity > 1 ? `${body.quantity} unit əlavə edildi` : 'Məhsul əlavə edildi'));
        loadProductsPage(0);
    } catch (e) {
        document.getElementById('prod-form-err').textContent = e.message;
        document.getElementById('prod-form-err').style.display = 'block';
    }
}

async function deleteProduct(id) {
    if (!confirm('Bu məhsulu silmək istədiyinizə əminsiniz?')) return;
    try {
        await API.delete('/products/' + id);
        showToast('Məhsul silindi');
        loadProductsPage(0);
    } catch (e) { showToast(e.message, 'error'); }
}