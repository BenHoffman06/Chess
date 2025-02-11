## **Function Call Structure**
### **Initialization**
- **Main Flow**:
    - `Main.main()` → Initializes application
        - Calls `UI.handleGUI()` → Creates GUI components and board layout
        - Calls `Main.setBoardFromFEN()` → Populates board state from FEN string

### **Point-and-Click Interaction**
- **Selection**:
    - `UI.mousePressed()` → Detects click location
        - Calls `UI.handleSquareSelection()` → Selects/deselects squares
            - Calls `Main.accessibleSquaresOf()` → Calculates valid moves
                - Internally calls `Main.getRawMoves()` for piece movement logic
                - Calls `Main.isKingInCheck()` for check validation
- **Move Execution**:
    - `UI.mouseReleased()` → Finalizes move
        - Calls `Main.tryMovePiece()` → Validates move legality
            - On success: `Main.movePiece()` → Updates board state
            - On failure: `Main.handleInvalidMoveTo()` → Highlights invalid move

### **Drag-and-Drop Interaction**
- **Dragging**:
    - `UI.mouseDragged()` → Updates drag position
        - Triggers `UI.repaint()` → Renders dragged piece dynamically
- **Drop**:
    - `UI.mouseReleased()` → Same as point-and-click flow
        - Calls `Main.tryMovePiece()` → Validates and executes move

### **Movement Logic**
- **Validation Pipeline**:
    - `Main.accessibleSquaresOf()` → Generates candidate moves
        - Uses `Main.getRawMoves()` for basic piece rules
        - Simulates moves with `Main.copyBoard()` and `Main.makeMove()`
        - Verifies king safety via `Main.isKingInCheck()`
- **Special Moves**:
    - Castling handled in `Main.movePiece()` via rook repositioning
    - En passant and pawn promotion *not implemented*

### **Rendering**
- **Square Rendering**:
    - `Square.paintComponent()` → Painted per frame
        - Renders square color (selected/highlighted states)
        - Draws piece images unless being dragged
- **GUI Overlays**:
    - `UI.paint()` → Draws on main panel
        - Renders dragged piece during mouse drag
        - Shows valid moves as circles/capture indicators
        - Highlights invalid moves with red flash via `Main.handleInvalidMoveTo()`

### **Sound Effects**
- **Capture Sound**:
    - Triggered by `Main.handleCapture()` during piece capture
        - Calls `UI.playCaptureSound()` → Asynchronously plays WAV file

---

## **Missing**
- **Missing Features**:
    - En passant
    - Checkmate detection
    - Threefold repetition
    - Algebraic notation disambiguation (e.g., "Nbd2")