# To-Do CRUD Java Puro

Proyecto educativo sin frameworks: Java puro, MongoDB, Socket o 
HTTP simple.

## Estructura

- `frontend/`: cliente Java
- `backend/`: servidor Java
- `shared/`: clases compartidas
- `mongodb/`: scripts de base de datos

## Requisitos

- JDK 8+
- MongoDB 4+
- Git

## 🏗️ Build

```bash
chmod +x build.sh
./build.sh
```
---

## 📁 Estructura final deseada
```
todo-crud-java/
├── frontend/
│   ├── lib/
│   ├── src/
│   │   └── com/
│   │       └── todo/
│   │           ├── Main.java
│   │           └── ui/
├── backend/
│   ├── lib/
│   ├── src/
│   │   └── com/
│   │       └── todo/
│   │           ├── Main.java
│   │           └── server/
├── shared/
│   └── src/
│       └── com/
│           └── todo/
│               └── model/
│                   └── Task.java
├── mongodb/
│   └── scripts/
│       └── init.js
├── .gitignore
├── README.md
└── build.sh
```

---