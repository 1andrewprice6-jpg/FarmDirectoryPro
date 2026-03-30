const db = require('./connection');

db.serialize(() => {
  console.log('[0xAF1] Initiating database migration protocol...');

  db.run(`CREATE TABLE IF NOT EXISTS farmers (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    farmName TEXT,
    address TEXT,
    phone TEXT,
    email TEXT,
    type TEXT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
  )`);

  db.run(`CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'worker'
  )`);

  console.log('[0xAF1] Migration protocol completed successfully.');
});
