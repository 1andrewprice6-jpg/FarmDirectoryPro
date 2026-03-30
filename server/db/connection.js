const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.resolve(__dirname, 'farm_directory.db');
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('[0xAF1] Database connection error:', err.message);
  } else {
    console.log('[0xAF1] Database firmament established.');
  }
});

module.exports = db;
