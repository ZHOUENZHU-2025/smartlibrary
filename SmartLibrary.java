import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * SmartLibrary — Command-Line Interface for the Smart Library System.
 *
 * v1.3 — Data Persistence additions:
 *   Startup : Checks for "current_catalogue.txt" first (saved session).
 *             Falls back to "books.txt" (read-only default) on first run.
 *             Restores the borrowing history stack from "current_history.txt"
 *             if it exists.
 *   Exit    : Saves both the BST catalogue and the history stack to
 *             "current_catalogue.txt" and "current_history.txt" before
 *             terminating, so the session is fully restored next launch.
 *
 * All library operations go through the LibraryADT interface only —
 * LibraryImpl is never referenced after construction (Information Hiding).
 *
 * @author Senior Java Engineer
 * @version 1.3
 */
public class SmartLibrary {

    // ── File paths ────────────────────────────────────────────────────────────
    /** Read-only fallback catalogue for first-run initialisation. */
    private static final String DEFAULT_CATALOGUE = "books.txt";

    /** Auto-saved catalogue written on exit and read back on the next launch. */
    private static final String SAVED_CATALOGUE = "current_catalogue.txt";

    /** Auto-saved history stack written on exit and read back on the next launch. */
    private static final String SAVED_HISTORY = "current_history.txt";

    // =========================================================================
    //  Entry point
    // =========================================================================

    public static void main(String[] args) {

        // Declare as LibraryADT — concrete type is hidden from this point on
        LibraryADT library = new LibraryImpl();
        Scanner    scanner  = new Scanner(System.in);

        // ── Data Initialisation ───────────────────────────────────────────────
        File savedCatalogue = new File(SAVED_CATALOGUE);

        if (savedCatalogue.exists()) {
            // Restore a previously saved session
            System.out.println("Saved session detected. Loading catalogue from \"" +
                    SAVED_CATALOGUE + "\"...");
            library.preloadData(SAVED_CATALOGUE);
        } else {
            // First-run: seed from the read-only default catalogue
            System.out.println("No saved session found. Loading default catalogue from \"" +
                    DEFAULT_CATALOGUE + "\"...");
            library.preloadData(DEFAULT_CATALOGUE);
        }

        // Restore borrowing history (graceful no-op if file is absent)
        System.out.println("Restoring borrowing history from \"" + SAVED_HISTORY + "\"...");
        library.preloadHistory(SAVED_HISTORY);

        // ── Menu loop ─────────────────────────────────────────────────────────
        while (true) {
            System.out.println("\n=== Smart Library System ===");
            System.out.println("1. Add Book");
            System.out.println("2. Search Book");
            System.out.println("3. Borrow Book");
            System.out.println("4. View History");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter numbers only.");
                scanner.nextLine();
                continue;
            }
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleAddBook(library, scanner);
                    break;
                case 2:
                    handleSearchBook(library, scanner);
                    break;
                case 3:
                    handleBorrowBook(library, scanner);
                    break;
                case 4:
                    library.viewLatestHistory();
                    break;
                case 5:
                    // ── Save state before exit ────────────────────────────────
                    System.out.println("\nSaving library state...");
                    library.saveLibraryState(SAVED_CATALOGUE, SAVED_HISTORY);
                    System.out.println("Thank you for using Smart Library. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1-5.");
            }
        }
    }

    // =========================================================================
    //  Menu action handlers
    // =========================================================================

    private static void handleAddBook(LibraryADT library, Scanner scanner) {
        int isbn = readInt(scanner, "Enter ISBN: ");
        if (isbn == -1) return;

        System.out.print("Enter Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Author: ");
        String author = scanner.nextLine();

        library.addBook(isbn, title, author);
        System.out.println("Book added successfully!");
    }

    private static void handleSearchBook(LibraryADT library, Scanner scanner) {
        int isbn = readInt(scanner, "Enter ISBN to search: ");
        if (isbn == -1) return;

        Book book = library.searchBook(isbn);
        if (book != null) {
            System.out.println("Book found: " + book.getTitle() +
                    " by " + book.getAuthor());
        } else {
            System.out.println("Book not found!");
        }
    }

    private static void handleBorrowBook(LibraryADT library, Scanner scanner) {
        int isbn = readInt(scanner, "Enter ISBN to borrow: ");
        if (isbn == -1) return;

        library.borrowBook(isbn);
    }

    // =========================================================================
    //  Input utility
    // =========================================================================

    /**
     * Reads a single integer from stdin with a prompt.
     *
     * @param scanner Scanner instance to read from
     * @param prompt  Text to print before reading
     * @return The integer entered, or {@code -1} on invalid input
     */
    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            int value = scanner.nextInt();
            scanner.nextLine();
            return value;
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter numbers only.");
            scanner.nextLine();
            return -1;
        }
    }
}
