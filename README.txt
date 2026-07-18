COS 202 SMART LIBRARY CIRCULATION & AUTOMATION SYSTEM

REQUIREMENTS
- Java JDK 17 or newer

COMPILE (run from the project folder)
  mkdir -p out
  javac -d out $(find src -name "*.java")

RUN
  java -cp out Main

DATA
- Application data is saved automatically in the data/ folder.
- File > Export Data lets you copy persistent data to another folder.
- File > Import Data loads data from an exported folder.

MAIN FEATURES
- Add books, magazines, journals, and users
- Borrow and return items
- FIFO reservation queue with duplicate prevention
- Stack-based undo for item deletion
- Safe deletion: borrowed or reserved items cannot be removed
- Linear, binary, and recursive search by title, author, or type
- Selection, insertion, merge, and quick sort by title, author, year, or type
- Fixed-size array cache ranked by real access frequency
- Persistent items, users, current borrowings, borrowing history, due dates,
  access counts, and reservation queues
- Reports: most borrowed items, overdue users, category distribution,
  and frequently accessed items
- Swing GUI with tabbed panels, CardLayout, timer reminders,
  validation dialogs, mnemonics, tooltips, file chooser, and custom rendering
