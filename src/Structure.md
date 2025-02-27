## **Function Call Structure**
### **Initialization**
- **core.Main Flow**:
    - `core.Main.main()` → Initializes application
        - Calls `core.UI.handleGUI()` → Creates GUI components and board layout
        - Calls `core.Main.setBoardFromFEN()` → Populates board state from FEN string

### **Point-and-Click Interaction**
- **Selection**:
    - `core.UI.mousePressed()` → Detects click location
        - Calls `core.UI.handleSquareSelection()` → Selects/deselects squares
            - Calls `core.Main.accessibleSquaresOf()` → Calculates valid moves
                - Internally calls `core.Main.getRawMoves()` for piece movement logic
                - Calls `core.Main.isKingInCheck()` for check validation
- **core.Move1 Execution**:
    - `core.UI.mouseReleased()` → Finalizes move
        - Calls `core.Main.tryMovePiece()` → Validates move legality
            - On success: `core.Main.movePiece()` → Updates board state
            - On failure: `core.Main.handleInvalidMoveTo()` → Highlights invalid move

### **Drag-and-Drop Interaction**
- **Dragging**:
    - `core.UI.mouseDragged()` → Updates drag position
        - Triggers `core.UI.repaint()` → Renders dragged piece dynamically
- **Drop**:
    - `core.UI.mouseReleased()` → Same as point-and-click flow
        - Calls `core.Main.tryMovePiece()` → Validates and executes move

### **Movement Logic**
- **Validation Pipeline**:
    - `core.Main.accessibleSquaresOf()` → Generates candidate moves
        - Uses `core.Main.getRawMoves()` for basic piece rules
        - Simulates moves with `core.Main.copyBoard()` and `core.Main.makeMove()`
        - Verifies king safety via `core.Main.isKingInCheck()`
- **Special Moves**:
    - Castling handled in `core.Main.movePiece()` via rook repositioning
    - En passant and pawn promotion *not implemented*

### **Rendering**
- **Square Rendering**:
    - `Square.paintComponent()` → Painted per frame
        - Renders square color (selected/highlighted states)
        - Draws piece images unless being dragged
- **GUI Overlays**:
    - `core.UI.paint()` → Draws on main panel
        - Renders dragged piece during mouse drag
        - Shows valid moves as circles/capture indicators
        - Highlights invalid moves with red flash via `core.Main.handleInvalidMoveTo()`

### **Sound Effects**
- **Capture Sound**:
    - Triggered by `core.Main.handleCapture()` during piece capture
        - Calls `core.UI.playCaptureSound()` → Asynchronously plays WAV file

---

## **Missing**
- **Missing Features**:
    - En passant
    - Checkmate detection
    - Threefold repetition
    - Algebraic notation disambiguation (e.g., "Nbd2")



    Structure problems:
movePiece and makeMove do completely different things, need renaming