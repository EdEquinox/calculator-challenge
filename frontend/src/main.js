
// DOM-driven calculator script for the button-based UI in index.html
const display = document.getElementById('display');
const requestIdEl = document.getElementById('request-id');
const numberButtons = document.querySelectorAll('[data-number]');
const operatorButtons = document.querySelectorAll('[data-operator]');
const equalsButton = document.getElementById('equals');
const clearButton = document.getElementById('clear');
const signButton = document.getElementById('sign');
const percentButton = document.getElementById('percent');

const API_BASE_URL = '';

let currentValue = '0';
let storedValue = null;
let currentOperator = null;

function updateDisplay() {
    display.textContent = currentValue;
}

function appendNumber(d) {
    if (currentValue === '0' && d !== '.') currentValue = d;
    else if (d === '.' && currentValue.includes('.')) return;
    else currentValue = currentValue + d;
    updateDisplay();
}

function chooseOperator(op) {
    if (currentOperator !== null) {
        // chain compute locally before changing operator
        computeLocal();
    }
    storedValue = currentValue;
    currentValue = '0';
    currentOperator = op;
}

function computeLocal() {
    if (currentOperator == null || storedValue == null) return;
    const a = parseFloat(storedValue);
    const b = parseFloat(currentValue);
    let res;
    switch (currentOperator) {
        case 'add': res = a + b; break;
        case 'subtract': res = a - b; break;
        case 'multiply': res = a * b; break;
        case 'divide': res = b === 0 ? 'ERR' : a / b; break;
        default: return;
    }
    currentValue = String(res);
    currentOperator = null;
    storedValue = null;
    updateDisplay();
}

function clearAll() {
    currentValue = '0';
    storedValue = null;
    currentOperator = null;
    requestIdEl.textContent = '';
    updateDisplay();
}

function toggleSign() {
    if (currentValue === '0') return;
    if (currentValue.startsWith('-')) currentValue = currentValue.slice(1);
    else currentValue = '-' + currentValue;
    updateDisplay();
}

function percent() {
    currentValue = String(parseFloat(currentValue) / 100);
    updateDisplay();
}

async function performRemoteCompute(operation) {
    // If there is a storedValue (operator chosen) use that as operand1, otherwise use currentValue
    const operand1 = storedValue !== null ? storedValue : currentValue;
    const operand2 = currentValue;

    requestIdEl.textContent = 'Calculando...';

    const url = `${API_BASE_URL}/${operation}?operand1=${encodeURIComponent(operand1)}&operand2=${encodeURIComponent(operand2)}`;

    try {
        const resp = await fetch(url);
        const rid = resp.headers.get('x-request-id') || 'N/A';
        const data = await resp.json().catch(() => ({}));
        if (!resp.ok || data.error) {
            requestIdEl.textContent = `Erro: ${data.error || resp.status}`;
            currentValue = 'ERR';
            updateDisplay();
            return;
        }
        currentValue = String(data.result);
        requestIdEl.textContent = `ID: ${rid}`;
        updateDisplay();
    } catch (err) {
        requestIdEl.textContent = 'Falha de comunicação';
        currentValue = 'ERR';
        updateDisplay();
    } finally {
        storedValue = null;
        currentOperator = null;
    }
}

numberButtons.forEach(btn => {
    btn.addEventListener('click', () => appendNumber(btn.dataset.number));
});

operatorButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const op = btn.dataset.operator;
        // If '=' should trigger, not here
        if (op) {
            // if there's already a stored value and operator, we can do remote compute on equals only
            chooseOperator(op);
        }
    });
});

equalsButton.addEventListener('click', async () => {
    if (currentOperator) {
        await performRemoteCompute(currentOperator);
    }
});

clearButton.addEventListener('click', clearAll);
signButton.addEventListener('click', toggleSign);
percentButton.addEventListener('click', percent);

updateDisplay();