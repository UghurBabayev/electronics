async function loadProducts() {
    const [products, categories, brands] = await Promise.all([
        API.get('/products'),
        API.get('/categories'),
        API.get('/brands')
    ]);

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Məhsullar</div>
            <div class="page-subtitle">Anbar inventarı</div>
        </div>
        <div class="card">
            <div class="toolbar">
                <div class="toolbar-left">
                    <input class="search-input" id="prod-search" placeholder="Məhsul axtar..." oninput="filterProducts()">
                    <select id="prod-cat-filter" onchange="filterProducts()" style="width:160px">
                        <option value="">Bütün kateqoriyalar</option>
                        ${categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('')}
                    </select>
                </div>
                <button class="btn btn-primary" onclick="showProductForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Məhsul əlavə et
                </button>
            </div>
            <div class="table-wrap">
                <table id="prod-table">
                    <thead><tr>
                        <th>Ad</th><th>Kateqoriya</th><th>Marka</th>
                        <th>Alış qiyməti</th><th>Stok</th><th>Alış tarixi</th><th></th>
                    </tr></thead>
                    <tbody id="prod-body"></tbody>
                </table>
            </div>
        </div>`;

    window._products = products;
    window._categories = categories;
    window._brands = brands;
    renderProductRows(products);
}

function renderProductRows(list) {
    const tbody = document.getElementById('prod-body');
    if (!list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="7">Məhsul tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(p => `
        <tr>
            <td><strong>${p.name}</strong></td>
            <td>${p.category || '—'}</td>
            <td>${p.brand || '—'}</td>
            <td>${fmt(p.purchasePrice)}</td>
            <td><span class="badge ${p.quantity > 0 ? 'badge-green' : 'badge-red'}">${p.quantity} ədəd</span></td>
            <td>${fmtDate(p.purchaseDate)}</td>
            <td>
                <button class="btn btn-ghost btn-sm" onclick="showProductForm(${p.id})">Düzəlt</button>
                <button class="btn btn-danger btn-sm" onclick="deleteProduct(${p.id})">Sil</button>
            </td>
        </tr>`).join('');
}

function filterProducts() {
    const q = document.getElementById('prod-search').value.toLowerCase();
    const cat = document.getElementById('prod-cat-filter').value;
    const filtered = window._products.filter(p =>
        p.name.toLowerCase().includes(q) &&
        (!cat || String(p.categoryId) === cat)
    );
    renderProductRows(filtered);
}

async function showProductForm(id = null) {
    const p = id ? window._products.find(x => x.id === id) : {};
    const cats = window._categories;
    const brands = window._brands;

    const overlay = openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">${id ? 'Məhsulu düzəlt' : 'Yeni məhsul'}</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="prod-form-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group"><label>Ad *</label>
                    <input id="pf-name" value="${p.name || ''}" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Kateqoriya</label>
                        <select id="pf-cat">
                            <option value="">Seçin</option>
                            ${cats.map(c => `<option value="${c.id}" ${p.categoryId==c.id?'selected':''}>${c.name}</option>`).join('')}
                        </select></div>
                    <div class="form-group"><label>Marka</label>
                        <select id="pf-brand">
                            <option value="">Seçin</option>
                            ${brands.map(b => `<option value="${b.id}" ${p.brandId==b.id?'selected':''}>${b.name}</option>`).join('')}
                        </select></div>
                </div>
                <div class="form-row">
                    <div class="form-group"><label>Alış qiyməti (₼) *</label>
                        <input id="pf-price" type="number" step="0.01" min="0" value="${p.purchasePrice || ''}"></div>
                    <div class="form-group"><label>Alış tarixi *</label>
                        <input id="pf-date" type="date" value="${p.purchaseDate || today()}"></div>
                </div>
                <div class="form-group"><label>Miqdar *</label>
                    <input id="pf-qty" type="number" min="1" value="${p.quantity ?? 1}"></div>
                <div class="form-group"><label>Açıqlama</label>
                    <textarea id="pf-desc" rows="2">${p.description || ''}</textarea></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveProduct(${id || 'null'})">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveProduct(id) {
    const body = {
        name: document.getElementById('pf-name').value,
        categoryId: document.getElementById('pf-cat').value || null,
        brandId: document.getElementById('pf-brand').value || null,
        purchasePrice: document.getElementById('pf-price').value,
        purchaseDate: document.getElementById('pf-date').value,
        quantity: parseInt(document.getElementById('pf-qty').value),
        description: document.getElementById('pf-desc').value || null
    };
    try {
        if (id) await API.put('/products/' + id, body);
        else     await API.post('/products', body);
        closeModal();
        showToast(id ? 'Məhsul yeniləndi' : 'Məhsul əlavə edildi');
        loadProducts();
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
        loadProducts();
    } catch (e) { showToast(e.message, 'error'); }
}