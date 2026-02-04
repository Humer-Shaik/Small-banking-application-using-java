// Simple front-end demo logic (not real banking backend)
let generatedAccNo = "";
let storedName = "";
let storedPin = "";
let balance = 5000;

let attempts = 0;
let locked = false;
let loggedIn = false;

const $ = (id) => document.getElementById(id);

function randomAccNo() {
  return "SB" + Math.floor(10000000 + Math.random() * 90000000);
}

function enableATM(status) {
  ["balanceBtn", "depositBtn", "withdrawBtn", "logoutBtn"].forEach(id => {
    $(id).disabled = !status;
  });
}

$("nextBtn").addEventListener("click", () => {
  const name = $("nameInput").value.trim();
  if (!name) {
    $("createMsg").textContent = "Enter name first.";
    return;
  }

  storedName = name;
  generatedAccNo = randomAccNo();
  $("accNoText").textContent = generatedAccNo;
  $("createMsg").textContent = "Account number generated. Now set PIN.";
});

$("createAccountBtn").addEventListener("click", () => {
  const pin = $("pinInput").value.trim();
  const pin2 = $("pinConfirmInput").value.trim();

  if (!generatedAccNo) {
    $("createMsg").textContent = "Generate account number first.";
    return;
  }
  if (!/^\d{4}$/.test(pin)) {
    $("createMsg").textContent = "PIN must be exactly 4 digits.";
    return;
  }
  if (pin !== pin2) {
    $("createMsg").textContent = "PIN mismatch.";
    return;
  }

  storedPin = pin;
  $("createMsg").textContent = "Account created successfully. Go to login.";
  $("loginNameInput").value = storedName;
  $("loginAccNoInput").value = generatedAccNo;
});

$("loginNameInput").addEventListener("input", () => {
  const name = $("loginNameInput").value.trim();
  if (name === storedName) $("loginAccNoInput").value = generatedAccNo;
  else $("loginAccNoInput").value = "";
});

$("loginBtn").addEventListener("click", () => {
  if (locked) {
    $("loginMsg").textContent = "Account locked (3 wrong attempts).";
    return;
  }

  const name = $("loginNameInput").value.trim();
  const pin = $("loginPinInput").value.trim();

  if (name !== storedName) {
    $("loginMsg").textContent = "Account not found.";
    return;
  }
  if (pin !== storedPin) {
    attempts++;
    $("loginMsg").textContent = `Wrong PIN. Attempts left: ${3 - attempts}`;
    if (attempts >= 3) {
      locked = true;
      $("loginMsg").textContent = "Account locked.";
    }
    return;
  }

  loggedIn = true;
  $("loginMsg").textContent = "Login successful.";
  $("screen").textContent = `Welcome ${storedName}!\nAccount: ${generatedAccNo}\n\nUse ATM buttons.`;
  enableATM(true);
});

$("balanceBtn").addEventListener("click", () => {
  if (!loggedIn) return;
  $("screen").textContent = `Balance: Rs ${balance}`;
});

$("depositBtn").addEventListener("click", () => {
  if (!loggedIn) return;
  const amt = parseInt(prompt("Enter deposit amount:"), 10);
  if (isNaN(amt) || amt <= 0) return;
  balance += amt;
  $("screen").textContent = `Deposited: Rs ${amt}\nBalance: Rs ${balance}`;
});

$("withdrawBtn").addEventListener("click", () => {
  if (!loggedIn) return;
  const amt = parseInt(prompt("Enter withdraw amount:"), 10);
  if (isNaN(amt) || amt <= 0) return;

  if (amt > balance) {
    $("screen").textContent = "Insufficient balance.";
    return;
  }

  balance -= amt;
  $("screen").textContent = `Withdrawn: Rs ${amt}\nBalance: Rs ${balance}`;
});

$("logoutBtn").addEventListener("click", () => {
  loggedIn = false;
  enableATM(false);
  $("screen").textContent = "Logged out. Please login to use ATM.";
});
