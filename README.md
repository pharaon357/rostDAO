# rostDAO

**rostDAO** is a Java package that simplifies the use of the **DAO (Data Access Object) design pattern** by providing ready-to-use implementations for data management.  
It reduces code duplication, improves security, and makes it easier to integrate multiple storage backends such as SQL databases, CSV files, and XML files.

📄 This repository also contains the article:  
*A new package for the DAO pattern in Java* (© 2024, Ramsès Talla) – licensed under [CC BY-NC-ND 4.0](https://creativecommons.org/licenses/by-nc-nd/4.0/).

---

## ✨ Features

- Full support for **CRUDL methods** (Create, Read, Update, Delete, List) → **23 access methods** available.
- Works with multiple data storage systems:
  - SQL databases (via JDBC)
  - CSV files
  - XML files
- Strong **security measures**:
  - Input validation
  - Use of `PreparedStatement` to prevent SQL injection
  - Immutable lists to prevent TOCTTOU vulnerabilities
- Highly **testable** and **extensible** design.
- Reduces implementation of DAO classes from hundreds of lines → to just a few instantiations.

---

## 📦 Installation

Clone this repository:

```bash
git clone https://github.com/pharaon357/rostDAO.git
cd rostDAO
