/**
 * QUEUE VISUALIZER — CLIENT CONTROLLER
 * Connects frontend interface to the Java REST API.
 */

const API = "/api";

// DOM Elements
const metricSize = document.getElementById("metricSize");
const metricState = document.getElementById("metricState");
const metricFront = document.getElementById("metricFront");
const metricRear = document.getElementById("metricRear");
const queueStage = document.getElementById("queueStage");
const enqueueInput = document.getElementById("enqueueInput");
const btnEnqueue = document.getElementById("btnEnqueue");
const btnDequeue = document.getElementById("btnDequeue");
const btnPeek = document.getElementById("btnPeek");
const btnClear = document.getElementById("btnClear");
const resultBox = document.getElementById("resultBox");
const resultIcon = document.getElementById("resultIcon");
const resultText = document.getElementById("resultText");
const resultTime = document.getElementById("resultTime");
const dbStatusBadge = document.getElementById("dbStatusBadge");
const dbNotice = document.getElementById("dbNotice");
const historyTableBody = document.getElementById("historyTableBody");

// ==========================================================================
// 1. FETCH & RENDER QUEUE STATE
// ==========================================================================

async function fetchQueue() {
    try {
        const response = await fetch(`${API}/queue`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        const data = await response.json();
        renderQueue(data);
        updateDatabaseStatus(data.databaseConnected);
    } catch (err) {
        showResult("error", `Failed to connect to Java server: ${err.message}. Is QueueServer running on port 8080?`);
        updateDatabaseStatus(false);
    }
}

function renderQueue(data) {
    const items = data.queue || [];
    const size = items.length;

    // Update Top Metric Counters
    metricSize.textContent = size;
    if (size === 0) {
        metricState.textContent = "EMPTY";
        metricState.style.color = "#94a3b8";
        metricFront.textContent = "—";
        metricRear.textContent = "—";
    } else {
        metricState.textContent = `ACTIVE (${size})`;
        metricState.style.color = "#34d399";
        metricFront.textContent = items[0];
        metricRear.textContent = items[size - 1];
    }

    // Render Stage
    queueStage.innerHTML = "";

    if (size === 0) {
        queueStage.innerHTML = `
            <div class="queue-empty-state">
                <div class="empty-icon-wrap">
                    <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                        <path d="M9 17h6M9 13h6M9 9h6M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v14a2 2 0 01-2 2z"></path>
                    </svg>
                </div>
                <p class="empty-primary">The queue is currently empty</p>
                <p class="empty-secondary">Enter a value and click Enqueue to watch FIFO in action.</p>
            </div>
        `;
        return;
    }

    // Build Nodes
    items.forEach((item, index) => {
        const isFront = index === 0;
        const isRear = index === size - 1;

        const nodeWrapper = document.createElement("div");
        nodeWrapper.className = "queue-node-wrapper";
        nodeWrapper.id = `node-${index}`;

        // Classes for node styling
        let nodeClasses = "queue-node";
        if (isFront) nodeClasses += " is-front";
        if (isRear) nodeClasses += " is-rear";

        // Badges HTML
        let tagHtml = "";
        if (isFront && isRear) {
            tagHtml = `<span class="node-tag tag-both">FRONT &amp; REAR</span>`;
        } else if (isFront) {
            tagHtml = `<span class="node-tag tag-front">FRONT</span>`;
        } else if (isRear) {
            tagHtml = `<span class="node-tag tag-rear">REAR</span>`;
        }

        const safeValue = escapeHtml(item);

        nodeWrapper.innerHTML = `
            <div class="${nodeClasses}">
                <div class="node-header">
                    <span class="node-index">Node [${index}]</span>
                    <div class="node-tag-group">${tagHtml}</div>
                </div>
                <div class="node-body">
                    <div class="node-value" title="${safeValue}">${safeValue}</div>
                </div>
                <div class="node-footer">
                    <span>${isFront ? "Head" : "Inner Node"}</span>
                    <span>${isRear ? "Tail &rarr; null" : "&rarr; next"}</span>
                </div>
            </div>
            ${!isRear ? `
                <div class="node-pointer" title="Pointer to next node">
                    <div class="pointer-line">
                        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M5 12h14M12 5l7 7-7 7"></path>
                        </svg>
                    </div>
                    <span class="pointer-text">next</span>
                </div>
            ` : ""}
        `;

        queueStage.appendChild(nodeWrapper);
    });

    // Auto scroll stage to reveal newest item if enqueued
    queueStage.scrollLeft = queueStage.scrollWidth;
}

// ==========================================================================
// 2. ENQUEUE OPERATION
// ==========================================================================

async function handleEnqueue(event) {
    if (event) event.preventDefault();

    const value = enqueueInput.value.trim();
    if (!value) {
        showResult("error", "Please enter a value before enqueuing.");
        enqueueInput.focus();
        return;
    }

    setButtonLoading(btnEnqueue, true, "Enqueuing...");

    try {
        const response = await fetch(`${API}/enqueue`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ value: value })
        });

        const data = await response.json();

        if (response.ok && data.success !== false) {
            showResult("success", `Enqueued "${value}" at the rear. Queue size is now ${data.size}.`);
            enqueueInput.value = "";
            enqueueInput.focus();
            await fetchQueue();
            fetchHistory(); // silent refresh
        } else {
            showResult("error", data.message || "Failed to enqueue value.");
        }
    } catch (err) {
        showResult("error", `Network error: ${err.message}`);
    } finally {
        setButtonLoading(btnEnqueue, false, "Enqueue");
    }
}

// ==========================================================================
// 3. DEQUEUE OPERATION
// ==========================================================================

