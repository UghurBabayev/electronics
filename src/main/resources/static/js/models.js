async function loadModels() {
    const [models, cats, brands] = await Promise.all([
        API.get('/models'),
        API.get('/categories'),
        API.get('/brands')
    ]);

    window._cats   = cats;
    window._brands = brands;

    document.getElementById('content').innerHTML = `
        <div class="page-header">
            <div class="page-title">Modellər</div>
            <div class="page-subtitle">Məhsul modelləri</div>
        </div>
        <div class="card">
            <div class="toolbar">
                <div class="toolbar-left">
                    <input class="search-input" id="model-search" placeholder="Model axtar..." oninput="filterModels()">
                </div>
                <button class="btn btn-primary" onclick="showModelPageForm()">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Model əlavə et
                </button>
            </div>
            <div class="table-wrap">
                <table id="model-table">
                    <thead><tr>
                        <th>Ad</th><th>Marka</th><th>Kateqoriya</th><th></th>
                    </tr></thead>
                    <tbody id="model-body"></tbody>
                </table>
            </div>
        </div>`;

    window._models_page = models;
    renderModelRows(models);
}

function renderModelRows(list) {
    const tbody = document.getElementById('model-body');
    if (!list.length) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="4">Model tapılmadı</td></tr>';
        return;
    }
    tbody.innerHTML = list.map(m => `
        <tr>
            <td><strong>${m.name}</strong></td>
            <td>${m.brand || '—'}</td>
            <td>${m.category || '—'}</td>
            <td>
                <button class="btn btn-ghost btn-sm" onclick="showModelPageForm(${m.id})">Düzəlt</button>
                <button class="btn btn-danger btn-sm" onclick="deleteModelPage(${m.id}, '${m.name.replace(/'/g,"\\'")}')">Sil</button>
            </td>
        </tr>`).join('');
}

function filterModels() {
    const q = document.getElementById('model-search').value.toLowerCase();
    const filtered = (window._models_page || []).filter(m =>
        m.name.toLowerCase().includes(q) ||
        (m.brand || '').toLowerCase().includes(q) ||
        (m.category || '').toLowerCase().includes(q)
    );
    renderModelRows(filtered);
}

function showModelPageForm(id = null) {
    const cats   = window._cats   || [];
    const brands = window._brands || [];
    const m = id ? (window._models_page || []).find(x => x.id === id) : {};

    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">${id ? 'Modeli düzəlt' : 'Yeni model'}</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="model-page-err" class="alert alert-error" style="display:none"></div>
                <div class="form-group"><label>Ad *</label>
                    <input id="mp-name" value="${m?.name || ''}" required></div>
                <div class="form-row">
                    <div class="form-group"><label>Marka</label>
                        <select id="mp-brand">
                            <option value="">Seçin</option>
                            ${brands.map(b => `<option value="${b.id}" ${m?.brandId==b.id?'selected':''}>${b.name}</option>`).join('')}
                        </select></div>
                    <div class="form-group"><label>Kateqoriya</label>
                        <select id="mp-cat">
                            <option value="">Seçin</option>
                            ${cats.map(c => `<option value="${c.id}" ${m?.categoryId==c.id?'selected':''}>${c.name}</option>`).join('')}
                        </select></div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveModelPage(${id || 'null'})">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveModelPage(id) {
    const errEl = document.getElementById('model-page-err');
    errEl.style.display = 'none';
    const body = {
        name:       document.getElementById('mp-name').value,
        brandId:    document.getElementById('mp-brand').value || null,
        categoryId: document.getElementById('mp-cat').value || null
    };
    try {
        if (id) await API.put('/models/' + id, body);
        else    await API.post('/models', body);
        closeModal();
        showToast(id ? 'Model yeniləndi' : 'Model əlavə edildi');
        loadModels();
    } catch (e) {
        errEl.textContent = e.message;
        errEl.style.display = 'block';
    }
}

async function deleteModelPage(id, name) {
    if (!confirm(`"${name}" modeli silinsin?`)) return;
    try {
        await API.delete('/models/' + id);
        showToast('Model silindi');
        loadModels();
    } catch (e) { showToast(e.message, 'error'); }
}