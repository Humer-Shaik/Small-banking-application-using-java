# Small Banking Application (Java)

A small banking application implemented in **Java** that simulates an **ATM / Banking system**.  
This project is built for **learning + demonstration** of core Java concepts like:

- OOP (Abstraction, Inheritance, Polymorphism, Encapsulation)
- Input validation
- Secure PIN handling (Hashing)
- Basic transaction processing

---

## Table of contents

- [About](#about)
- [Project objective](#project-objective)
- [Key features](#key-features)
- [OOP concepts used](#oop-concepts-used)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Clone the repository](#clone-the-repository)
- [Build & Run](#build--run)
  - [Run using Terminal](#run-using-terminal)
  - [Run using VS Code](#run-using-vs-code)
- [Usage Flow](#usage-flow)
- [Sample Output](#sample-output)
- [Security & Data integrity](#security--data-integrity)
- [Screenshots](#screenshots)
- [Future improvements](#future-improvements)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

---

## About

This repository contains a simple **banking / ATM simulation** built using Java.  
It allows a user to **create an account**, **set their own PIN**, **login securely**, and perform banking operations such as **deposit**, **withdraw**, and **balance check**.

The goal is to build a beginner-friendly but realistic Java project that demonstrates **clean OOP design** and **basic security**.

---

## Project objective

This project was designed to:

- Learn and apply **core Java OOP principles**
- Practice **object-oriented design**
- Implement a **secure ATM PIN system** using hashing
- Build a project that can be extended into:
  - DB-based system (SQLite/MySQL)
  - Web app (Spring Boot + REST API + HTML/CSS UI)
  - Desktop app (JavaFX)

---

## Key features

✅ Account Creation  
- Enter account holder name  
- Random account number is generated automatically  
- User creates a secure 4-digit ATM PIN

✅ Secure Login  
- Login using Account Holder name  
- Account number auto-fills  
- PIN verification with **3 attempts only**
- Locks account after 3 failed attempts

✅ Banking Operations  
- Check Balance  
- Deposit  
- Withdraw  
- Mini Statement (transaction summary)

✅ Secure Storage  
- PIN is stored as **SHA-256 hash**, not plain text  
- Balance stored in integer paise/cents (long) for accuracy

---

## OOP concepts used

This project demonstrates all major OOP concepts:

### 1. Encapsulation
- Private fields inside `Account`
- Controlled access using getters
- Balance updates only through safe methods

### 2. Abstraction
- `Account` is an abstract class
- Shared operations defined once

### 3. Inheritance
- `SavingsAccount extends Account`
- Can be extended to `CurrentAccount`, etc.

### 4. Polymorphism
- ATM uses reference type `Account`
- Different account types can behave differently

---

## Tech stack

- **Language:** Java
- **Concepts:** OOP + Basic Security
- **UI (Optional):** HTML + CSS + JS files included (UI prototype)
- **Version Control:** Git + GitHub

---

## Project structure

```bash
Small-banking-application-using-java/
│
├── Main.java              # Main source code (ATM logic)
├── README.md              # Documentation
├── .gitignore             # Ignore compiled files
│
├── index.html             # UI prototype (optional)
├── style.css              # UI prototype styling
├── script.js              # UI prototype logic
│
└── (compiled files ignored)
