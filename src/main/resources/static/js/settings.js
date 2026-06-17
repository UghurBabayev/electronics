async function loadSettings() {
    document.getElementById('content').innerHTML =
        '<div style="color:var(--text-muted);padding:40px;text-align:center">Yüklənir...</div>';
    const isAdmin = (JSON.parse(localStorage.getItem('user') || '{}').role === 'ADMIN');
    let cats, brands, models, balances, users;
    try {
        [cats, brands, models, balances, users] = await Promise.all([
            API.get('/categories'),
            API.get('/brands'),
            API.get('/models'),
            API.get('/balances'),
            isAdmin ? API.get('/users') : Promise.resolve(null)
        ]);
    } catch (e) {
        document.getElementById('content').innerHTML =
            `<div class="alert alert-error">${e.message}</div>`;
        return;
    }

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
                    <thead><tr><th>Ad</th><th>Marka</th><th>Kateqoriya</th><th></th></tr></thead>
                    <tbody>
                        ${models.map(m => `
                        <tr>
                            <td><strong>${m.name}</strong></td>
                            <td>${m.brand || '—'}</td>
                            <td>${m.category || '—'}</td>
                            <td>
                                <button class="btn btn-ghost btn-sm" onclick="showModelForm(${m.id})">Düzəlt</button>
                                <button class="btn btn-danger btn-sm" onclick="deleteModel(${m.id})">Sil</button>
                            </td>
                        </tr>`).join('') || '<tr class="empty-row"><td colspan="4">Model yoxdur</td></tr>'}
                    </tbody>
                </table>
            </div>
        </div>

        ${isAdmin && users ? `
        <!-- İstifadəçilər (yalnız ADMIN) -->
        <div class="card" style="margin-bottom:20px">
            <div class="toolbar" style="margin-bottom:12px">
                <div>
                    <strong>İstifadəçilər</strong>
                    <div style="font-size:13px;color:var(--text-muted);margin-top:2px">Sistemə giriş hüququ olan istifadəçilər</div>
                </div>
                <button class="btn btn-primary btn-sm" onclick="showUserForm()">+ Yeni istifadəçi</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Ad Soyad</th><th>İstifadəçi adı</th><th>Rol</th><th>Status</th><th>Müddət</th><th></th></tr></thead>
                    <tbody>
                        ${users.map(u => `
                        <tr>
                            <td><strong>${u.fullName}</strong></td>
                            <td>${u.username}</td>
                            <td><span class="badge ${u.role==='ADMIN'?'badge-blue':'badge-gray'}">${u.role}</span></td>
                            <td><span class="badge ${u.active?'badge-green':'badge-red'}">${u.active?'Aktiv':'Deaktiv'}</span></td>
                            <td>${accessUntilCell(u.accessUntil)}</td>
                            <td>
                                <button class="btn btn-ghost btn-sm" onclick="showUserForm(${JSON.stringify(u).replace(/"/g,'&quot;')})">Düzəlt</button>
                                <button class="btn btn-primary btn-sm" onclick="extendUserAccess(${u.id})" title="1 ay uzat">+1 ay</button>
                                <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id},'${u.username}')">Sil</button>
                            </td>
                        </tr>`).join('') || '<tr class="empty-row"><td colspan="6">İstifadəçi yoxdur</td></tr>'}
                    </tbody>
                </table>
            </div>
        </div>` : ''}

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
        categoryId: document.getElementById('mod-cat').value || null
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

function showUserForm(u = null) {
    const isEdit = u !== null;
    if (typeof u === 'string') u = JSON.parse(u);
    openModal(`
        <div class="modal">
            <div class="modal-header">
                <span class="modal-title">${isEdit ? 'İstifadəçini düzəlt' : 'Yeni istifadəçi'}</span>
                <button class="modal-close" onclick="closeModal()">×</button>
            </div>
            <div class="modal-body">
                <div id="user-form-err" class="alert alert-error" style="display:none"></div>
                ${!isEdit ? `<div class="form-group"><label>İstifadəçi adı *</label>
                    <input id="usr-name" value="" required placeholder="admin2"></div>` : ''}
                <div class="form-group"><label>Ad Soyad *</label>
                    <input id="usr-fullname" value="${isEdit ? u.fullName : ''}" required></div>
                <div class="form-group"><label>Rol *</label>
                    <select id="usr-role">
                        <option value="USER" ${isEdit && u.role==='USER'?'selected':''}>USER</option>
                        <option value="ADMIN" ${isEdit && u.role==='ADMIN'?'selected':''}>ADMIN</option>
                    </select></div>
                ${isEdit ? `<div class="form-group"><label>Status</label>
                    <select id="usr-active">
                        <option value="true" ${u.active?'selected':''}>Aktiv</option>
                        <option value="false" ${!u.active?'selected':''}>Deaktiv</option>
                    </select></div>` : ''}
                <div class="form-group"><label>${isEdit ? 'Yeni şifrə (boş buraxsanız dəyişmir)' : 'Şifrə *'}</label>
                    <input id="usr-pass" type="password" ${!isEdit?'required':''} placeholder="${isEdit?'Dəyişmək istəmirsinizsə boş buraxın':''}"></div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-ghost" onclick="closeModal()">Ləğv et</button>
                <button class="btn btn-primary" onclick="saveUser(${isEdit ? u.id : 'null'})">Yadda saxla</button>
            </div>
        </div>`);
}

async function saveUser(id) {
    const errEl = document.getElementById('user-form-err');
    errEl.style.display = 'none';
    try {
        if (id) {
            await API.put('/users/' + id, {
                fullName:    document.getElementById('usr-fullname').value,
                role:        document.getElementById('usr-role').value,
                active:      document.getElementById('usr-active').value === 'true',
                newPassword: document.getElementById('usr-pass').value || null
            });
            showToast('İstifadəçi yeniləndi');
        } else {
            await API.post('/users', {
                username: document.getElementById('usr-name').value,
                fullName: document.getElementById('usr-fullname').value,
                role:     document.getElementById('usr-role').value,
                password: document.getElementById('usr-pass').value
            });
            showToast('İstifadəçi yaradıldı');
        }
        closeModal(); loadSettings();
    } catch (e) {
        errEl.textContent = e.message; errEl.style.display = 'block';
    }
}

async function deleteUser(id, username) {
    if (!confirm(`"${username}" istifadəçisi silinsin? Bu əməliyyat geri alına bilməz.`)) return;
    try { await API.delete('/users/' + id); showToast('İstifadəçi silindi'); loadSettings(); }
    catch (e) { showToast(e.message, 'error'); }
}

async function extendUserAccess(id) {
    try {
        await API.post('/users/' + id + '/extend');
        showToast('Müddət 1 ay uzadıldı');
        loadSettings();
    } catch (e) { showToast(e.message, 'error'); }
}

function accessUntilCell(dateStr) {
    if (!dateStr) return '<span style="color:var(--text-muted)">Limitsiz</span>';
    const today = new Date(); today.setHours(0,0,0,0);
    const until = new Date(dateStr);
    const diffDays = Math.ceil((until - today) / 86400000);
    if (diffDays < 0)
        return `<span style="color:var(--danger);font-weight:600">Bitib (${fmtDate(dateStr)})</span>`;
    if (diffDays <= 7)
        return `<span style="color:#f59e0b;font-weight:600">${diffDays} gün qalıb</span>`;
    return `<span style="color:var(--text-muted)">${fmtDate(dateStr)}</span>`;
}