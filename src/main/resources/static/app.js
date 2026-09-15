const API = "/api/books";

const els = {
    body: document.getElementById("booksBody"),
    empty: document.getElementById("emptyState"),
    search: document.getElementById("searchInput"),
    searchBtn: document.getElementById("searchBtn"),
    resetBtn: document.getElementById("resetBtn"),
    newBtn: document.getElementById("newBtn"),
    dialog: document.getElementById("bookDialog"),
    form: document.getElementById("bookForm"),
    dialogTitle: document.getElementById("dialogTitle"),
    cancelBtn: document.getElementById("cancelBtn"),
    formError: document.getElementById("formError"),

    id: document.getElementById("bookId"),
    title: document.getElementById("title"),
    author: document.getElementById("author"),
    genre: document.getElementById("genre"),
    isbn: document.getElementById("isbn"),
    price: document.getElementById("price"),
    stock: document.getElementById("stock"),
    year: document.getElementById("year"),
};

async function apiList(query) {
    const url = query ? `${API}?title=${encodeURIComponent(query)}` : API;
    const res = await fetch(url);
    return res.json();
}

async function apiCreate(book) {
    return send(API, "POST", book);
}

async function apiUpdate(id, book) {
    return send(`${API}/${id}`, "PUT", book);
}

async function apiDelete(id) {
    const res = await fetch(`${API}/${id}`, {method: "DELETE"});
    if (!res.ok) throw new Error("Delete failed");
}

async function send(url, method, book) {
    const res = await fetch(url, {
        method,
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(book),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        const msg = data.fields
            ? Object.entries(data.fields).map(([k, v]) => `${k}: ${v}`).join(", ")
            : (data.message || "Request failed");
        throw new Error(msg);
    }
    return data;
}

function money(v) {
    return Number(v).toFixed(2);
}

function esc(s) {
    return String(s ?? "").replace(/[&<>"]/g, c =>
        ({"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;"}[c]));
}

function render(books) {
    els.body.innerHTML = "";
    els.empty.hidden = books.length > 0;
    for (const b of books) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${esc(b.title)}</td>
            <td>${esc(b.author)}</td>
            <td>${esc(b.genre)}</td>
            <td>${esc(b.isbn)}</td>
            <td class="num">${money(b.price)}</td>
            <td class="num">${b.stock}</td>
            <td class="num">${b.year}</td>
            <td class="actions-col">
                <button class="btn" data-edit="${b.id}">Ред.</button>
                <button class="btn btn-danger" data-del="${b.id}">✕</button>
            </td>`;
        els.body.appendChild(tr);
    }
}

async function reload(query) {
    try {
        render(await apiList(query));
    } catch (e) {
        alert("Не вдалося завантажити книги: " + e.message);
    }
}

function openDialog(book) {
    els.formError.hidden = true;
    els.form.reset();
    if (book) {
        els.dialogTitle.textContent = "Редагувати книгу";
        els.id.value = book.id;
        els.title.value = book.title ?? "";
        els.author.value = book.author ?? "";
        els.genre.value = book.genre ?? "";
        els.isbn.value = book.isbn ?? "";
        els.price.value = book.price ?? 0;
        els.stock.value = book.stock ?? 0;
        els.year.value = book.year ?? 0;
    } else {
        els.dialogTitle.textContent = "Нова книга";
        els.id.value = "";
    }
    els.dialog.showModal();
}

function readForm() {
    return {
        title: els.title.value.trim(),
        author: els.author.value.trim(),
        genre: els.genre.value.trim(),
        isbn: els.isbn.value.trim(),
        price: parseFloat(els.price.value) || 0,
        stock: parseInt(els.stock.value, 10) || 0,
        year: parseInt(els.year.value, 10) || 0,
    };
}

els.newBtn.addEventListener("click", () => openDialog(null));
els.cancelBtn.addEventListener("click", () => els.dialog.close());

els.searchBtn.addEventListener("click", () => reload(els.search.value.trim()));
els.search.addEventListener("keydown", e => {
    if (e.key === "Enter") reload(els.search.value.trim());
});
els.resetBtn.addEventListener("click", () => {
    els.search.value = "";
    reload();
});

els.body.addEventListener("click", async e => {
    const editId = e.target.getAttribute("data-edit");
    const delId = e.target.getAttribute("data-del");
    if (editId) {
        const res = await fetch(`${API}/${editId}`);
        openDialog(await res.json());
    } else if (delId) {
        if (confirm("Видалити цю книгу?")) {
            try {
                await apiDelete(delId);
                reload(els.search.value.trim());
            } catch (err) {
                alert(err.message);
            }
        }
    }
});

els.form.addEventListener("submit", async e => {
    e.preventDefault();
    const book = readForm();
    const id = els.id.value;
    try {
        if (id) {
            await apiUpdate(id, book);
        } else {
            await apiCreate(book);
        }
        els.dialog.close();
        reload(els.search.value.trim());
    } catch (err) {
        els.formError.textContent = err.message;
        els.formError.hidden = false;
    }
});

reload();