async function handleDequeue() {
    setButtonLoading(btnDequeue, true, "Dequeuing...");

    // Animate front node exit if present
    const firstNode = document.getElementById("node-0");
    if (firstNode) {
        firstNode.classList.add("node-dequeuing");
        await new Promise(r => setTimeout(r, 220));
    }

    try {
        const response = await fetch(`${API}/dequeue`, {
            method: "POST"
        });

        const data = await response.json();

        if (data.value !== null && data.value !== undefined) {
            showResult("success", `Dequeued front element: "${data.value}". Queue size is now ${data.size}.`);
        } else {
            showResult("info", data.message || "Queue is already empty. Underflow avoided.");
        }

        await fetchQueue();
        fetchHistory(); // silent refresh
    } catch (err) {
        showResult("error", `Network error: ${err.message}`);
    } finally {
        setButtonLoading(btnDequeue, false, "Dequeue (Poll)");
    }
}

// ==========================================================================
// 4. PEEK OPERATION
// ==========================================================================

async function handlePeek() {
    setButtonLoading(btnPeek, true, "Peeking...");

    try {
        const response = await fetch(`${API}/peek`);
        const data = await response.json();

        if (data.value !== null && data.value !== undefined) {
            showResult("info", `Peek Front Element: "${data.value}". Element remains in the queue.`);

            // Highlight front node on screen
            const frontNodeElement = document.querySelector(".queue-node.is-front");
            if (frontNodeElement) {
                frontNodeElement.classList.add("is-peeked");
                setTimeout(() => {
                    frontNodeElement.classList.remove("is-peeked");
                }, 2000);
            }
        } else {
            showResult("info", "Queue is empty. There is no front element to peek.");
        }
    } catch (err) {
        showResult("error", `Network error: ${err.message}`);
    } finally {
        setButtonLoading(btnPeek, false, "Peek (Front)");
    }
}

// ==========================================================================
// 5. CLEAR QUEUE OPERATION
// ==========================================================================

async function handleClearQueue() {
    const confirmed = confirm("Are you sure you want to clear all elements from the queue?");
    if (!confirmed) return;

    setButtonLoading(btnClear, true, "Clearing...");

    try {
        const response = await fetch(`${API}/clear`, {
            method: "POST"
        });
        const data = await response.json();

        showResult("success", data.message || "Queue has been cleared.");
        await fetchQueue();
    } catch (err) {
        showResult("error", `Network error: ${err.message}`);
    } finally {
        setButtonLoading(btnClear, false, "Clear Queue");
    }
}

// ==========================================================================
// 6. DATABASE HISTORY
// ==========================================================================

async function fetchHistory() {
    try {
        const response = await fetch(`${API}/history`);
        const data = await response.json();

        updateDatabaseStatus(data.databaseConnected);

        const records = data.history || [];
        historyTableBody.innerHTML = "";

        if (records.length === 0) {
            historyTableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="text-center text-muted py-4">
                        ${data.databaseConnected 
                            ? "No transaction history recorded yet." 
                            : "Database logging offline. Operations are currently running in-memory."}
                    </td>
                </tr>
            `;
            return;
        }

        records.forEach(row => {
            const tr = document.createElement("tr");
            const opClass = row.operation === "ENQUEUE" ? "op-enqueue" : "op-dequeue";
            tr.innerHTML = `
                <td><code>#${row.id}</code></td>
                <td><span class="op-badge ${opClass}">${escapeHtml(row.operation)}</span></td>
                <td><strong>${escapeHtml(row.value)}</strong></td>
                <td class="text-muted">${escapeHtml(row.time)}</td>
            `;
            historyTableBody.appendChild(tr);
        });
    } catch (err) {
        // Fallback gracefully without breaking page
        updateDatabaseStatus(false);
    }
}

async function handleClearHistory() {
    const confirmed = confirm("Are you sure you want to clear the persistent database history log?");
    if (!confirmed) return;

    try {
        const response = await fetch(`${API}/history`, {
            method: "DELETE"
        });
        const data = await response.json();

        showResult("info", data.message || "History cleared.");
        fetchHistory();
    } catch (err) {
        showResult("error", `Failed to clear history: ${err.message}`);
    }
}

// ==========================================================================
// 7. UI HELPERS & STATUS
// ==========================================================================

function updateDatabaseStatus(isConnected) {
    if (isConnected) {
        dbStatusBadge.textContent = "MySQL: Connected";
        dbStatusBadge.className = "badge badge-online";
        dbNotice.style.display = "none";
    } else {
        dbStatusBadge.textContent = "MySQL: Offline (In-Memory)";
        dbStatusBadge.className = "badge badge-offline";
        dbNotice.style.display = "flex";
    }
}

function showResult(type, message) {
    resultBox.className = `result-box ${type}`;
    resultText.textContent = message;
    resultTime.textContent = new Date().toLocaleTimeString();

    // Icon updates
    if (type === "success") {
        resultIcon.innerHTML = `
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
        `;
    } else if (type === "error") {
        resultIcon.innerHTML = `
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
        `;
    } else {
        resultIcon.innerHTML = `
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
        `;
    }
}

function setButtonLoading(button, isLoading, text) {
    if (!button) return;
    button.disabled = isLoading;
    const span = button.querySelector("span");
    if (span) span.textContent = text;
}

function quickFill(value) {
    enqueueInput.value = value;
    enqueueInput.focus();
}

function quickRandom() {
    const num = Math.floor(100 + Math.random() * 900);
    enqueueInput.value = `Task-${num}`;
    enqueueInput.focus();
}

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ==========================================================================
// 8. INITIALIZATION
// ==========================================================================

document.addEventListener("DOMContentLoaded", () => {
    fetchQueue();
    fetchHistory();
    enqueueInput.focus();
});